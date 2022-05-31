package net.runelite.client.plugins.oneclickfurnace;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.GroundObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "One Click Furnace",
        description = "Smelts / crafts",
        tags = {"one", "click", "oneclick", "cannonballs","smithing"},
        enabledByDefault = false
)
@Slf4j
public class OneClickFurnacePlugin extends Plugin {
    //if quantities aren't right set them?

    private int bankingState = 1;
    private int timeout = 0;

    @Inject
    private Client client;

    @Inject
    private OneClickFurnaceConfig config;

    @Provides
    OneClickFurnaceConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(OneClickFurnaceConfig.class);
    }

    protected void startUp() throws Exception {
        bankingState = 1;
        timeout = 0;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        //cballs slow as fuck!
        if (client.getLocalPlayer().getAnimation()==899 || client.getLocalPlayer().getAnimation()==827) timeout = 10;
        if (timeout>0) timeout--;
        if (getInventoryItem(config.method().material)==null) timeout = 0;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Furnace"))
            handleClick(event);
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;

        String text= "<col=00ff00>One Click Furnace";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }

    private void handleClick(MenuOptionClicked event) {
        if (config.consumeClicks())
        {
            if (timeout>0
            || (client.getLocalPlayer().isMoving() && !bankOpen()))
            {
                return;
            }
        }

        if (widgetHandler()!=null)
        {
            setMenuEntry(event,widgetHandler());
            return;
        }

        if (bankOpen())
        {
            //set bank quantity to 1
            if (client.getVarbitValue(6590)!=0)
            {
                setMenuEntry(event,createMenuEntry(1, MenuAction.CC_OP, -1, 786460, false));
                return;
            }
            //set bank tab to main tab
            if (client.getVarbitValue(Varbits.CURRENT_BANK_TAB)!=0)
            {
                setMenuEntry(event,createMenuEntry(1, MenuAction.CC_OP, 10, WidgetInfo.BANK_TAB_CONTAINER.getId(), false));
                return;
            }

            switch (bankingState)
            {
                case 1:
                    if (getInventoryItem(config.method().product)!=null)
                    {
                        if (config.method()==CraftingMethods.Molten_Glass)
                        {
                            setMenuEntry(event,depositAll());
                            bankingState = 2;
                            return;
                        }
                        setMenuEntry(event,depositProduct());
                        bankingState = 2;
                        return;
                    }
                    bankingState = 2;
                case 2:
                    if (config.method().material2!=-1)
                    {
                        setMenuEntry(event,withdrawX());
                        bankingState = 3;
                        return;
                    }
                    bankingState = 3;
                case 3:
                    setMenuEntry(event,withdrawAll());
                    bankingState = 4;
                    return;
                case 4:
                    if (useFurnace()==null)
                    {
                        client.addChatMessage(ChatMessageType.BROADCAST,"","Furnace not found. Try Edge or Priff","");
                        return;
                    }
                    setMenuEntry(event,useFurnace());
                    return;
                    //this resets to intial state whenever the bank is opened
            }

        }
        if (getInventoryItem(config.method().material)!=null)
        {
            if (config.method().material2==-1 ||
                    (config.method().material2!=-1 && getInventoryItem(config.method().material2)!=null))
            {
                if (useFurnace()==null)
                {
                    client.addChatMessage(ChatMessageType.BROADCAST,"","Furnace not found. Try Edge or Priff","");
                    return;
                }
                setMenuEntry(event,useFurnace());
                return;
            }
        }
        setMenuEntry(event,bank());
    }

    private GameObject getGameObject(String name) {
        return new GameObjectQuery()
                .nameEquals(name)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private NPC getNPC(String name) {
        return new NPCQuery()
                .nameEquals(name)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject == null) {
            return new Point(0, 0);
        }
        if (tileObject instanceof GameObject) {
            return ((GameObject) tileObject).getSceneMinLocation();
        }
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    private MenuEntry bank() {
        bankingState = 1;
        NPC banker = getNPC("Banker");
        return createMenuEntry(banker.getIndex(), MenuAction.NPC_THIRD_OPTION, 0, 0, false);
    }

    private MenuEntry depositProduct() {
        Widget item = getInventoryItem(config.method().product);
        return createMenuEntry(8, MenuAction.CC_OP_LOW_PRIORITY, item.getIndex(), WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), false);
    }

    private MenuEntry depositAll() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.BANK_DEPOSIT_INVENTORY.getId(), false);
    }

    private MenuEntry withdrawX() {
        int bankIndex = getBankIndex(config.method().material2);
        return createMenuEntry(5, MenuAction.CC_OP, bankIndex, WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
    }

    private MenuEntry withdrawAll() {
        int bankIndex = getBankIndex(config.method().material);
        return createMenuEntry(7, MenuAction.CC_OP_LOW_PRIORITY, bankIndex, WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
    }

    private MenuEntry useFurnace() {
        GameObject furnace = getGameObject("Furnace");
        if (furnace == null) return null;
        return createMenuEntry(furnace.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, getLocation(furnace).getX(), getLocation(furnace).getY(), false);
    }

    private MenuEntry widgetHandler() {
        if (client.getWidget(270,1)!=null
            || client.getWidget(446,1)!=null
            || client.getWidget(6,1)!=null)
        {
            return createMenuEntry(1, MenuAction.CC_OP, -1, config.method().opcode, false);
        }
        return null;
    }

    private Widget getInventoryItem(int id) {
        client.runScript(6009, 9764864, 28, 1, -1); //rebuild inventory ty pajeet
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (bankInventoryWidget!=null && !bankInventoryWidget.isHidden())
        {
            return getWidgetItem(bankInventoryWidget,id);
        }
        if (inventoryWidget!=null) //if hidden check exists then you can't access inventory from any tab except inventory
        {
            return getWidgetItem(inventoryWidget,id);
        }
        return null;
    }

    private Widget getWidgetItem(Widget widget,int id) {
        for (Widget item : widget.getDynamicChildren())
        {
            if (item.getItemId() == id)
            {
                return item;
            }
        }
        return null;
    }

    private int getBankIndex(int ID){
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(ID)
                .result(client)
                .first();
        if (bankItem != null) {
            return bankItem.getWidget().getIndex();
        }
        return -1;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private void setMenuEntry(MenuOptionClicked event, MenuEntry menuEntry){
        event.setId(menuEntry.getIdentifier());
        event.setMenuAction(menuEntry.getType());
        event.setParam0(menuEntry.getParam0());
        event.setParam1(menuEntry.getParam1());
    }
}

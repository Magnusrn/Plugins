package net.runelite.client.plugins.oneclickglassblowing;

import java.util.Collection;
import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import static net.runelite.api.AnimationID.*;

@Extension
@PluginDescriptor(
        name = "One Click Glassblowing",
        enabledByDefault = false,
        description = "One Click Glassblowing. Only works at clan bank or north fossil island bank, Set bank up with fillers and have Glassblowing pipe in inventory. Must be in the main section of bank. credit TP")
@Slf4j
public class OneClickGlassblowingPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OneClickGlassblowingConfig config;

    @Provides
    OneClickGlassblowingConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickGlassblowingConfig.class);
    }

    private int stage = 1;
    private int timeout;

    @Override
    protected void startUp() throws Exception {
        stage = 1;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
        }
        if (client.getLocalPlayer().getAnimation() == CRAFTING_GLASSBLOWING)
        {
            timeout =4;
        }

        if (getInventoryItem(ItemID.MOLTEN_GLASS)==null &! bankOpen())
        {
            timeout=0;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (timeout!=0)
        {
            event.consume();
            return;
        }
        if (event.getMenuOption().equals("<col=00ff00>One Click Glassblowing"))
        {
            handleClick(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        String text;

        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }
        else
        {
            text = "<col=00ff00>One Click Glassblowing";
        }
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }

    private void handleClick(MenuOptionClicked event){
        //System.out.println("stage = " + stage);
        switch (stage)
        {
            case 1:
                event.setMenuEntry(openBank());
                stage = 2;
                timeout++;
                break;
            case 2:
                event.setMenuEntry(depositItems());
                stage = 3;
                break;
            case 3:
                event.setMenuEntry(withdrawAllMoltenGlass());
                stage = 4;
                break;
            case 4:
                event.setMenuEntry(closeBank());
                stage = 5;
                break;
            case 5:
                event.setMenuEntry(useGlassblowingPipe());
                stage = 6;
                break;
            case 6:
                event.setMenuEntry(useOnMoltenGlass());
                stage = 7;
                timeout+=2;
                break;
            case 7:
                event.setMenuEntry(selectGlassblowingItem());
                stage = 1;
                timeout+=2;
                break;
        }
    }

    private MenuEntry openBank(){
        if (config.bankChoice()== GlassblowingType.BankChoice.Clan_Hall)
        {
            int BANK_BOOTH_ID = 10583;
            return createMenuEntry(
                    BANK_BOOTH_ID,
                    MenuAction.GAME_OBJECT_SECOND_OPTION,
                    getLocation(getGameObject(BANK_BOOTH_ID)).getX(),
                    getLocation(getGameObject(BANK_BOOTH_ID)).getY(),
                    false);
        }
        int BANK_CHEST_ID = 30796;
        return createMenuEntry(
                BANK_CHEST_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(BANK_CHEST_ID)).getX(),
                getLocation(getGameObject(BANK_CHEST_ID)).getY(),
                true);
    }

    private MenuEntry depositItems(){
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                786474,
                false);
    }

    private MenuEntry withdrawAllMoltenGlass(){
        return createMenuEntry(
                7,
                MenuAction.CC_OP,
                getBankIndex(),
                786445,
                true);
    }

    private MenuEntry closeBank(){
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                11,
                786434,
                true);
    }

    private MenuEntry useGlassblowingPipe(){
        return createMenuEntry(
                1785,
                MenuAction.ITEM_USE,
                getInventoryItem(1785).getIndex(),
                9764864,
                true);
    }

    private MenuEntry useOnMoltenGlass(){
        return createMenuEntry(
                1775,
                MenuAction.ITEM_USE_ON_WIDGET_ITEM,
                getInventoryItem(1775).getIndex(),
                9764864,
                true);
    }

    private MenuEntry selectGlassblowingItem(){
        int MENU_ID = 0;
        if(config.glassblowingType()==GlassblowingType.GlassblowingItem.Light_Orb)
        {
            MENU_ID = 17694741;
        }
        if(config.glassblowingType()==GlassblowingType.GlassblowingItem.Lantern_Lens)
        {
            MENU_ID = 17694740;
        }

        if(config.glassblowingType()==GlassblowingType.GlassblowingItem.Unpowered_Orb)
        {
            MENU_ID = 17694739;
        }
        if (MENU_ID==0) return null;

        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                MENU_ID,
                false);
    }

    private int getBankIndex(){
        int MOLTEN_GLASS = 1775;
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(MOLTEN_GLASS)
                .result(client)
                .first();
        return bankItem.getWidget().getIndex();
    }

    private WidgetItem getInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                    return item;
                }
            }
        }
        return null;
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
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

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}
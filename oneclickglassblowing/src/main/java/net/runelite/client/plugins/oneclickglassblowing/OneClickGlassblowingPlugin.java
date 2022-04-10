package net.runelite.client.plugins.oneclickglassblowing;

import java.util.Arrays;
import java.util.Collection;
import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
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
        name = "One Click Glass",
        enabledByDefault = false,
        description = "One Click Glassblowing/Superglass Make. Default bank is North of Fossil Island. Check Discord for setup info")
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

    private int glassblowingStage = 1;
    private int superglassMakeStage = 1;
    private int seaweedCount = 0;
    private int timeout;

    @Override
    protected void startUp() throws Exception {
        glassblowingStage = 1;
        superglassMakeStage = 1;
        seaweedCount = 0;
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
        if (event.getMenuOption().equals("<col=00ff00>One Click Molten Glass"))
        {
            if (config.mode()== Types.Mode.GLASSBLOWING)
            {
                blowGlassHandler(event);
                return;
            }
            superGlassMakeHandler(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }
        String text = "<col=00ff00>One Click Molten Glass";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }

    private void blowGlassHandler(MenuOptionClicked event){
        System.out.println("glassblowingStage = " + glassblowingStage + " timeout = " + timeout);
        switch (glassblowingStage)
        {
            case 1:
                event.setMenuEntry(openBank());
                if (!bankOpen())
                {
                    return;
                }
                glassblowingStage = 2;
            case 2:
                event.setMenuEntry(depositItems());
                glassblowingStage = 3;
                return;
            case 3:
                event.setMenuEntry(withdrawAllMoltenGlass());
                glassblowingStage = 4;
                return;
            case 4:
                if (usePipeOnGlass()==null)
                {
                    return;
                }
                event.setMenuEntry(usePipeOnGlass());
                glassblowingStage = 5;
                return;
            case 5:
                event.setMenuEntry(selectGlassblowingItem());
                if (getInventoryItem(ItemID.MOLTEN_GLASS)!=null)
                {
                    return;
                }
                event.setMenuEntry(openBank());
                timeout = 1;
                glassblowingStage = 1;
        }
    }

    private void superGlassMakeHandler(MenuOptionClicked event){
        if (timeout>0) return;
        System.out.println("superglassMakeStage = " + superglassMakeStage);

        switch (superglassMakeStage)
        {
            case 1:
                event.setMenuEntry(openBank());
                seaweedCount = 0;
                timeout = 1;
                if (!bankOpen())
                {
                    return;
                }
                superglassMakeStage = 2;
            case 2:
                event.setMenuEntry(depositItems());
                superglassMakeStage = 3;
                return;
            case 3:
                if ((config.superglassMakeMethod()==Types.SuperGlassMakeMethod.THREE_EIGHTEEN || config.superglassMakeMethod()==Types.SuperGlassMakeMethod.TWO_TWELVE)
                        && withdrawOneSeaweed()==null) return;
                if (config.superglassMakeMethod()==Types.SuperGlassMakeMethod.THIRTEEN_THIRTEEN && withdrawXsodaAshOrSeaweed()==null) return;
                if (seaweedCount<config.superglassMakeMethod().seaweedCount)
                {
                    if (config.superglassMakeMethod()==Types.SuperGlassMakeMethod.THIRTEEN_THIRTEEN)
                    {
                        event.setMenuEntry(withdrawXsodaAshOrSeaweed());
                        seaweedCount++;
                        return;
                    }
                    event.setMenuEntry(withdrawOneSeaweed());
                    seaweedCount++;
                    return;
                }
                superglassMakeStage = 4;
            case 4:
                if (withdrawXSand()==null) return;
                event.setMenuEntry(withdrawXSand());
                superglassMakeStage = 5;
                return;
            case 5:
                event.setMenuEntry(castSuperglassMake());
                timeout = 4;
                superglassMakeStage = 1;
        }
    }

    private MenuEntry castSuperglassMake() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_SUPERGLASS_MAKE.getId(), false);
    }

    private MenuEntry withdrawOneSeaweed() {
        if (getBankIndex(ItemID.GIANT_SEAWEED) ==-1) return null;
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                getBankIndex(ItemID.GIANT_SEAWEED),
                786445,
                true);
    }

    private MenuEntry withdrawXsodaAshOrSeaweed() {
        if (getBankIndex(ItemID.SODA_ASH)!=-1)
        {
            return createMenuEntry(
                    5,
                    MenuAction.CC_OP,
                    getBankIndex(ItemID.SODA_ASH),
                    786445,
                    true);
        }
        if (getBankIndex(ItemID.SEAWEED)!=-1)
        {
            return createMenuEntry(
                    5,
                    MenuAction.CC_OP,
                    getBankIndex(ItemID.SEAWEED),
                    786445,
                    true);
        }
        return null;
    }

    private MenuEntry withdrawXSand() {
        if (getBankIndex(ItemID.BUCKET_OF_SAND) ==-1) return null;
        return createMenuEntry(
                5,
                MenuAction.CC_OP,
                getBankIndex(ItemID.BUCKET_OF_SAND),
                786445,
                true);
    }

    private MenuEntry openBank(){
        if (config.bankType() == Types.Banks.BOOTH) {
            GameObject gameObject = getGameObject(config.bankID());
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_SECOND_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    false);
        }

        if (config.bankType() == Types.Banks.CHEST) {
            GameObject gameObject = getGameObject(config.bankID());
            return createMenuEntry(
                    gameObject.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION,
                    getLocation(gameObject).getX(),
                    getLocation(gameObject).getY(),
                    false);
        }

        if (config.bankType() == Types.Banks.NPC) {
            NPC npc = getNpc(config.bankID());
            return createMenuEntry(
                    npc.getIndex(),
                    MenuAction.NPC_THIRD_OPTION,
                    getNPCLocation(npc).getX(),
                    getNPCLocation(npc).getY(),
                    false);
        }
        return null;
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
        if (getBankIndex(ItemID.MOLTEN_GLASS) ==-1) return null;
        return createMenuEntry(
                7,
                MenuAction.CC_OP,
                getBankIndex(ItemID.MOLTEN_GLASS),
                786445,
                true);
    }

    private MenuEntry usePipeOnGlass(){
        int itemID = ItemID.GLASSBLOWING_PIPE;
        WidgetItem moltenGlass = getInventoryItem(ItemID.MOLTEN_GLASS);
        client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedItemSlot(getInventoryItem(itemID).getIndex());
        client.setSelectedItemID(itemID);
        if (moltenGlass == null) return null;
        return createMenuEntry(moltenGlass.getId(), MenuAction.ITEM_USE_ON_WIDGET_ITEM, moltenGlass.getIndex(), 9764864, true);
    }

    private MenuEntry selectGlassblowingItem(){
        int MENU_ID = config.product().ID;
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                MENU_ID,
                false);
    }

    private int getBankIndex(int id){
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(id)
                .result(client)
                .first();
        if (bankItem == null) return -1;
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

    private NPC getNpc(int id)
    {
        return new NPCQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private Point getNPCLocation(NPC npc)
    {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}
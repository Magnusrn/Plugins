package net.runelite.client.plugins.oneclickteaks;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "One Click Teaks",
        description = "Cuts and banks teaks at Fossil Island.",
        tags = {"one", "click", "oneclick", "teaks", "fossil", "island"}
)
public class OneClickTeaksPlugin extends Plugin {

    private final int BIRD_NEST_WITH_SEEDS = 22798;

    @Inject
    private Client client;

    @Inject
    private OneClickTeaksConfig config;


    @Provides
    OneClickTeaksConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickTeaksConfig.class);
    }

    @Subscribe
    private void onClientTick(ClientTick event) {

        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        String text = "<col=00ff00>One Click Teaks";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }


    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {

        if((client.getLocalPlayer().getAnimation()!=-1|| client.getLocalPlayer().isMoving()) && config.consumeClicks())
        {
            event.consume();
        }
        if (event.getMenuOption().equals("<col=00ff00>One Click Teaks"))
        {
            handleClick(event);
        }
    }

    private void handleClick(MenuOptionClicked event) {
        if (isSouthOfShortcut())
        {
            if (getEmptySlots()==0)
            {
                if (bankOpen())
                {
                    event.setMenuEntry(depositAllMenuEntry());
                    return;
                }
                event.setMenuEntry(getBankMenuEntry());
                return;
            }
            event.setMenuEntry(southToNorthShortcutMenuEntry());
        }

        else if (isNorthOfShortcut())
        {
            if (getEmptySlots()==0)
            {
                event.setMenuEntry(northToSouthShortcutMenuEntry());
                return;
            }
            event.setMenuEntry(cutTeakMenuEntry());
        }
    }

    private MenuEntry depositAllMenuEntry(){
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.BANK_DEPOSIT_INVENTORY.getId(),true);
    }

    private MenuEntry southToNorthShortcutMenuEntry(){
        return createMenuEntry(31481, MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(getGameObject(31481)).getX(), getLocation(getGameObject(31481)).getY(), false);
    }

    private MenuEntry northToSouthShortcutMenuEntry(){
        return createMenuEntry(31482, MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(getGameObject(31482)).getX(), getLocation(getGameObject(31482)).getY(), false);
    }

    private MenuEntry cutTeakMenuEntry(){
        int treeID = getTeakTree().getId();
       return createMenuEntry(treeID, MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(getGameObject(treeID)).getX(), getLocation(getGameObject(treeID)).getY(), false);
    }

    private MenuEntry getBankMenuEntry(){
        return createMenuEntry(31427, MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(getGameObject(31427)).getX(), getLocation(getGameObject(31427)).getY(), false);
    }

    public int getEmptySlots() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryWidget.getWidgetItems().size();
        } else {
            return -1;
        }
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    
    private GameObject getTeakTree()
    {

        HashMap<Integer, Integer> TeakTrees = new HashMap<Integer, Integer>(); //game object ID as key, tree felled varbit as value. value of 0 is choppable.
        TeakTrees.put(30481,4957);
        TeakTrees.put(30480,4955);
        TeakTrees.put(30482,4953);

        List<Integer> ChoppableTrees = new ArrayList<>();

        for (Integer gameObjectID : TeakTrees.keySet()) {
            if (client.getVarbitValue(TeakTrees.get(gameObjectID))==0)
            {
                ChoppableTrees.add(gameObjectID);
            }
        }

        return new GameObjectQuery()
                .idEquals(ChoppableTrees)
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

    private boolean isNorthOfShortcut(){
        WorldPoint DARK_ALTAR_SW_POINT = new WorldPoint(3692,3823,0);
        WorldPoint DARK_ALTAR_NE_POINT = new WorldPoint(3726,3845,0);
        WorldArea DARK_ALTAR_AREA = new WorldArea(DARK_ALTAR_SW_POINT,DARK_ALTAR_NE_POINT);

        return (client.getLocalPlayer().getWorldLocation().isInArea(DARK_ALTAR_AREA));
    }

    private boolean isSouthOfShortcut(){
        WorldPoint SW_POINT = new WorldPoint(3699,3795,0);
        WorldPoint NE_POINT = new WorldPoint(3747,3821,0);
        WorldArea SOUTH_AREA = new WorldArea(SW_POINT,NE_POINT);

        return (client.getLocalPlayer().getWorldLocation().isInArea(SOUTH_AREA));
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}

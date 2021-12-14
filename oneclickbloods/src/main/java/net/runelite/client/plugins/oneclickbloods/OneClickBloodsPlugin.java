package net.runelite.client.plugins.oneclickbloods;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.GroundObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.ArrayList;
import java.util.Collection;

@PluginDescriptor(
        name = "One Click Bloods",
        description = "...",
        tags = {"one","click","bloods","oneclick"},
        enabledByDefault = false
)

public class OneClickBloodsPlugin extends Plugin {


    private int SOUTH_RUNESTONE_VARBIT = 4928;//if varbit == 0 then runestone is mineable
    private int NORTH_RUNESTONE_ID = 8981;
    private int SOUTH_RUNESTONE_ID = 10796;
    private int BLOOD_ALTAR_ID = 27978;
    private int DARK_ALTAR_ID = 27979;

    private int BLOOD_ALTAR_TO_RUNESTONE_SHORTCUT_ID = 27984; //this is a ground object not a game object
    private int RUNESTONE_TO_DARK_ALTAR_SHORTCUT_ID  = 34741; //this is a ground object not a game object

    private int DARK_ESSENCE_FRAGMENTS_ID = 7938;
    private int DENSE_ESSENCE_BLOCK_ID = 13445;
    private int DARK_ESSENCE_BLOCK_ID = 13446;
    private int CHISEL_ID = 1755;

    private int RUNECRAFTING_ANIMATION_ID = 791;
    private int CHISELING_RUNESTONE_ANIMATION_ID = 7201;
    private int CHISELING_DENSE_ESSENCE_ANIMATION_ID = 7202;

    private int timeout;

    private boolean DARK_ESSENCE_FRAGMENTS_PILE_FULL = false;

    @Inject
    private Client client;

    @Inject
    private OneClickBloodsConfig config;

    @Provides
    OneClickBloodsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickBloodsConfig.class);
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }
        String text = "<col=00ff00>One Click Bloods";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Bloods"))
        {
            handleClick(event);
        }
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event)
    {
        if (client.getLocalPlayer().getAnimation()==RUNECRAFTING_ANIMATION_ID)
        {
            DARK_ESSENCE_FRAGMENTS_PILE_FULL=false;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (timeout>0)
        {
            timeout--;
        }
        if (config.afkChisel() && client.getLocalPlayer().getAnimation()==CHISELING_DENSE_ESSENCE_ANIMATION_ID)
        {
            timeout=1;
        }
    }

    private void handleClick(MenuOptionClicked event) {
        if (client.getLocalPlayer().getAnimation() == AnimationID.MINING_RUNE_PICKAXE //if mining runestone
                || client.getLocalPlayer().getAnimation() == AnimationID.MINING_DRAGON_PICKAXE
                || client.getLocalPlayer().getAnimation() == AnimationID.MINING_BLACK_PICKAXE
                || client.getLocalPlayer().getAnimation() == AnimationID.MINING_CRYSTAL_PICKAXE
                || client.getLocalPlayer().getAnimation() == CHISELING_RUNESTONE_ANIMATION_ID)
        {
            event.consume();
            return;
        }
        if(client.getLocalPlayer().isMoving()
                ||client.getLocalPlayer().getPoseAnimation()
                != client.getLocalPlayer().getIdlePoseAnimation())
        {
            event.consume();
            return;
        }

        if (timeout>0)
        {
            event.consume();
            return;
        }

        if (getInventoryItem(DARK_ESSENCE_BLOCK_ID)!=null &! DARK_ESSENCE_FRAGMENTS_PILE_FULL)
        {
            if (client.getWidget(193,2)!=null)
            {
                if (client.getWidget(193,2).getText().contains("Your pile of fragments cannot grow any larger"))
                {
                    DARK_ESSENCE_FRAGMENTS_PILE_FULL = true;
                    client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId()); //repeating code from few lines down, but not sure of alternative. setting as method would be out of place
                    client.setSelectedItemSlot(getInventoryItem(CHISEL_ID).getIndex());
                    client.setSelectedItemID(CHISEL_ID);
                    event.setMenuEntry(useChiselOnRockSlideMES());
                    return;
                }
            }
            client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
            client.setSelectedItemSlot(getInventoryItem(CHISEL_ID).getIndex());
            client.setSelectedItemID(CHISEL_ID);
            event.setMenuEntry(useChiselOnBlockMES());
            return;
        }

        if (isWithinRunestoneArea())
        {
            if (getEmptySlots()>0)
            {
                event.setMenuEntry(mineRunestone());
            }
            else
            {
                event.setMenuEntry(runestoneToDarkAltarAreaShortcutMES());
            }
            return;
        }

        if (isWithinDarkAltarArea())
        {
            if (getInventoryItem(DENSE_ESSENCE_BLOCK_ID)!=null)
            {
                event.setMenuEntry(venerateMES());
                return;
            }

            if (getInventoryItem(DARK_ESSENCE_FRAGMENTS_ID)!=null && getEmptySlots()>20)
            {
                event.setMenuEntry(runestoneToDarkAltarAreaShortcutMES()); //takes this shortcut in reverse
                return;
            }

            client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
            client.setSelectedItemSlot(getInventoryItem(CHISEL_ID).getIndex());
            client.setSelectedItemID(CHISEL_ID);
            event.setMenuEntry(useChiselOnRockSlideMES());
        }

        if (isWithinBloodAltarArea())
        {
            if (getInventoryItem(DARK_ESSENCE_FRAGMENTS_ID)!=null)
            {
                event.setMenuEntry(runecraftMES());
                return;
            }
            event.setMenuEntry(bloodAltarToRunestoneShortcutMES());
        }
    }

    private boolean isWithinRunestoneArea() {
        WorldPoint RUNESTONE_SW_POINT = new WorldPoint(1751,3839,0);
        WorldPoint RUNESTONE_NE_POINT = new WorldPoint(1777,3870,0);
        WorldArea RUNESTONE_AREA = new WorldArea(RUNESTONE_SW_POINT,RUNESTONE_NE_POINT);

        WorldPoint RUNESTONE_AGILITY_SW_POINT = new WorldPoint(1758,3869,0); //creates small area on the south side of the agility shortcut
        WorldPoint RUNESTONE_AGILITY_NE_POINT = new WorldPoint(1763,3873,0);
        WorldArea RUNESTONE_AGILITY_AREA = new WorldArea(RUNESTONE_AGILITY_SW_POINT,RUNESTONE_AGILITY_NE_POINT);

        return (client.getLocalPlayer().getWorldLocation().isInArea(RUNESTONE_AREA) || client.getLocalPlayer().getWorldLocation().isInArea(RUNESTONE_AGILITY_AREA));
    }

    private boolean isWithinDarkAltarArea() {
        WorldPoint DARK_ALTAR_SW_POINT = new WorldPoint(1714,3874,0);
        WorldPoint DARK_ALTAR_NE_POINT = new WorldPoint(1762,3889,0);
        WorldArea DARK_ALTAR_AREA = new WorldArea(DARK_ALTAR_SW_POINT,DARK_ALTAR_NE_POINT);

        return (client.getLocalPlayer().getWorldLocation().isInArea(DARK_ALTAR_AREA));
    }

    private boolean isWithinBloodAltarArea() {
        WorldPoint BLOOD_ALTAR_SW_POINT = new WorldPoint(1707,3822,0);
        WorldPoint BLOOD_ALTAR_NE_POINT = new WorldPoint(1742,3860,0);
        WorldArea BLOOD_ALTAR_AREA = new WorldArea(BLOOD_ALTAR_SW_POINT,BLOOD_ALTAR_NE_POINT);
        return (client.getLocalPlayer().getWorldLocation().isInArea(BLOOD_ALTAR_AREA));
    }

    private Point getLocation(TileObject tileObject) {
        if(tileObject == null){
            return new Point(0 , 0);
        }
        if (tileObject instanceof GameObject)
            return ((GameObject)tileObject).getSceneMinLocation();
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private GroundObject getGroundObject(int ID) {
        return new GroundObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private GameObject getRockslide()
    {
        WorldPoint ROCKSLIDE_SW_POINT = new WorldPoint(1739,3852,0);
        WorldPoint ROCKSLIDE_NE_POINT = new WorldPoint(1742,3855,0);
        WorldArea ROCKSLIDE_AREA = new WorldArea(ROCKSLIDE_SW_POINT,ROCKSLIDE_NE_POINT);

        ArrayList<GameObject> list = new GameObjectQuery()
                .idEquals(11428)
                .result(client)
                .list;
        for (GameObject item:list)
        {
            if (item.getWorldLocation().isInArea(ROCKSLIDE_AREA))
            {
                return (item);
            }
        }
        return null;
    }

    private MenuEntry runestoneToDarkAltarAreaShortcutMES(){
        return new MenuEntry(
                "Climb",
                "<col=ffff>Rocks",
                RUNESTONE_TO_DARK_ALTAR_SHORTCUT_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                getLocation(getGroundObject(RUNESTONE_TO_DARK_ALTAR_SHORTCUT_ID)).getX(),
                getLocation(getGroundObject(RUNESTONE_TO_DARK_ALTAR_SHORTCUT_ID)).getY(),
                false);
    }

    private MenuEntry venerateMES() {
        return new MenuEntry(
                "Venerate",
                "<col=ffff>Dark Altar",
                DARK_ALTAR_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                getLocation(getGameObject(DARK_ALTAR_ID)).getX(),
                getLocation(getGameObject(DARK_ALTAR_ID)).getY(),
                false);
    }

    private MenuEntry useChiselOnBlockMES() {
        return new MenuEntry(
                "Use",
                "<col=ff9040>Chisel<col=ffffff> -> <col=ff9040>Dark essence block",
                DARK_ESSENCE_BLOCK_ID,
                MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(),
                getLastInventoryItem(DARK_ESSENCE_BLOCK_ID).getIndex(),
                9764864,
                false);
    }

    private MenuEntry useChiselOnRockSlideMES() {
        return new MenuEntry(
                "Use",
                "<col=ff9040>Chisel<col=ffffff> -> <col=ffff>Rockslide",
                11428,
                MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(),
                getRockslide().getSceneMinLocation().getX(),
                getRockslide().getSceneMinLocation().getY(),
                false);
    }

    private MenuEntry runecraftMES() {
        return new MenuEntry(
                "Bind",
                "<col=ffff>Blood Altar",
                BLOOD_ALTAR_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                getLocation(getGameObject(BLOOD_ALTAR_ID)).getX(),
                getLocation(getGameObject(BLOOD_ALTAR_ID)).getY(),
                false);
    }

    private MenuEntry bloodAltarToRunestoneShortcutMES(){
        return new MenuEntry(
                "Climb",
                "<col=ffff>Rocks",
                BLOOD_ALTAR_TO_RUNESTONE_SHORTCUT_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                getLocation(getGroundObject(BLOOD_ALTAR_TO_RUNESTONE_SHORTCUT_ID)).getX(),
                getLocation(getGroundObject(BLOOD_ALTAR_TO_RUNESTONE_SHORTCUT_ID)).getY(),
                false);
    }

    private MenuEntry mineRunestone(){
        int RUNESTONE_ID = NORTH_RUNESTONE_ID;
        if (client.getVarbitValue(SOUTH_RUNESTONE_VARBIT)==0) //if south runestone is mineable
        {
            RUNESTONE_ID = SOUTH_RUNESTONE_ID;
        }

        return new MenuEntry(
                "Chip",
                "<col=ffff>Dense runestone",
                RUNESTONE_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                getLocation(getGameObject(RUNESTONE_ID)).getX(),
                getLocation(getGameObject(RUNESTONE_ID)).getY(),
                false);
    }

    public int getEmptySlots() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryWidget.getWidgetItems().size();
        } else {
            return -1;
        }
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

    private WidgetItem getLastInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            int LastIndex = -1;
            WidgetItem LastItem = null;
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                   if (item.getIndex()>LastIndex) {
                   LastIndex = item.getIndex();
                   LastItem = item;
                   }
                }
            }
            return LastItem;
        }
        return null;
    }
}

/*TODO
MAYBE ADD OPTION TO NOT ONE CLICK CHISEL OR  ONE CLICK USE CHISEL ON ROCKS
ADD EXTREME CONSUME CLICKS TO ALLOW FOR FAST AS FK CLICKING?
ADD CONFIG OPTION FOR CONSUME CLICKS
ADD MANUAL WALK INSTEAD OF USING CHISEL ON ROCK
 */
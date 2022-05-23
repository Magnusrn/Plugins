package net.runelite.client.plugins.oneclickbloods;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.GroundObjectQuery;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;

import java.util.*;

@Extension
@PluginDescriptor(
        name = "One Click Bloods",
        description = "Start at the runestone area, sometimes bugs out otherwise.",
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
    private boolean SHOULD_RUN_TO_ALTAR = false;

    @Inject
    private Client client;

    @Inject
    private OneClickBloodsConfig config;

    @Provides
    OneClickBloodsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(OneClickBloodsConfig.class);
    }

    public void walkTile() { //walks to a random point within the area visible of the blood altar
        WorldPoint sw = new WorldPoint(1735,3844,0);
        WorldPoint nw = new WorldPoint(1740,3856,0);
        WorldArea worldArea = new WorldArea(sw,nw);
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(worldArea.toWorldPointList().size());
        WorldPoint randomPoint = worldArea.toWorldPointList().get(index);
        int x = randomPoint.getX() - client.getBaseX();
        int y = randomPoint.getY() - client.getBaseY();
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (client.getWidget(193,2)!=null)
        {
            if (client.getWidget(193,2).getText().contains("Your pile of fragments cannot grow any larger"))
            {
                DARK_ESSENCE_FRAGMENTS_PILE_FULL = true;
                SHOULD_RUN_TO_ALTAR = true;
            }
        }

        if (client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;
        String text = "<col=00ff00>One Click Bloods";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
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
        if (client.getLocalPlayer()!=null)
        {
            if ((client.getLocalPlayer()).getAnimation()==RUNECRAFTING_ANIMATION_ID)
            {
                DARK_ESSENCE_FRAGMENTS_PILE_FULL=false;
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
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
            Print("consuming click while mining");
            event.consume();
            return;
        }
        if(client.getLocalPlayer().isMoving())
        {
            Print("consuming click while moving");
            event.consume();
            return;
        }

        if (timeout>0)
        {
            Print("consuming click as timeout>0");
            event.consume();
            return;
        }

        if (getInventoryItem(DARK_ESSENCE_BLOCK_ID)!=null &! DARK_ESSENCE_FRAGMENTS_PILE_FULL)
        {
            Print("Chiselling Essence");
            setSelectedInventoryItem(getInventoryItem(CHISEL_ID));
            setMenuEntry(event,useChiselOnBlockMES());
            return;
        }

        if (isWithinRunestoneArea())
        {
            if (getEmptySlots()>0)
            {
                Widget bloodEss= getInventoryItem(ItemID.BLOOD_ESSENCE);
                if(bloodEss!=null) {
                    Widget activebloodEss = getInventoryItem(ItemID.BLOOD_ESSENCE_ACTIVE);
                    if(activebloodEss==null){
                        Print("Activating Blood Essence");
                        setMenuEntry(event,activateBloodEssenceMES(bloodEss.getIndex()));
                        return;
                    }
                }
                if(config.useSpec()) {
                    if (client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000) {
                        Print("Using Special Attack");
                        setMenuEntry(event,specAtk());
                        return;
                    }
                }
                Print("Mining Runestone");
                setMenuEntry(event,mineRunestone());
            }
            else
            {
                Print("Trying to use Runestone -> Dark Altar shortcut");
                setMenuEntry(event,runestoneToDarkAltarAreaShortcutMES());
            }
            return;
        }

        if (isWithinDarkAltarArea())
        {
            if (getInventoryItem(DENSE_ESSENCE_BLOCK_ID)!=null)
            {
                Print("Venerating");
                setMenuEntry(event,venerateMES());
                return;
            }

            if (getInventoryItem(DARK_ESSENCE_FRAGMENTS_ID)!=null && getEmptySlots()>20)
            {
                Print("Trying to use Runestone -> Dark Altar Shortcut");
                setMenuEntry(event,runestoneToDarkAltarAreaShortcutMES()); //takes this shortcut in reverse
                return;
            }

            if (SHOULD_RUN_TO_ALTAR)
            {
                Print("Trying to walk to random point before blood area is visible");
                walkTile();
                return;
            }
        }

        if (isWithinBloodAltarArea())
        {
            SHOULD_RUN_TO_ALTAR = false;
            if (getInventoryItem(DARK_ESSENCE_FRAGMENTS_ID)!=null)
            {
                Print("Runecrafting");
                setMenuEntry(event,runecraftMES());
                return;
            }
            Print("Trying to use Blood Altar -> Runestone Shortcut");
            setMenuEntry(event,bloodAltarToRunestoneShortcutMES());
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

    private MenuEntry runestoneToDarkAltarAreaShortcutMES(){
        return createMenuEntry(
                RUNESTONE_TO_DARK_ALTAR_SHORTCUT_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGroundObject(RUNESTONE_TO_DARK_ALTAR_SHORTCUT_ID)).getX(),
                getLocation(getGroundObject(RUNESTONE_TO_DARK_ALTAR_SHORTCUT_ID)).getY(),
                false);
    }

    private MenuEntry venerateMES() {
        return createMenuEntry(
                DARK_ALTAR_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(DARK_ALTAR_ID)).getX(),
                getLocation(getGameObject(DARK_ALTAR_ID)).getY(),
                false);
    }

    private MenuEntry useChiselOnBlockMES() {
        return createMenuEntry(
               0,
                MenuAction.WIDGET_TARGET_ON_WIDGET,
                getLastInventoryItem(DARK_ESSENCE_BLOCK_ID).getIndex(),
                9764864,
                false);
    }
    private MenuEntry runecraftMES() {
        return createMenuEntry(
                BLOOD_ALTAR_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(BLOOD_ALTAR_ID)).getX(),
                getLocation(getGameObject(BLOOD_ALTAR_ID)).getY(),
                false);
    }

    private MenuEntry bloodAltarToRunestoneShortcutMES(){
        return createMenuEntry(
                BLOOD_ALTAR_TO_RUNESTONE_SHORTCUT_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
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

        return createMenuEntry(
                RUNESTONE_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(RUNESTONE_ID)).getX(),
                getLocation(getGameObject(RUNESTONE_ID)).getY(),
                false);
    }

    private MenuEntry specAtk(){
        Widget specAtk = client.getWidget(WidgetInfo.MINIMAP_SPEC_CLICKBOX);
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                specAtk.getId(),
                false);
    }

    private MenuEntry activateBloodEssenceMES(int slot){
        return createMenuEntry(
                ItemID.BLOOD_ESSENCE,
                MenuAction.ITEM_FIRST_OPTION,
                slot,
                WidgetInfo.INVENTORY.getId(),
                false);
    }

    private int getEmptySlots() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());

        if (bankInventory!=null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren()!=null)
        {
            return getEmptySlots(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        }

        if (inventory!=null && inventory.getDynamicChildren()!=null)
        {
            return getEmptySlots(WidgetInfo.INVENTORY);
        }

        return -1;
    }

    private int getEmptySlots(WidgetInfo widgetInfo) {
        client.runScript(6009, 9764864, 28, 1, -1);
        List<Widget> inventoryItems = Arrays.asList(client.getWidget(widgetInfo.getId()).getDynamicChildren());
        return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
    }

    private Widget getInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return getWidgetItem(inventoryWidget,id);
        }
        if (bankInventoryWidget!=null && !bankInventoryWidget.isHidden())
        {
            return getWidgetItem(bankInventoryWidget,id);
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

    private Widget getLastInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return getLastWidgetItem(inventoryWidget,id);
        }
        return null;
    }

    private Widget getLastWidgetItem(Widget widget,int id) {
        return Arrays.stream(widget.getDynamicChildren())
                .filter(item -> item.getItemId()==id)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private void setSelectedInventoryItem(Widget item) {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(item.getIndex());
        client.setSelectedSpellItemId(item.getItemId()); //this might be different inside/outside bank? getID works sometimes idfk
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private void setMenuEntry(MenuOptionClicked event, MenuEntry menuEntry){
        event.setId(menuEntry.getIdentifier());
        event.setMenuAction(menuEntry.getType());
        event.setParam0(menuEntry.getParam0());
        event.setParam1(menuEntry.getParam1());
    }

    private void Print(String string){
        if (config.debug())
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE,"",string,"");
        }
    }
}
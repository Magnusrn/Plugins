package net.runelite.client.plugins.oneclickcustom;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.plugins.oneclickcustom.utils.GetObjects;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.oneclickcustom.utils.Inventory;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(
        name = "One Click Custom",
        description = "Sets the Menu entry for left click anywhere",
        tags = {"one click","custom","oneclick"},
        enabledByDefault = false
)
@Slf4j
public class oneClickCustomPlugin extends Plugin{
    /*TODO Switch to caching*/

    List<TileItem> GroundItems = new ArrayList<>();

    @Inject
    private Client client;
    @Inject
    private GetObjects objects;
    @Inject
    private Inventory inventory;
    @Inject
    private oneClickCustomConfig config;
    @Inject
    private ConfigManager configManager;

    @Provides
    oneClickCustomConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(oneClickCustomConfig.class);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        GroundItems.clear();
    }

    @Subscribe
    private void onItemSpawned(ItemSpawned event)
    {
        for (TileItem item : GroundItems)
        {
            if (item == event.getItem()) //Don't add if item already exists, prevents doubling when crossing loading lines
            {
                return;
            }
        }
        if (getConfigIds().contains(event.getItem().getId()))
        {
            GroundItems.add(event.getItem());
        }
    }

    @Subscribe
    private void onItemDespawned(ItemDespawned event)
    {
        if (getConfigIds().contains(event.getItem().getId()))
        {
            GroundItems.remove(event.getItem());
        }
    }

    @Override
    protected void startUp() throws Exception
    {
        GroundItems.clear();
    }
    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (config.oneClickType()!=oneClickCustomTypes.methods.Use_Item_On_X) return;

        if (0 <= event.getActionParam0() && event.getActionParam0()<= 27
                && event.getOption().equals("Use"))
        {
            Widget item = inventory.getItemByInventoryIndex(event.getActionParam0());

            if (getItemOnNPCsHashMap().get(item.getItemId())!=null) //if inventory item exists in config list
            {
                NPC nearestnpc = objects.getNpc(getItemOnNPCsHashMap().get(item.getItemId())); //gets nearest npc from key and checks if visible
                if (nearestnpc!=null)
                {
                    insertInventoryMenu(event, item.getItemId());
                }
            }

            if (getItemOnGameObjectsHashMap().get(item.getItemId())!=null) //if inventory item exists in config list
            {
                GameObject nearestGameObject = objects.getGameObject(getItemOnGameObjectsHashMap().get(item.getItemId())); //gets nearest gameObject from key and checks if visible
                if (nearestGameObject!=null)
                {
                    insertInventoryMenu(event, item.getItemId());
                }
            }
        }
    }

    private void insertInventoryMenu(MenuEntryAdded event,int ID) {
        client.insertMenuItem(
                "One Click Custom",
                "",
                MenuAction.UNKNOWN.getId(),
                event.getIdentifier(),
                event.getActionParam0(),
                event.getActionParam1(),
                true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries())
                .filter(x->x.getOption().equals("One Click Custom"))
                .findFirst().orElse(null));
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if(event.getMenuOption().equals("<col=00ff00>One Click Custom"))
        {
            if((client.getLocalPlayer().getAnimation()!=-1|| client.getLocalPlayer().isMoving()) && config.consumeClick() && config.oneClickType()!=oneClickCustomTypes.methods.Pickpocket)
            {
                event.consume();
            }
            handleClick(event);
        }

        if (event.getMenuOption().equals("One Click Custom"))
        {
            handleInventoryItemClicked(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (config.oneClickType()==oneClickCustomTypes.methods.Use_Item_On_X) return;

        if (config.oneClickType()==oneClickCustomTypes.methods.Gather && objects.getGameObject(getConfigIds())==null) return;

        if ((GroundItems.size()==0 || objects.getNearestTileItem(GroundItems)==null) && config.oneClickType() == oneClickCustomTypes.methods.Pick_Up) return;

        if (objects.getNpc(getConfigIds())==null &!(config.oneClickType()==oneClickCustomTypes.methods.Gather) &! (config.oneClickType() == oneClickCustomTypes.methods.Pick_Up)) return;

        if (inventory.getEmptySlots()==0 && config.InventoryFull() && config.oneClickType()!=oneClickCustomTypes.methods.Attack) return;

        if(client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;
        String text =  "<col=00ff00>One Click Custom";
        client.insertMenuItem(text,"", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries())
                .filter(x->x.getOption().equals("<col=00ff00>One Click Custom"))
                .findFirst().orElse(null));
    }

    private void handleClick(MenuOptionClicked event) {
        if (inventory.getEmptySlots()==0 && config.InventoryFull()) return;
        event.setMenuEntry(setCustomMenuEntry());
    }

    private void handleInventoryItemClicked(MenuOptionClicked event) { //hella copy paste code fix this dumb shit. Maybe rework whole plugin tbh kinda bodged.
        int itemID = event.getWidget().getItemId();
        inventory.setSelectedInventoryItem(inventory.getLastInventoryItem(itemID));
        if (getItemOnNPCsHashMap().get(itemID)!=null)
        {
            NPC nearestnpc = objects.getNpc(getItemOnNPCsHashMap().get(itemID));
            if (nearestnpc!=null)
            {
                event.setMenuEntry(createMenuEntry(nearestnpc.getIndex(),
                        MenuAction.WIDGET_TARGET_ON_NPC,
                        getLocation(nearestnpc).getX(),
                        getLocation(nearestnpc).getY(),
                        false));
                return;
            }
        }

        if (getItemOnGameObjectsHashMap().get(itemID)!=null)
        {
            GameObject nearestGameObject = objects.getGameObject(getItemOnGameObjectsHashMap().get(itemID));

            if (nearestGameObject!=null)
            {
                event.setMenuEntry(createMenuEntry(nearestGameObject.getId(),
                        MenuAction.WIDGET_TARGET_ON_GAME_OBJECT,
                        getLocation(nearestGameObject).getX(),
                        getLocation(nearestGameObject).getY(),
                        false));
            }
        }
    }

    private MenuEntry setCustomMenuEntry() {
        if (config.Bank()&&(config.oneClickType()==oneClickCustomTypes.methods.Fish ||
                config.oneClickType()==oneClickCustomTypes.methods.Gather||
                config.oneClickType()==oneClickCustomTypes.methods.Pickpocket||
                config.oneClickType()==oneClickCustomTypes.methods.Pick_Up))
        {
            if (inventory.getEmptySlots()==0)
            {
                if (depositBoxOpen())
                { //deposit all
                    return createMenuEntry(1, MenuAction.CC_OP, -1, 12582916, false);
                }
                if (bankOpen())
                { //deposit all
                    return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.BANK_DEPOSIT_INVENTORY.getId(), false);
                }

                if (bankVisible()) {
                    if (config.bankType() == oneClickCustomTypes.bankTypes.Booth) {
                        GameObject gameObject = objects.getGameObject(config.bankID());
                        return createMenuEntry(
                                gameObject.getId(),
                                MenuAction.GAME_OBJECT_SECOND_OPTION,
                                getLocation(gameObject).getX(),
                                getLocation(gameObject).getY(),
                                false);
                    }

                    if (config.bankType() == oneClickCustomTypes.bankTypes.Chest) {
                        GameObject gameObject = objects.getGameObject(config.bankID());
                        return createMenuEntry(
                                gameObject.getId(),
                                MenuAction.GAME_OBJECT_FIRST_OPTION,
                                getLocation(gameObject).getX(),
                                getLocation(gameObject).getY(),
                                false);
                    }

                    if (config.bankType() == oneClickCustomTypes.bankTypes.NPC) {
                        NPC npc = objects.getNpc(config.bankID());
                        return createMenuEntry(
                                npc.getIndex(),
                                MenuAction.NPC_THIRD_OPTION,
                                getLocation(npc).getX(),
                                getLocation(npc).getY(),
                                false);
                    }
                }
            }
        }

        if (config.oneClickType()==oneClickCustomTypes.methods.Pick_Up)
        {
            if (!GroundItems.isEmpty()) {
                TileItem tileItem = objects.getNearestTileItem(GroundItems);
                return createMenuEntry(
                        objects.getNearestTileItem(GroundItems).getId(),
                        MenuAction.GROUND_ITEM_THIRD_OPTION,
                        tileItem.getTile().getSceneLocation().getX(),
                        tileItem.getTile().getSceneLocation().getY(),
                        true);
            }
            return null;
        }

        if (config.oneClickType()==oneClickCustomTypes.methods.Gather)
        {
            MenuAction action = getMenuAction();

            GameObject customGameObject = objects.getGameObject(getConfigIds());
            return createMenuEntry(
                    customGameObject.getId(),
                    action,
                    getLocation(customGameObject).getX(),
                    getLocation(customGameObject).getY(),
                    true);
        }

        NPC customNPCObject = objects.getNpc(getConfigIds());

        if(config.oneClickType()==oneClickCustomTypes.methods.Fish)
        {
            MenuAction action = getMenuAction();
            return createMenuEntry(
                    customNPCObject.getIndex(),
                    action,
                    getLocation(customNPCObject).getX(),
                    getLocation(customNPCObject).getY(),
                    true);
        }

        if (config.oneClickType()==oneClickCustomTypes.methods.Attack)
        {
            NPC nearestAliveNPC = objects.getNearestAliveNPC(getConfigIds());
            return createMenuEntry(
                    nearestAliveNPC.getIndex(),
                    MenuAction.NPC_SECOND_OPTION,
                    getLocation(nearestAliveNPC).getX(),
                    getLocation(nearestAliveNPC).getY(),
                    true);
        }

        if(config.oneClickType()==oneClickCustomTypes.methods.Pickpocket)
        {
            return createMenuEntry(
                    customNPCObject.getIndex(),
                    MenuAction.NPC_THIRD_OPTION,
                    getLocation(customNPCObject).getX(),
                    getLocation(customNPCObject).getY(),
                    true);
        }
        return null;
    }

    private MenuAction getMenuAction() {
        MenuAction npcMenuAction = MenuAction.NPC_FIRST_OPTION;
        MenuAction objectMenuAction = MenuAction.GAME_OBJECT_FIRST_OPTION ;
        switch(config.opcode())
        {
            case 2:
                objectMenuAction = MenuAction.GAME_OBJECT_SECOND_OPTION;
                npcMenuAction = MenuAction.NPC_SECOND_OPTION;
                break;
            case 3:
                objectMenuAction = MenuAction.GAME_OBJECT_THIRD_OPTION;
                npcMenuAction = MenuAction.NPC_THIRD_OPTION;
                break;
            case 4:
                objectMenuAction = MenuAction.GAME_OBJECT_FOURTH_OPTION;
                npcMenuAction = MenuAction.NPC_FOURTH_OPTION;
                break;
            case 5:
                objectMenuAction = MenuAction.GAME_OBJECT_FIFTH_OPTION;
                npcMenuAction = MenuAction.NPC_FIFTH_OPTION;
                break;
        }
        return config.oneClickType()==oneClickCustomTypes.methods.Fish ? npcMenuAction : objectMenuAction;
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject instanceof GameObject) return ((GameObject) tileObject).getSceneMinLocation();
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    private Point getLocation(NPC npc) {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }

    private boolean bankVisible(){
        if (config.bankType() == oneClickCustomTypes.bankTypes.Booth || config.bankType() == oneClickCustomTypes.bankTypes.Chest)
        {
            return objects.getGameObject(config.bankID())!=null;
        }
        return objects.getNpc(config.bankID())!=null;
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private boolean depositBoxOpen() {
        return client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER) != null && !client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER).isHidden();
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private List<Integer> getConfigIds(){
        List<String> IdList = Arrays.asList((config.IDs().strip()).split(","));
        return IdList.stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    private HashMap<Integer, List<Integer>> getItemOnGameObjectsHashMap()
    {
        return parseConfigString(config.itemOnGameObjectString());
    }

    private HashMap<Integer, List<Integer>> getItemOnNPCsHashMap()
    {
        return parseConfigString(config.itemOnNpcString());
    }

    private HashMap<Integer, List<Integer>> parseConfigString(String ConfigString)
    {
        HashMap<Integer, List<Integer>> IDs = new HashMap<>();

        if (!ConfigString.trim().isEmpty())
        {
            for (String line : ConfigString.trim().split("\n"))
            {
                Pattern pattern = Pattern.compile("^(\\d*,)*\\d*$"); //regex to allow for commenting, skips line if not correct format.
                Matcher matcher = pattern.matcher(line);
                boolean matchFound = matcher.find();
                if (!matchFound || line.equals("")) continue; //idk why this needs the empty line check why doesn't empty line fail the regex

                List<Integer> lineIDs = new ArrayList<>();
                for(String id : line.split(",")) {
                    lineIDs.add(Integer.parseInt(id));
                }
                Integer key = lineIDs.get(0);
                List<Integer> values = lineIDs.subList(1, lineIDs.size());
                IDs.put(key,values);
            }
        }
        return IDs;
    }
}
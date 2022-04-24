package net.runelite.client.plugins.oneclickcustom;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (config.oneClickType()!=oneClickCustomTypes.Use_Item_On_X)
        {
            return;
        }

        if (0 <= event.getActionParam0() && event.getActionParam0()<= 27
                && event.getOption().equals("Use"))
        {
            Widget item = getItemByInventoryIndex(event.getActionParam0());

            if (getItemOnNPCsHashMap().get(item.getItemId())!=null) //if inventory item exists in config list
            {
                NPC nearestnpc = getNpc(getItemOnNPCsHashMap().get(item.getItemId())); //gets nearest npc from key and checks if visible
                if (nearestnpc!=null)
                {
                    insertInventoryMenu(event, item.getItemId());
                }
            }

            if (getItemOnGameObjectsHashMap().get(item.getItemId())!=null) //if inventory item exists in config list
            {
                GameObject nearestGameObject = getGameObject(getItemOnGameObjectsHashMap().get(item.getItemId())); //gets nearest gameObject from key and checks if visible
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
    public void onMenuOptionClicked(MenuOptionClicked event)
    {

        if(event.getMenuOption().equals("<col=00ff00>One Click Custom"))
        {
            if((client.getLocalPlayer().getAnimation()!=-1|| client.getLocalPlayer().isMoving()) && config.consumeClick() && config.oneClickType()!=oneClickCustomTypes.Pickpocket)
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
    private void onClientTick(ClientTick event) //fix this baloney
    {
        if (config.oneClickType()==oneClickCustomTypes.Use_Item_On_X)
        {
            return;
        }

        if (config.oneClickType()==oneClickCustomTypes.Gather && getGameObject(getConfigIds())==null)
        {
            return;
        }

        if ((GroundItems.size()==0 || getNearestTileItem(GroundItems)==null) && config.oneClickType() == oneClickCustomTypes.Pick_Up)
        {
            return;
        }

        if (getNpc(getConfigIds())==null &!(config.oneClickType()==oneClickCustomTypes.Gather) &! (config.oneClickType() == oneClickCustomTypes.Pick_Up))
        {
            return;
        }

        if (getEmptySlots()==0 && config.InventoryFull() && config.oneClickType()!=oneClickCustomTypes.Attack)
        {
            return;
        }

        if(client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;
        String text =  "<col=00ff00>One Click Custom";
        client.insertMenuItem(text,"", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries())
                .filter(x->x.getOption().equals("<col=00ff00>One Click Custom"))
                .findFirst().orElse(null));
    }

    private void handleClick(MenuOptionClicked event)
    {
        if (getEmptySlots()==0 && config.InventoryFull())
        {
            return;
        }
        event.setMenuEntry(setCustomMenuEntry());
    }

    private void handleInventoryItemClicked(MenuOptionClicked event) { //hella copy paste code fix this dumb shit. Maybe rework whole plugin tbh kinda bodged.
        int itemID = event.getWidget().getItemId();
        setSelectedInventoryItem(getLastInventoryItem(itemID));
        if (getItemOnNPCsHashMap().get(itemID)!=null)
        {
            NPC nearestnpc = getNpc(getItemOnNPCsHashMap().get(itemID));
            if (nearestnpc!=null)
            {
                event.setMenuEntry(createMenuEntry(nearestnpc.getIndex(),
                        MenuAction.WIDGET_TARGET_ON_NPC,
                        getNPCLocation(nearestnpc).getX(),
                        getNPCLocation(nearestnpc).getY(),
                        false));
                return;
            }
        }

        if (getItemOnGameObjectsHashMap().get(itemID)!=null)
        {
            GameObject nearestGameObject = getGameObject(getItemOnGameObjectsHashMap().get(itemID));

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

    private MenuEntry setCustomMenuEntry()
    {
        if (config.Bank()&&(config.oneClickType()==oneClickCustomTypes.Fish ||
                config.oneClickType()==oneClickCustomTypes.Gather||
                config.oneClickType()==oneClickCustomTypes.Pickpocket||
                config.oneClickType()==oneClickCustomTypes.Pick_Up))
        {
            if (getEmptySlots()==0)
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
                    if (config.bankType() == oneClickCustomBankTypes.Booth) {
                        GameObject gameObject = getGameObject(config.bankID());
                        return createMenuEntry(
                                gameObject.getId(),
                                MenuAction.GAME_OBJECT_SECOND_OPTION,
                                getLocation(gameObject).getX(),
                                getLocation(gameObject).getY(),
                                false);
                    }

                    if (config.bankType() == oneClickCustomBankTypes.Chest) {
                        GameObject gameObject = getGameObject(config.bankID());
                        return createMenuEntry(
                                gameObject.getId(),
                                MenuAction.GAME_OBJECT_FIRST_OPTION,
                                getLocation(gameObject).getX(),
                                getLocation(gameObject).getY(),
                                false);
                    }

                    if (config.bankType() == oneClickCustomBankTypes.NPC) {
                        NPC npc = getNpc(config.bankID());
                        return createMenuEntry(
                                npc.getIndex(),
                                MenuAction.NPC_THIRD_OPTION,
                                getNPCLocation(npc).getX(),
                                getNPCLocation(npc).getY(),
                                false);
                    }
                }
            }
        }

        if (config.oneClickType()==oneClickCustomTypes.Pick_Up)
        {
            if (!GroundItems.isEmpty()) {
                TileItem tileItem = getNearestTileItem(GroundItems);
                return createMenuEntry(
                        getNearestTileItem(GroundItems).getId(),
                        MenuAction.GROUND_ITEM_THIRD_OPTION,
                        tileItem.getTile().getSceneLocation().getX(),
                        tileItem.getTile().getSceneLocation().getY(),
                        true);
            }
            return null;
        }

        if (config.oneClickType()==oneClickCustomTypes.Gather)
        {
            MenuAction action =MenuAction.GAME_OBJECT_FIRST_OPTION ;
            switch(config.opcode()){
                case 1:
                    action = MenuAction.GAME_OBJECT_FIRST_OPTION;
                    break;
                case 2:
                    action = MenuAction.GAME_OBJECT_SECOND_OPTION;
                    break;
                case 3:
                    action = MenuAction.GAME_OBJECT_THIRD_OPTION;
                    break;
                case 4:
                    action = MenuAction.GAME_OBJECT_FOURTH_OPTION;
                    break;
                case 5:
                    action = MenuAction.GAME_OBJECT_FIFTH_OPTION;
                    break;
            }
            GameObject customGameObject = getGameObject(getConfigIds());
            return createMenuEntry(
                    customGameObject.getId(),
                    action,
                    getLocation(customGameObject).getX(),
                    getLocation(customGameObject).getY(),
                    true);
        }

        NPC customNPCObject = getNpc(getConfigIds());

        if(config.oneClickType()==oneClickCustomTypes.Fish)
        {
            MenuAction action = MenuAction.NPC_FIRST_OPTION;
            switch(config.opcode()){
                case 1:
                    action = MenuAction.NPC_FIRST_OPTION;
                    break;
                case 2:
                    action = MenuAction.NPC_SECOND_OPTION;
                    break;
                case 3:
                    action = MenuAction.NPC_THIRD_OPTION;
                    break;
                case 4:
                    action = MenuAction.NPC_FOURTH_OPTION;
                    break;
                case 5:
                    action = MenuAction.NPC_FIFTH_OPTION;
                    break;
            }

            return createMenuEntry(
                    customNPCObject.getIndex(),
                    action,
                    getNPCLocation(customNPCObject).getX(),
                    getNPCLocation(customNPCObject).getY(),
                    true);
        }

        if (config.oneClickType()==oneClickCustomTypes.Attack)
        {
            ArrayList<NPC> npcs = new NPCQuery()
                    .idEquals(getConfigIds())
                    .result(client)
                    .list;
            NPC nearestAliveNPC = null;

            for (NPC npc : npcs)
            {
                if (npc.getHealthRatio()==0)
                {
                    continue;
                }
                if (nearestAliveNPC==null)
                {
                    nearestAliveNPC=npc;
                }

                if (client.getLocalPlayer().getWorldLocation().distanceTo(npc.getWorldLocation())<client.getLocalPlayer().getWorldLocation().distanceTo(nearestAliveNPC.getWorldLocation()))
                {
                    nearestAliveNPC = npc;
                }
            }

            return createMenuEntry(
                    nearestAliveNPC.getIndex(),
                    MenuAction.NPC_SECOND_OPTION,
                    getNPCLocation(nearestAliveNPC).getX(),
                    getNPCLocation(nearestAliveNPC).getY(),
                    true);
        }

        if(config.oneClickType()==oneClickCustomTypes.Pickpocket)
        {
            return createMenuEntry(
                    customNPCObject.getIndex(),
                    MenuAction.NPC_THIRD_OPTION,
                    getNPCLocation(customNPCObject).getX(),
                    getNPCLocation(customNPCObject).getY(),
                    true);
        }
        return null;
    }

    private Point getLocation(TileObject tileObject)
    {
        if (tileObject instanceof GameObject)
        {

            return ((GameObject) tileObject).getSceneMinLocation();
        }
        else
        {
            return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
        }
    }

    private Point getNPCLocation(NPC npc)
    {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }

    private NPC getNpc(List<Integer> ids)
    {
        return new NPCQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private NPC getNpc(int... id)
    {
        return new NPCQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private GameObject getGameObject(List<Integer> ids)
    {
        return new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    private GameObject getGameObject(int id)
    {
        return new GameObjectQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private boolean bankVisible(){
        if (config.bankType() == oneClickCustomBankTypes.Booth || config.bankType() == oneClickCustomBankTypes.Chest)
        {
            return getGameObject(config.bankID())!=null;
        }
        return getNpc(config.bankID())!=null;
    }

    private TileItem getNearestTileItem(List<TileItem> tileItems)
    {
        int currentDistance;
        if (tileItems.size()==0 || tileItems.get(0) == null)
        {
            return null;
        }
        TileItem closestTileItem = tileItems.get(0);
        int closestDistance = closestTileItem.getTile().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation());
        for (TileItem tileItem : tileItems)
        {
            currentDistance = tileItem.getTile().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation());
            if (currentDistance < closestDistance)
            {
                closestTileItem = tileItem;
                closestDistance = currentDistance;
            }
        }
        return closestTileItem;
    }

    private Widget getItemByInventoryIndex(int index) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return Arrays.stream(inventoryWidget.getDynamicChildren())
                    .filter(item -> item.getIndex() == index)
                    .findAny().orElse(null);
        }
        return null;
    }

    private void setSelectedInventoryItem(Widget item) {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(item.getIndex());
        client.setSelectedSpellItemId(item.getItemId());
    }

    public int getEmptySlots() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());

        if (inventory!=null && !inventory.isHidden()
                && inventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        if (bankInventory!=null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }
        return -1;
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private boolean depositBoxOpen() {
        return client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER) != null;
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
                if (!matchFound) continue;

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
}
package net.runelite.client.plugins.oneclickcustom;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import org.pf4j.Extension;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
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
            if (item.getTile() == event.getTile()) //Don't add if tile already exists, prevents doubling when crossing loading lines
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
        if (event.getType()!= MenuAction.EXAMINE_ITEM.getId())
        {
            return;
        }

        if (getItemOnNPCsHashMap().containsKey(event.getIdentifier()) || getItemOnGameObjectsHashMap().containsKey(event.getIdentifier()))
        {
            client.insertMenuItem(
                    "One Click Custom",
                    "",
                    MenuAction.UNKNOWN.getId(),
                    event.getIdentifier(),
                    event.getActionParam0(),
                    event.getActionParam1(),
                    true);
        }
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
    private void onClientTick(ClientTick event)
    {
        if (config.oneClickType()==oneClickCustomTypes.Use_Item_On_X)
        {
            return;
        }

        if (config.oneClickType()==oneClickCustomTypes.Gather && getGameObject()==null)
        {
            //System.out.println("oneclick set to gather, gameobject is null.");
            return;
        }

        if ((GroundItems.size()==0 || getNearestTileItem(GroundItems)==null) && config.oneClickType() == oneClickCustomTypes.Pick_Up)
        {
            //System.out.println("ground item check null");
            return;
        }

        if (getNpcObject()==null &!(config.oneClickType()==oneClickCustomTypes.Gather) &! (config.oneClickType() == oneClickCustomTypes.Pick_Up))
        {
            //System.out.println("npcobject check null");
            return;
        }

        if (getInventQuantity(client)==28 && config.InventoryFull() && config.oneClickType()!=oneClickCustomTypes.Attack)
        {
            //System.out.println("full invent");
            return;
        }


        if(client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;
        String text;
        {
            text =  "<col=00ff00>One Click Custom";
        }

        client.insertMenuItem(
                text,
                "",
                MenuAction.UNKNOWN.getId(),
                0,
                0,
                0,
                true);
    }

    private void handleClick(MenuOptionClicked event)
    {
        if (getInventQuantity(client)==28 && config.InventoryFull())
        {
            return;
        }
        event.setMenuEntry(setCustomMenuEntry());
    }

    private void handleInventoryItemClicked(MenuOptionClicked event) { //hella copy paste code fix this dumb shit. Maybe rework whole plugin tbh kinda bodged.
        client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedItemSlot(getInventoryItem(event.getId()).getIndex());
        client.setSelectedItemID(event.getId());

        if (getItemOnNPCsHashMap().get(event.getId())!=null)
        {
            NPC nearestnpc = new NPCQuery()
                    .idEquals(getItemOnNPCsHashMap().get(event.getId()))
                    .result(client)
                    .nearestTo(client.getLocalPlayer());

            if (nearestnpc!=null)
            {
                event.setMenuEntry(createMenuEntry(nearestnpc.getIndex(),MenuAction.ITEM_USE_ON_NPC, getNPCLocation(nearestnpc).getX(), getNPCLocation(nearestnpc).getY(), false));
                return;
            }
        }

        if (getItemOnGameObjectsHashMap().get(event.getId())!=null)
        {
            GameObject nearestGameObject = new GameObjectQuery()
                    .idEquals(getItemOnGameObjectsHashMap().get(event.getId()))
                    .result(client)
                    .nearestTo(client.getLocalPlayer());

            if (nearestGameObject!=null)
            {
                event.setMenuEntry(createMenuEntry(nearestGameObject.getId(),MenuAction.ITEM_USE_ON_GAME_OBJECT, getLocation(nearestGameObject).getX(), getLocation(nearestGameObject).getY(), false));
            }
        }
    }

    private MenuEntry setCustomMenuEntry()
    {
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
            //System.out.println("Should be returning Gather MES");
            GameObject customGameObject = getGameObject();
            return createMenuEntry(
                    customGameObject.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION,
                    getLocation(customGameObject).getX(),
                    getLocation(customGameObject).getY(),
                    true);
        }

        NPC customNPCObject = getNpcObject();

        if(config.oneClickType()==oneClickCustomTypes.Fish)
        {
            //System.out.println("Should be returning Fish MES");
            return createMenuEntry(
                    customNPCObject.getIndex(),
                    MenuAction.NPC_FIRST_OPTION,
                    getNPCLocation(customNPCObject).getX(),
                    getNPCLocation(customNPCObject).getY(),
                    true);
        }

        if (config.oneClickType()==oneClickCustomTypes.Attack)
        {
            //System.out.println("Should be returning Attack MES");

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
            //System.out.println("Should be returning Pickpocket MES");
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

    private NPC getNpcObject()
    {
        return new NPCQuery()
                .idEquals(getConfigIds())
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private GameObject getGameObject()
    {
        return new GameObjectQuery()
                .idEquals(getConfigIds())
                .result(client)
                .nearestTo(client.getLocalPlayer());
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

    @Nullable
    public static Collection<WidgetItem> getInventoryItems(Client client) {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

        if (inventory == null) {
            return null;
        }

        return new ArrayList<>(inventory.getWidgetItems());
    }


    public static int getInventQuantity(Client client) {
        Collection<WidgetItem> inventoryItems = getInventoryItems(client);

        if (inventoryItems == null) {
            return 0;
        }

        int count = 0;

        for (WidgetItem inventoryItem : inventoryItems) {
            if (!(String.valueOf(inventoryItem).contains("id=-1"))){
                count += 1;
            }
        }
        return count;
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
        if (config.itemOnGameObjectString().trim().isEmpty())
        {
            return null;
        }
        return parseConfigString(config.itemOnGameObjectString());
    }

    private HashMap<Integer, List<Integer>> getItemOnNPCsHashMap()
    {
        if (config.itemOnNpcString().trim().isEmpty())
        {
            return null;
        }
        return parseConfigString(config.itemOnNpcString());
    }

    private HashMap<Integer, List<Integer>> parseConfigString(String ConfigString)
    {
        HashMap<Integer, List<Integer>> IDs = new HashMap<>();

        for (String line : ConfigString.trim().split("\n"))
        {
            List<Integer> lineIDs = new ArrayList<>();
            for(String id : line.split(",")) {
                lineIDs.add(Integer.parseInt(id));
            }
            Integer key = lineIDs.get(0);
            List<Integer> values = lineIDs.subList(1, lineIDs.size());
            IDs.put(key,values);
        }
        return IDs;
    }
}

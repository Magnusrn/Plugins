package net.runelite.client.plugins.oneclickcustom;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.GameEventManager;
import org.pf4j.Extension;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

@Extension
@PluginDescriptor(
        name = "One Click Custom",
        description = "Sets test testthe Menu entry for left click anywhere",
        tags = {"one click","custom"},
        enabledByDefault = false
)
@Slf4j
public class oneClickCustomPlugin extends Plugin{

    List<TileItem> GroundItems = new ArrayList<>();

    @Inject
    private Client client;

    @Inject
    GameEventManager gameEventManager;

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
    public void onConfigChanged(ConfigChanged event)
    {
        if(event.getGroup().equals("oneclickcustom"))
        {
            gameEventManager.simulateGameEvents(this);
        }
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
        if (event.getItem().getId()==config.ID())
        {
            GroundItems.add(event.getItem());
        }
    }

    @Subscribe
    private void onItemDespawned(ItemDespawned event)
    {
        if (event.getItem().getId()==config.ID())
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
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if(event.getMenuOption().equals("<col=00ff00>One Click Custom"))
        {
            if((client.getLocalPlayer().getAnimation()!=-1|| client.getLocalPlayer().isMoving()) && config.consumeClick())
            {
                event.consume();
            }
            handleClick(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event)
    {
        if (config.oneClickType()==oneClickCustomTypes.Gather && getNearestGameObjectByPath()==null)
        {
            //System.out.println("oneclick set to gather, gameobject is null.");
            return;
        }

        if ((GroundItems.size()==0 || getNearestTileItemByPath(GroundItems)==null) && config.oneClickType() == oneClickCustomTypes.Pick_Up)
        {
            //System.out.println("ground item check null");
            return;
        }

        if (getNearestNPCByPath()==null &!(config.oneClickType()==oneClickCustomTypes.Gather) &! (config.oneClickType() == oneClickCustomTypes.Pick_Up))
        {
            //System.out.println("npcobject check null");
            return;
        }

        if (getInventQuantity(client)==28 && config.InventoryFull())
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

    private MenuEntry setCustomMenuEntry()
    {

        if (config.oneClickType()==oneClickCustomTypes.Pick_Up)
        {
            if (!GroundItems.isEmpty()) {
                TileItem tileItem = getNearestTileItemByPath(GroundItems);
                return createMenuEntry(
                        config.ID(),
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
            GameObject customGameObject = getNearestGameObjectByPath();

            return createMenuEntry(
                    customGameObject.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION,
                    getLocation(customGameObject).getX(),
                    getLocation(customGameObject).getY(),
                    true);
        }

        NPC customNPCObject = getNearestNPCByPath();

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
            return createMenuEntry(
                    customNPCObject.getIndex(),
                    MenuAction.NPC_SECOND_OPTION,
                    getNPCLocation(customNPCObject).getX(),
                    getNPCLocation(customNPCObject).getY(),
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

    private NPC getNearestNPCByPath()
    {
        List<NPC> NPCs = new NPCQuery()
                .idEquals(config.ID())
                .result(client)
                .list;
        if (NPCs.size()==0 || NPCs.get(0) == null)
        {
            return null;
        }

        NPC closestNPC = null;
        for (NPC npc: NPCs) //determines if NPC is blocked(if final path tile is not within 1 tile of NPC)
        {
            if (getPathWorldPoints(npc)==null)
            {
                return null;
            }

            if (getPathWorldPoints(npc).get(getPathWorldPoints(npc).size()-1).distanceTo(npc.getWorldArea())<=1){ //.get returns list item by index
                closestNPC = npc;
                break;
            }
        }
        if (closestNPC==null) //path is blocked
        {
            return null;
        }

        for (NPC npc: NPCs)
        {
            if(getPathDistance(getPathWorldPoints(npc))<getPathDistance(getPathWorldPoints(closestNPC)))
            {
                closestNPC = npc;
            }
        }
        return closestNPC;
    }

    private GameObject getNearestGameObjectByPath()
    {
        List<GameObject> gameObjects = new GameObjectQuery()
                .idEquals(config.ID())
                .result(client)
                .list;
        if (gameObjects.size()==0 || gameObjects.get(0) == null)
        {
            return null;
        }

        GameObject closestGameObject = gameObjects.get(0);
        for (GameObject gameObject: gameObjects)
        {
            if (getPathWorldPoints(gameObject)==null
            || getPathWorldPoints(closestGameObject)==null)
            {
                continue;
            }
            if(getPathDistance(getPathWorldPoints(gameObject))<getPathDistance(getPathWorldPoints(closestGameObject)))
            {
                closestGameObject = gameObject;
            }
        }
        System.out.println(getPathDistance(getPathWorldPoints(closestGameObject)));
        return closestGameObject;
    }

    private TileItem getNearestTileItemByPath(List<TileItem> tileItems)
    {
        if (tileItems.size()==0 || tileItems.get(0) == null)
        {
            return null;
        }

        TileItem closestTileItem = null;
        for (TileItem tileitem: tileItems) //determines if TileItem is blocked(if final path tile is not same tile as TileItem)
        {
            if (getPathTileItems(tileitem)==null)
            {
                return null;
            }

            if (getPathTileItems(tileitem).get(getPathTileItems(tileitem).size()-1).distanceTo(tileitem.getTile().getWorldLocation())==0){ //.get returns list item by index
                closestTileItem = tileitem;
                break;
            }
        }
        if (closestTileItem==null) //path is blocked
        {
            return null;
        }

        for (TileItem tileItem: tileItems)
        {
            if(getPathDistance(getPathTileItems(tileItem))<getPathDistance(getPathTileItems(closestTileItem)))
            {
                closestTileItem = tileItem;
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

    private List<WorldPoint> getPathWorldPoints(TileObject object){
        return client.getLocalPlayer().getWorldLocation().pathTo(client,object.getWorldLocation());
    }
    private List<WorldPoint> getPathTileItems(TileItem tileItem){
        return client.getLocalPlayer().getWorldLocation().pathTo(client,tileItem.getTile().getWorldLocation());
    }
    private List<WorldPoint> getPathWorldPoints(NPC npc){
        return client.getLocalPlayer().getWorldLocation().pathTo(client,npc.getWorldLocation());
    }

    private int getPathDistance(List<WorldPoint> worldPoints) {
        if (worldPoints.size() == 0)
        {
            return 0;
        }

        if (worldPoints.size() == 1)
        {
            return client.getLocalPlayer().getWorldLocation().distanceTo(worldPoints.get(0));
        }

        int pathLength = 0;
        int previousX = worldPoints.get(0).getX();
        int previousY = worldPoints.get(0).getY();
        for (WorldPoint worldPoint : worldPoints)
        {
            double distance = Math.hypot(Math.abs(worldPoint.getX() - previousX), Math.abs(worldPoint.getY() - previousY));
            pathLength += distance;
            previousX = worldPoint.getX();
            previousY = worldPoint.getY();
        }
        return pathLength;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

}

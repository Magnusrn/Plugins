package net.runelite.client.plugins.oneclickcustom;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
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
        description = "Sets the Menu entry for left click anywhere",
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
        if (config.oneClickType()==oneClickCustomTypes.Gather && checkforGameObject()==null)
        {
            //System.out.println("oneclick set to gather, gameobject is null.");
            return;
        }

        if ((GroundItems.size()==0 || getNearestTileItem(GroundItems)==null) && config.oneClickType() == oneClickCustomTypes.Pick_Up)
        {
            //System.out.println("ground item check null");
            return;
        }

        if (checkForNPCObject()==null &!(config.oneClickType()==oneClickCustomTypes.Gather) &! (config.oneClickType() == oneClickCustomTypes.Pick_Up))
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
                TileItem tileItem = getNearestTileItem(GroundItems);
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
            GameObject customGameObject = checkforGameObject();
            return createMenuEntry(
                    customGameObject.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION,
                    getLocation(customGameObject).getX(),
                    getLocation(customGameObject).getY(),
                    true);
        }

        NPC customNPCObject = checkForNPCObject();

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

    private NPC checkForNPCObject()
    {
        return new NPCQuery()
                .idEquals(config.ID())
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private GameObject checkforGameObject()
    {
        return new GameObjectQuery()
                .idEquals(config.ID())
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

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}

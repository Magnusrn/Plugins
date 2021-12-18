package net.runelite.client.plugins.oneclicktelegrab;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.GroundObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import java.util.ArrayList;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "One Click Telegrab(Wines)",
        description = "Left click Telekinetic Grab for Wines of Zamorak in the wilderness",
        tags = {"one", "click", "oneclick", "telegrab", "wine"}
)
public class OneClickTelegrabPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OneClickTelegrabConfig config;

    @Provides
    OneClickTelegrabConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickTelegrabConfig.class);
    }

    private final int WINE_OF_ZAMORAK_WILDERNESS_ID = 23489;
    private final int WINE_OF_ZAMORAK_FALADOR_ID = 245;
    private final int MYSTERIOUS_POOL_ID = 30395;
    List<TileItem> GroundItems = new ArrayList<>();


    @Subscribe
    private void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
            return;
        if (!config.TrueOneClick())
        {
            return;
        }
        String text = "<col=00ff00>One Click Telegrab";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (event.getTarget().contains("Wine of zamorak") &&
            event.getOption().contains("Take"))
        {
            event.setOption("Telegrab");
            event.setModified();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (config.ConsumeClicks() && getNearestTileItem(GroundItems)==null)
        {
            event.consume();
        }
        if (event.getMenuOption().contains("Telegrab") && event.getMenuTarget().contains("Wine of zamorak"))
        {
            handleClick(event);
        }
        else if (config.TrueOneClick())
        {
            handleClick(event);
        }
    }

    private void handleClick(MenuOptionClicked event) {
        final Widget widget = client.getWidget(WidgetInfo.SPELL_TELEKINETIC_GRAB);
        client.setSelectedSpellName("<col=00ff00>" + "Telekinetic Grab" + "</col>");
        client.setSelectedSpellWidget(widget.getId());
        client.setSelectedSpellChildIndex(-1);
        if (telegrabMES()!=null)
        {
            event.setMenuEntry(telegrabMES());
        }
    }

    private MenuEntry telegrabMES() {
        if (!GroundItems.isEmpty()) {
            TileItem tileItem = getNearestTileItem(GroundItems);
            return createMenuEntry(
                    getWineID(),
                    MenuAction.SPELL_CAST_ON_GROUND_ITEM,
                    tileItem.getTile().getSceneLocation().getX(),
                    tileItem.getTile().getSceneLocation().getY(),
                    false);
        }
        return null;
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
        if (event.getItem().getId()==getWineID())
        {
            GroundItems.add(event.getItem());
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        GroundItems.clear();
    }

    @Override
    protected void startUp() throws Exception
    {
        GroundItems.clear();
    }

    @Subscribe
    private void onItemDespawned(ItemDespawned event)
    {
        if (event.getItem().getId()==getWineID())
        {
            GroundItems.remove(event.getItem());
        }
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

    private int getWineID() { //returns true if the mysterious pool(well) beside the wilderness wine spot is visible
        GameObject MYSTERIOUS_POOL = new GameObjectQuery()
                .idEquals(MYSTERIOUS_POOL_ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        if (MYSTERIOUS_POOL!=null)
        {
            return WINE_OF_ZAMORAK_WILDERNESS_ID;
        }
        return WINE_OF_ZAMORAK_FALADOR_ID;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}
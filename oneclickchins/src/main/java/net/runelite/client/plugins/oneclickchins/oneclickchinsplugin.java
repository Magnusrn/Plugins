package net.runelite.client.plugins.oneclickchins;

import javax.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.ArrayList;

@Extension
@PluginDescriptor(
        name = "One Click Chinchompas",
        enabledByDefault = false,
        description = "one click chinchompas. Requires you to lay the traps initially as it only resets. Probably too slow to do 6 traps. Credit TP.")
@Slf4j
public class oneclickchinsplugin extends Plugin{

    private static final int BOX_TRAP_EXPIRED = 10008;
    private static final int GREY_CHINCHOMPA_CAUGHT = 9382;
    private static final int RED_CHINCHOMPA_CAUGHT = 9383;
    private static final int BLACK_CHINCHOMPA_CAUGHT = 721;
    private static final int CHINCHOMPA_FAILED = 9385;
    private int timeout;
    HunterState hunterState = HunterState.RESET_TRAP;

    @Inject
    private Client client;

    @Inject
    private oneclickchinsconfig config;

    @Provides
    oneclickchinsconfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(oneclickchinsconfig.class);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Chinchompas"))
            handleClick(event);
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        if (timeout>0){
            timeout--;
        }
    }

    enum HunterState
    {
        RESET_FAILED_TRAP,
        RESET_EXPIRED_TRAP,
        RESET_TRAP
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        if (ResetExpiredTrapMenuEntry()!=null)
        {
            hunterState = HunterState.RESET_EXPIRED_TRAP;
        }

        else if (getTrapMenuEntry(getCaughtChinchompaType())!=null)
        {
            hunterState = HunterState.RESET_TRAP;
        }

        else if (getTrapMenuEntry(CHINCHOMPA_FAILED)!=null)
        {
            hunterState = HunterState.RESET_FAILED_TRAP;

        }
        String text = "<col=00ff00>One Click Chinchompas";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event)
    {
        int TRAP_RESET_ANIMATION = 5212;
        if (client.getLocalPlayer()==null) return;
        if (client.getLocalPlayer().getAnimation()== TRAP_RESET_ANIMATION)
        {
            timeout=9;
        }
    }

    private void handleClick(MenuOptionClicked event) {
        System.out.println(timeout);

        if (client.getLocalPlayer().isMoving()
                || client.getLocalPlayer().getPoseAnimation() != client.getLocalPlayer().getIdlePoseAnimation()
                || client.getLocalPlayer().getAnimation() == 5208
                || client.getLocalPlayer().getAnimation() == 5212
                || timeout>0)
        {
            event.consume();
        }

        if (client.getPlayers().size() > 1 && config.playerspotted())
        {
            event.consume();
        }
        switch (hunterState)
        {
            case RESET_FAILED_TRAP:
                if (getTrapMenuEntry(CHINCHOMPA_FAILED)!=null)
                {
                    event.setMenuEntry(getTrapMenuEntry(CHINCHOMPA_FAILED));
                }
                else
                {
                    event.consume();
                }
                break;
            case RESET_EXPIRED_TRAP:
                if(ResetExpiredTrapMenuEntry()!=null)
                {
                    event.setMenuEntry(ResetExpiredTrapMenuEntry());
                }
                else
                {
                    event.consume();
                }
                break;
            case RESET_TRAP:
                if (getTrapMenuEntry(getCaughtChinchompaType())!=null)
                {
                    event.setMenuEntry(getTrapMenuEntry(getCaughtChinchompaType()));
                }
                else
                {
                    event.consume();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + hunterState);
        }
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject instanceof GameObject)
            return ((GameObject) tileObject).getSceneMinLocation();
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    public TileItem getGroundItem(int id)
    {
        Tile[][][] tiles = client.getScene().getTiles();

        int z = client.getPlane();

        for (int x = 0; x < Constants.SCENE_SIZE; ++x)
        {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y)
            {
                Tile tile = tiles[z][x][y];

                if (tile == null || client.getLocalPlayer() == null)
                {
                    continue;
                }

                if (client.getLocalPlayer().getWorldLocation().distanceTo(tile.getWorldLocation())>config.withinXtiles())
                {
                    continue;
                }

                if (tile.getItemLayer() != null)
                {
                    TileItem tileItem = (TileItem) tile.getItemLayer().getBottom();
                    if (tileItem.getId() == id)
                    {
                        return tileItem;
                    }
                }
            }
        }
        return null;
    }


    private MenuEntry getTrapMenuEntry(int ID) {
        ArrayList<GameObject> gameObjects = new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .list;

        GameObject closestGameObject = null;

        for (GameObject gameobject: gameObjects)
        {
            if (closestGameObject==null)
            {
                if (client.getLocalPlayer().getWorldLocation().distanceTo(gameobject.getWorldLocation()) <= config.withinXtiles())
                {
                    closestGameObject = gameobject;
                }
            }
            else if (client.getLocalPlayer().getWorldLocation().distanceTo(gameobject.getWorldLocation())<
                    client.getLocalPlayer().getWorldLocation().distanceTo(closestGameObject.getWorldLocation()))
            {
                closestGameObject = gameobject;
            }
        }

        if (closestGameObject == null) return null;

        return createMenuEntry(
                ID,
                MenuAction.GAME_OBJECT_SECOND_OPTION,
                getLocation(closestGameObject).getX(),
                getLocation(closestGameObject).getY(),
                true);
    }

    private MenuEntry ResetExpiredTrapMenuEntry() {
        TileItem object = getGroundItem(BOX_TRAP_EXPIRED);

        if (object == null) return null;
        return createMenuEntry(
                BOX_TRAP_EXPIRED,
                MenuAction.GROUND_ITEM_FOURTH_OPTION,
                object.getTile().getLocalLocation().getSceneX(),
                object.getTile().getLocalLocation().getSceneY(),
                true);
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private int getCaughtChinchompaType(){
        if (config.chinchompaType()==ChinchompaType.Grey){
            return GREY_CHINCHOMPA_CAUGHT;
        }
        if (config.chinchompaType()==ChinchompaType.Red){
            return RED_CHINCHOMPA_CAUGHT;
        }
        return BLACK_CHINCHOMPA_CAUGHT;
    }
}
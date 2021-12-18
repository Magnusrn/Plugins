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

@Extension
@PluginDescriptor(
        name = "One Click Chinchompas",
        enabledByDefault = false,
        description = "one click chinchompas. Requires you to lay the traps initially as it only resets")
@Slf4j
public class oneclickchinsplugin extends Plugin{


    public static final int BOX_TRAP_EXPIRED = 10008;
    private static final int CHINCHOMPA_CAUGHT = 9383;
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

        if (ExpiredTrapExists())
        {
            hunterState = HunterState.RESET_EXPIRED_TRAP;
        }

        else if (CaughtTrapExists())
        {
            hunterState = HunterState.RESET_TRAP;

        }

        else if (FailedTrapExists())
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
        if (client.getLocalPlayer().getAnimation()== TRAP_RESET_ANIMATION)
        {
            timeout=9;
        }
    }

    private void handleClick(MenuOptionClicked event) {

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
                event.setMenuEntry(ResetFailedTrap());
                break;
            case RESET_EXPIRED_TRAP:
                event.setMenuEntry(ResetExpiredTrap());
                break;
            case RESET_TRAP:
                event.setMenuEntry(ResetTrap());
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
        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();

        int z = client.getPlane();

        for (int x = 0; x < Constants.SCENE_SIZE; ++x)
        {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y)
            {
                Tile tile = tiles[z][x][y];

                if (tile == null)
                {
                    continue;
                }
                Player player = client.getLocalPlayer();
                if (player == null)
                {
                    continue;
                }
                TileItem tileItem = findItemAtTile(tile, id);
                if (tileItem != null)
                {
                    return tileItem;
                }
            }
        }
        return null;
    }

    private TileItem findItemAtTile(Tile tile, int id)
    {
        ItemLayer tileItemPile = tile.getItemLayer();
        if (tileItemPile != null)
        {
            TileItem tileItem = (TileItem) tileItemPile.getBottom();
            if (tileItem.getId() == id)
            {
                return tileItem;
            }
        }
        return null;
    }

    private MenuEntry ResetTrap(){
        GameObject gameobject = new GameObjectQuery()
                .idEquals(CHINCHOMPA_CAUGHT)
                .result(client)
                .nearestTo(client.getLocalPlayer());

        return createMenuEntry(
                CHINCHOMPA_CAUGHT,
                MenuAction.GAME_OBJECT_SECOND_OPTION,
                getLocation(gameobject).getX(),
                getLocation(gameobject).getY(),
                true);
    }

    private MenuEntry ResetFailedTrap() {
        GameObject gameobject = new GameObjectQuery()
                .idEquals(CHINCHOMPA_FAILED)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        return createMenuEntry(
                CHINCHOMPA_FAILED,
                MenuAction.GAME_OBJECT_SECOND_OPTION,
                getLocation(gameobject).getX(),
                getLocation(gameobject).getY(),
                true);
    }

    private MenuEntry ResetExpiredTrap() {
        TileItem object = getGroundItem(BOX_TRAP_EXPIRED);
        return createMenuEntry(
                BOX_TRAP_EXPIRED,
                MenuAction.GROUND_ITEM_FOURTH_OPTION,
                object.getTile().getLocalLocation().getSceneX(),
                object.getTile().getLocalLocation().getSceneY(),
                true);
    }

    private boolean CaughtTrapExists(){
        GameObject gameobject = new GameObjectQuery()
                .idEquals(CHINCHOMPA_CAUGHT)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        return gameobject != null;
    }

    private boolean FailedTrapExists(){
        GameObject gameobject = new GameObjectQuery()
                .idEquals(CHINCHOMPA_FAILED)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        return gameobject != null;
    }

    private boolean ExpiredTrapExists()
    {
        TileItem object = getGroundItem(BOX_TRAP_EXPIRED);
        return object != null;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}
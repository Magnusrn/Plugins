package net.runelite.client.plugins.nexredclick;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
        name = "Nex Red Click ",
        enabledByDefault = false,
        description = "Remaps ctrl click on Nex to click door outside instance")
@Slf4j
public class NexRedClickPlugin extends Plugin{

    @Inject
    private Client client;

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (!client.isKeyPressed(KeyCode.KC_CONTROL))
        {
            return;
        }
        if (event.getMenuTarget().contains("Nex"))
        {
            event.setMenuEntry(DoorMenuEntry());
        }
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject instanceof GameObject)
            return ((GameObject) tileObject).getSceneMinLocation();
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    private MenuEntry DoorMenuEntry(){
        return createMenuEntry(
                42934,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(42934)).getX(),
                getLocation(getGameObject(42934)).getY(),
                false);
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

}
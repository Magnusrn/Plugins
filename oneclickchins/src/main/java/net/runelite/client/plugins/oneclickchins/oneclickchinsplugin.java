/*
 * Copyright (c) 2017, Robin Weymans <Robin.weymans@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.oneclickchins;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Extension
@PluginDescriptor(
        name = "One Click Chinchompas",
        enabledByDefault = false,
        description = "one click chinchompas. Requires you to lay the traps initially(WHILE PLUGIN IS ON) as it only resets.")
@Slf4j
public class oneclickchinsplugin extends Plugin{
    private final Map<WorldPoint, BoxTrap> traps = new HashMap<>();
    private final Map<WorldPoint, BoxTrap> fallenTraps = new HashMap<>();
    private int timeout;
    @Inject
    private Client client;

    @Inject
    private oneclickchinsconfig config;

    @Provides
    oneclickchinsconfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(oneclickchinsconfig.class);
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        final GameObject gameObject = event.getGameObject();
        final WorldPoint trapLocation = gameObject.getWorldLocation();
        final BoxTrap myTrap = traps.get(trapLocation);
        switch (gameObject.getId())
        {
            case ObjectID.BOX_TRAP_9380: // Box trap placed
                // If the player is on that tile, assume he is the one that placed the trap
                // Note that a player can move and set up a trap in the same tick, and this
                // event runs after the player movement has been updated, so we need to
                // compare to the trap location to the last location of the player.
                if (trapLocation.distanceTo(client.getLocalPlayer().getWorldLocation()) == 0)
                {
                    traps.put(trapLocation, new BoxTrap(gameObject));
                    timeout = 0;
                }
                break;

            case ObjectID.SHAKING_BOX: // Black chinchompa caught
            case ObjectID.SHAKING_BOX_9382: // Grey chinchompa caught
            case ObjectID.SHAKING_BOX_9383: // Red chinchompa caught

                if (myTrap != null)
                {
                    myTrap.setState(BoxTrap.State.FULL);
                    myTrap.setGameObject(event.getGameObject());
                    myTrap.resetTimer();
                }

            case ObjectID.BOX_TRAP_9385: //Empty box trap
                if (myTrap != null)
                {
                    myTrap.setState(BoxTrap.State.EMPTY);
                    myTrap.setGameObject(event.getGameObject());
                    myTrap.resetTimer();
                }
        }
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned event) {
        //if item spawn location matches where a lain trap was
        if (traps.values().stream().anyMatch(boxTrap -> boxTrap.getGameObject().getWorldLocation().equals(event.getTile().getWorldLocation())))
        {
            traps.remove(event.getItem().getTile().getWorldLocation());
            fallenTraps.put(event.getTile().getWorldLocation(), new BoxTrap(event));
        }
    }

    @Subscribe
    public void onItemDespawned(ItemDespawned event) {
        if (fallenTraps.values().stream().anyMatch(fallenBoxTrap -> fallenBoxTrap.getTileItem().equals(event.getItem())))
        {
            fallenTraps.remove(event.getItem().getTile().getWorldLocation());
        }
    }

    @Override
    protected void startUp() {

    }

    @Override
    protected void shutDown() throws Exception {
        traps.clear();
        fallenTraps.clear();
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (timeout>0){
            timeout--;
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;

        String text = "<col=00ff00>One Click Chinchompas";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Chinchompas"))
            handleClick(event);
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        int TRAP_RESET_ANIMATION = 5212;
        if (client.getLocalPlayer()==null) return;
        if (client.getLocalPlayer().getAnimation()== TRAP_RESET_ANIMATION)
        {
            timeout=9;
        }
    }

    private void handleClick(MenuOptionClicked event) {
        if (timeout > 0 ) return;

        if (client.getPlayers().size() > 1 && config.playerspotted())
        {
            event.consume();
            return;
        }
        //i think pick up fallen ones IF there's less than 30s(.5) after they fall, gives some wiggle room.
        if (fallenTraps.values().stream().anyMatch(boxTrap -> boxTrap.getTrapTimeRemaining()<30)
            ||resetTrapMenuEntry()==null && resetExpiredTrapMenuEntry()!=null)
        {
            event.setMenuEntry(resetExpiredTrapMenuEntry());
            return;
        }
        if (resetTrapMenuEntry()!=null)
        {
            event.setMenuEntry(resetTrapMenuEntry());
        }
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject instanceof GameObject)
            return ((GameObject) tileObject).getSceneMinLocation();
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }
    private MenuEntry resetTrapMenuEntry() {
        GameObject trap = getOptimalTrap(traps);
        if (trap == null) return null;

        return createMenuEntry(
                trap.getId(),
                MenuAction.GAME_OBJECT_SECOND_OPTION,
                getLocation(trap).getX(),
                getLocation(trap).getY(),
                true);
    }

    private MenuEntry resetExpiredTrapMenuEntry() {
        TileItem object = getOptimalFallenTrap(fallenTraps);

        if (object == null) return null;
        return createMenuEntry(
                object.getId(),
                MenuAction.GROUND_ITEM_FOURTH_OPTION,
                object.getTile().getLocalLocation().getSceneX(),
                object.getTile().getLocalLocation().getSceneY(),
                true);
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private GameObject getOptimalTrap(Map<WorldPoint, BoxTrap> boxTraps) {
        BoxTrap boxtrap;
        /*need to work on optimizing this, may be different depending on number of traps
        must never let a trap despawn or be visible to other players
        must never let a caught chin fall over
        full has high priority unless groundItem will despawn*/
        boxtrap  = boxTraps.values().stream().filter(boxTrap -> boxTrap.getState().equals(BoxTrap.State.FULL)).
                min(Comparator.comparing(BoxTrap::getTrapTimeRemaining)).orElse(null);
        if (boxtrap == null)
        {//failed(empty) has lowest priority
            boxtrap = boxTraps.values().stream().filter(boxTrap -> boxTrap.getState().equals(BoxTrap.State.EMPTY)).
                    min(Comparator.comparing(BoxTrap::getTrapTimeRemaining)).orElse(null);
        }
        return boxtrap != null ? boxtrap.getGameObject() : null;
    }

    private TileItem getOptimalFallenTrap(Map<WorldPoint,BoxTrap> fallenBoxTraps) {
        BoxTrap fallenBoxTrap = fallenBoxTraps.values().stream().min(Comparator.comparing(BoxTrap::getTrapTimeRemaining)).orElse(null);
        return fallenBoxTrap != null ? fallenBoxTrap.getTileItem() : null;
    }
}
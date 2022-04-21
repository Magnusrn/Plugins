/*
 * Copyright (c) 2019, jkybtw <https://github.com/jkybtw>
 * Copyright (c) 2019, openosrs <https://openosrs.com>
 * Copyright (c) 2019, kyle <https://github.com/Kyleeld>
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
package net.runelite.client.plugins.oneclicksandstone;

import java.util.*;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.GameEventManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
        name = "One Click Sandstone",
        enabledByDefault = false,
        description = "Mines Sand, deposits into grinder and casts humidify if needed. Hardcoded to consume clicks with rune pickaxe or dragon pickaxe. credit TP")
@Slf4j
public class OneClickSandstonePlugin extends Plugin {

    final static WorldPoint SW = new WorldPoint(3164,2913,0);
    final static WorldPoint NE = new WorldPoint(3168,2916,0);
    final static WorldArea AREA = new WorldArea(SW,NE);
    final static Set<Integer> waterSkins = Set.of(1825,1827,1829,1823);

    @Inject
    private Client client;

    @Inject
    GameEventManager gameEventManager;

    @Inject
    private OneClickSandstoneConfig config;

    @Provides
    OneClickSandstoneConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickSandstoneConfig.class);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Sandstone"))
            handleClick(event);
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
            return;

        String text = "<col=00ff00>One Click Sandstone";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }

    private void handleClick(MenuOptionClicked event) {
        if(client.getLocalPlayer().isMoving()
                ||client.getLocalPlayer().getPoseAnimation()
                != client.getLocalPlayer().getIdlePoseAnimation()
                || client.getLocalPlayer().getAnimation() == AnimationID.MINING_RUNE_PICKAXE
                || client.getLocalPlayer().getAnimation() == AnimationID.MINING_DRAGON_PICKAXE)
        {
            event.consume();
        }
        if (getEmptySlots() == 0)
            event.setMenuEntry(depositGrinderMenuEntry());
        else if(shouldCastHumidify(waterSkins))
            event.setMenuEntry(createHumidifyMenuEntry());
        else {
            event.setMenuEntry(mineSandStone());
        }
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject instanceof GameObject)
            return ((GameObject)tileObject).getSceneMinLocation();
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    private GameObject checkforGameObject()
    {
        if (config.forceMineNorth())
        {
            ArrayList<GameObject> list = new GameObjectQuery()
                    .idEquals(11386)
                    .result(client)
                    .list;
            for (GameObject item:list)
            {
                if (item.getWorldLocation().isInArea(AREA))
                {
                    return (item);
                }
            }
        }
        return new GameObjectQuery()
                .idEquals(11386)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }


    private GameObject checkForDepositGrinder()
    {
        return new GameObjectQuery()
                .idEquals(26199)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private int getEmptySlots() {
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

    private MenuEntry mineSandStone() {
        GameObject customGameObject = checkforGameObject();
        return createMenuEntry(
                11386,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(customGameObject).getX(),
                getLocation(customGameObject).getY(),
                true);
    }

    private MenuEntry createHumidifyMenuEntry() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                WidgetInfo.SPELL_HUMIDIFY.getId(),
                true);
    }

    private MenuEntry depositGrinderMenuEntry() {
        return createMenuEntry(
                26199,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(checkForDepositGrinder()).getX(),
                getLocation(checkForDepositGrinder()).getY(),true);
    }

    private boolean shouldCastHumidify(Collection<Integer> ids) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (ids.contains(item.getId())) {
                    return false;
                }
            }
        }
        return true;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}
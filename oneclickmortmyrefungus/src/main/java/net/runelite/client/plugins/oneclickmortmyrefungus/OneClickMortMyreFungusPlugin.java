package net.runelite.client.plugins.oneclickmortmyrefungus;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "One Click Mort Myre Fungus",
        description = "POH Fairy ring/butler method only.",
        tags = {"one", "click","mort","myre","fungus","mushrooms"},
        enabledByDefault = false
)
public class OneClickMortMyreFungusPlugin extends Plugin {
    @Inject
    private Client client;

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if(client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;
        if(!(isInPOH() || isInSwamp())) return;
        String text;
        {
            text =  "<col=00ff00>One Click Mort Myre Fungus";
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

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Mort Myre Fungus")) {
            handleClick(event);
        }
    }

    private void handleClick(MenuOptionClicked event)
    {
        if (isInSwamp())
        {
            if (getEmptySlots()==0)
            {
                event.setMenuEntry(teleportToHouseMES());
                return;
            }
            if (gatherFungusMES()!=null)
            {
                event.setMenuEntry(gatherFungusMES());
                return;
            }
            if (shouldCastBloom())
            {
                event.setMenuEntry(castBloomMES());
                return;
            }
            walktoCastingTile();
            return;
        }

        if (isInPOH())
        {
            if (getEmptySlots()==0)
            {
                if (client.getWidget(219,1)!=null && client.getWidget(219,1).getChild(1).getText().contains("Take to bank"))
                {
                    event.setMenuEntry(sendToBankMES());
                    return;
                }

                if (client.getWidget(231,5)!=null)
                {
                    event.setMenuEntry(clickContinueMES());
                    return;
                }

                if (client.getWidget(370,19)!=null && client.getWidget(370,19).getChild(3)!=null)
                {
                    event.setMenuEntry(callButlerMES());
                    return;
                }

                if (client.getWidget(116,8)!=null)
                {
                    event.setMenuEntry(houseOptionsMES());
                    return;
                }
            }
            if (client.getBoostedSkillLevel(Skill.PRAYER)<40)
            {
                event.setMenuEntry(drinkFromPoolMES());
                return;
            }
            event.setMenuEntry(useFairyRingMES());
        }
    }

    private void walkTile(WorldPoint worldpoint) {
        int x = worldpoint.getX() - client.getBaseX();
        int y = worldpoint.getY() - client.getBaseY();
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }

    private void walktoCastingTile() {
        WorldPoint worldpoint = new WorldPoint(3472,3419,0);
        walkTile(worldpoint);
    }

    private MenuEntry castBloomMES() {
        WidgetItem sickle = getInventoryItem(2963);
        return createMenuEntry(sickle.getId(), MenuAction.ITEM_THIRD_OPTION, sickle.getIndex(), WidgetInfo.INVENTORY.getId(), false);
    }

    private MenuEntry gatherFungusMES() {
        WorldPoint worldpoint = new WorldPoint(3473,3420,0); //checks if mushrooms exist on NW log as this automatically paths optimally and saves 1t!!!
        GameObject fungiOnLog =new GameObjectQuery()
                .idEquals(3509)
                .result(client)
                .stream()
                .filter(gameObject -> gameObject.getWorldLocation().distanceTo(worldpoint)==0)
                .findAny().orElse(null);

        if (fungiOnLog==null){
            fungiOnLog = getGameObject(3509);
        }

        if (fungiOnLog==null) return null;
        return createMenuEntry(fungiOnLog.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, getLocation(fungiOnLog).getX(), getLocation(fungiOnLog).getY(), false);
    }

    private MenuEntry drinkFromPoolMES() {
        GameObject pool = getGameObject(29241);
        return createMenuEntry(pool.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(pool).getX(),getLocation(pool).getY(), false);
    }

    private MenuEntry useFairyRingMES() {
        GameObject fairyRing = getGameObject(29228);
        return createMenuEntry(fairyRing.getId(), MenuAction.GAME_OBJECT_THIRD_OPTION, getLocation(fairyRing).getX(),getLocation(fairyRing).getY(), false);
    }

    private MenuEntry teleportToHouseMES() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_TELEPORT_TO_HOUSE.getId(), false);
    }

    private MenuEntry houseOptionsMES() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, 7602250, false);
    }

    private MenuEntry callButlerMES() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, 24248339, false);
    }

    private MenuEntry sendToBankMES() {
        return createMenuEntry(0, MenuAction.WIDGET_TYPE_6, 1, WidgetInfo.DIALOG_OPTION_OPTION1.getId(), false);
    }

    private MenuEntry clickContinueMES() {
        return createMenuEntry(0, MenuAction.WIDGET_TYPE_6, -1, 15138821, false);
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
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

    public int getEmptySlots() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryWidget.getWidgetItems().size();
        } else {
            return -1;
        }
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private boolean isInSwamp() {
        return getGameObject(3508)!=null; //checks for rotting log
    }

    private boolean isInPOH() {
        return getGameObject(4525)!=null; //checks for portal, p sure this is same for everyone if not need to do alternative check.
    }

    private boolean shouldCastBloom() {
        //in correct spot, inventory isn't full, no mushrooms gatherable
        return
        (client.getLocalPlayer().getWorldLocation().equals(new WorldPoint(3472, 3419, 0)))
                && getEmptySlots()>0
                && getGameObject(3509)==null;
    }

}

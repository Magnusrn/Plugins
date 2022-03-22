package net.runelite.client.plugins.oneclickbloodsmorytania;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import java.util.Arrays;

@Extension
@PluginDescriptor(
        name = "One Click Bloods Morytania",
        description = "Active One Click Bloods Runecrafting at the new altar. set fairy ring to DLS",
        enabledByDefault = false
)
@Slf4j
public class OneClickBloodsMorytaniaPlugin extends Plugin {

    @Inject
    private Client client;

    @Subscribe
    private void onClientTick(ClientTick event)
    {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) return;
        String text = "<col=00ff00>One Click Bloods Morytania";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        //Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException
    {
        if (event.getMenuOption().equals("<col=00ff00>One Click Bloods Morytania"))
            handleClick(event);
    }
    private void handleClick(MenuOptionClicked event) //billion if statements but unsure of alternative method, can't assign menuentries until visible
    {

        if (isinBloodAltar())
        {
            //empty pouches n shit, then tele to bank
            event.setMenuEntry(craftRunesMES());
            return;
        }

        if (bankOpen())
        {
            return;
        }
        if (isInPOH())
        {
            if (client.getEnergy()<50)
            {
                event.setMenuEntry(drinkFromPoolMES());
                return;
            }
            event.setMenuEntry(useFairyRingMES());
            return;
        }

        if (isInBloodAltarArea())
        {
            event.setMenuEntry(enterAltarMES());
            return;
        }

        if (isInPOH())
        {
            if (client.getEnergy()<50)
            {
                event.setMenuEntry(drinkFromPoolMES());
                return;
            }
            event.setMenuEntry(useFairyRingMES());
            return;
        }
        if (isInMorytaniaHideout1())
        {
            event.setMenuEntry(leaveMorytaniaHideout1MES());
            return;
        }
        if (isInMorytaniaHideout2())
        {
            event.setMenuEntry(leaveMorytaniaHideout2MES());
            return;
        }
        if (isInMorytaniaHideout3())
        {
            event.setMenuEntry(leaveMorytaniaHideout3MES());
            return;
        }
        if (isinMorytaniaHideout4LowAgility())
        {
            event.setMenuEntry(leaveMorytaniaHideout4LowAgilityMES());
            return;
        }
        if (isinMorytaniaHideout5LowAgilityShortcut())
        {
            event.setMenuEntry(useLowAgilityShortcut1MES());
            return;
        }
        if (isinMorytaniaHideout5LowAgilityShortcut2())
        {
            event.setMenuEntry(useLowAgilityShortcut2MES());
            return;
        }

        if (isinMorytaniaHideout5HighAgilityShortcut())
        {
            event.setMenuEntry(UseHighAgilityShortcut2MES());
            return;
        }
    }

    private boolean isInMorytaniaHideout1() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3437,9819,0),new WorldPoint(3454,9830,0)));
    }

    private boolean isInMorytaniaHideout2() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3457,9807,0),new WorldPoint(3475,9825,0)));
    }

    private boolean isInMorytaniaHideout3() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3476,9799,0),new WorldPoint(3507,9840,0)));
    }

    private boolean isinMorytaniaHideout4LowAgility() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3485,9859,0),new WorldPoint(3498,9879,0)));
    }

    private boolean isinMorytaniaHideout5LowAgilityShortcut() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3546,9785,0),new WorldPoint(3572,9812,0)))
                || client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3511,9807,0),new WorldPoint(3538,9832,0)));
    }

    private boolean isinMorytaniaHideout5LowAgilityShortcut2() {
        return false;
    }

    private boolean isinMorytaniaHideout5HighAgilityShortcut() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3532,9764,0),new WorldPoint(3542,9781,0)));
    }

    private boolean isInBloodAltarArea() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3543,9764,0),new WorldPoint(3570,9784,0)));
    }

    private MenuEntry drinkFromPoolMES() {
        GameObject pool = getGameObject(29241);
        return createMenuEntry(pool.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(pool).getX(),getLocation(pool).getY(), false);
    }

    private MenuEntry useFairyRingMES() {
        GameObject fairyRing = getGameObject(29228);
        return createMenuEntry(fairyRing.getId(), MenuAction.GAME_OBJECT_THIRD_OPTION, getLocation(fairyRing).getX(),getLocation(fairyRing).getY(), false);
    }

    private MenuEntry leaveMorytaniaHideout1MES() {
        GameObject tunnel = getGameObject(16308);
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }
    private MenuEntry leaveMorytaniaHideout2MES() {
        GameObject tunnel = getGameObject(5046);
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry leaveMorytaniaHideout3MES() {
        //if 93 agility & 78 mining use good shortcut else use shit one
        GameObject tunnel = getGameObject(0); //new tunnel ID
        if (client.getBoostedSkillLevel(Skill.AGILITY)<74 || client.getBoostedSkillLevel(Skill.MINING)<78)
        {
            tunnel = getGameObject(12770);
        }
        return createMenuEntry( tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry leaveMorytaniaHideout4LowAgilityMES() {
        //multiple objects with same ID so need to ensure it's the south tunnel
        WorldArea worldarea = new WorldArea(new WorldPoint(3488,9858,0),new WorldPoint(3495,9865,0));
        GameObject tunnel = new GameObjectQuery()
                .idEquals(12771)
                .result(client)
                .stream()
                .filter(t -> t.getWorldLocation().isInArea(worldarea))
                .findFirst()
                .orElse(null);
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry useLowAgilityShortcut1MES() {
        GameObject tunnel = getGameObject(0); //low agility tunnel ID
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }
    private MenuEntry useLowAgilityShortcut2MES() {
        GameObject tunnel = getGameObject(0); //low agility tunnel ID (2nd one)
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry UseHighAgilityShortcut2MES() {
        GameObject tunnel = getGameObject(0); //93 agility tunnel ID
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry enterAltarMES() {
        GameObject altar = getGameObject(38044);
        return createMenuEntry(34817, MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(altar).getX(), getLocation(altar).getY(), false);
    }

    private MenuEntry craftRunesMES()
    {
        GameObject altar = getGameObject(0);
        return createMenuEntry(
                29631,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(altar).getX(),
                getLocation(altar).getY(),
                true);
    }

    private boolean isInPOH() {
        return getGameObject(4525)!=null; //checks for portal, p sure this is same for everyone if not need to do alternative check.
    }

    private boolean isinBloodAltar() {
        int BLOOD_ALTAR_ID = 0;
        return getGameObject(BLOOD_ALTAR_ID)!=null;
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject == null) {
            return new Point(0, 0);
        }
        if (tileObject instanceof GameObject) {
            return ((GameObject) tileObject).getSceneMinLocation();
        }
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    private MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}
package net.runelite.client.plugins.oneclickamethyst;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.Menu;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.api.queries.WallObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.*;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(
        name = "One Click Amethyst",
        enabledByDefault = false,
        description = "Mines and chisels Amethyst. If no chisel in invent it will bank instead."
)
@Slf4j
public class OneClickAmethystPlugin extends Plugin
{

    Set<Integer> MINING_ANIMATION = Set.of(6752,6758,8344,4481,7282,8345);
    private boolean CHISELING = false;
    private final int AMETHYST_ID = 21347;
    int CHISEL_ID = 1755;

    @Inject
    private Client client;

    @Inject
    private OneClickAmethystConfig config;

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (event.getMenuOption().equals("<col=00ff00>One Click Amethyst"))
            handleClick(event);
    }

    @Provides
    OneClickAmethystConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickAmethystConfig.class);
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        String text;
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
            return;
        text = "<col=00ff00>One Click Amethyst";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }

    private void handleClick(MenuOptionClicked event) throws InterruptedException {
        if (getInventoryItem(AMETHYST_ID)==null)
        {
            System.out.println("1");
            CHISELING=false;
        }
        if (CHISELING)
        {
            System.out.println("2");
            event.consume();
            return;
        }
        System.out.println("3");
        if(client.getLocalPlayer().isMoving() ||client.getLocalPlayer().getPoseAnimation()
                != client.getLocalPlayer().getIdlePoseAnimation())
        {
            event.consume();
        }
        System.out.println("4");

        if(MINING_ANIMATION.contains(client.getLocalPlayer().getAnimation()))
        {
            event.consume();
            return;
        }
        System.out.println("5");

        if (getEmptySlots()>0)
        {
            event.setMenuEntry(mineAmethyst());
            return;
        }
        System.out.println("6");

        if (bankOpen())
        {
            event.setMenuEntry(depositAllMES());
            return;
        }

        if (getInventoryItem(CHISEL_ID)==null)
        {
            event.setMenuEntry(bankMES());
            return;
        }

        if (client.getWidget(270,5)!=null)
        {
            event.setMenuEntry(chooseProductMenuEntry());
            CHISELING = true;
            return;
        }
        System.out.println("7");
        event.setMenuEntry(useChiselOnAmethystMenuEntry());
    }


    private Point getLocation(WallObject wallObject) {
        return new Point(wallObject.getLocalLocation().getSceneX(),
                wallObject.getLocalLocation().getSceneY());
    }

    private WallObject getAmethystVein()
    {
        List<Integer> Ids= Arrays.asList(11388,11389);
        List<Player> players = new PlayerQuery()
                .result(client)
                .list;

        List<WallObject> wallObjects = new WallObjectQuery()
                .idEquals(Ids)
                .result(client)
                .stream()
                .filter(wallObject -> players.stream().noneMatch(p -> p.getWorldLocation().distanceTo(wallObject.getWorldLocation())<2))
                .collect(Collectors.toList());

        return wallObjects.stream()
                .min(Comparator.comparing(entityType -> entityType.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation())))
                .orElse(null);
    }

    private MenuEntry mineAmethyst() {
        WallObject customWallObject = getAmethystVein();
        return createMenuEntry(
                customWallObject.getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(customWallObject).getX(),
                getLocation(customWallObject).getY(), true);
    }

    private MenuEntry useChiselOnAmethystMenuEntry()
    {
        client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedItemSlot(getInventoryItem(CHISEL_ID).getIndex());
        client.setSelectedItemID(CHISEL_ID);
        return createMenuEntry(
                21347,
                MenuAction.ITEM_USE_ON_WIDGET_ITEM,
                getInventoryItem(AMETHYST_ID).getIndex(),
                9764864,
                true);
    }

    private MenuEntry chooseProductMenuEntry(){
        int ID = 17694737;
        if (config.getProduct()==Product.BOLTS)
        {
            ID = 17694734;
        }
        if (config.getProduct()==Product.JAVELIN)
        {
            ID = 17694736;
        }
        if (config.getProduct()==Product.ARROWTIPS)
        {
            ID = 17694735;
        }
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                ID,
                true);
    }

    private MenuEntry bankMES(){
        return createMenuEntry(
                4483,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(4483)).getX(),
                getLocation(getGameObject(4483)).getY(),
                false);
    }

    private MenuEntry depositAllMES(){
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                WidgetInfo.BANK_DEPOSIT_INVENTORY.getId(),
                false);
    }

    public int getEmptySlots() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryWidget.getWidgetItems().size();
        } else {
            return -1;
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

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
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
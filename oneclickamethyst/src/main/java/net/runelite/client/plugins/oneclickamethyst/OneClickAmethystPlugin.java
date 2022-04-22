package net.runelite.client.plugins.oneclickamethyst;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.api.queries.WallObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
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
    Set<Integer> GEMS = Set.of(1623,1621,1619,1617);
    Set<Integer> MINING_ANIMATION = Set.of(6752,6758,8344,4481,7282,8345);
    private boolean CHISELING = false;

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
        if (getInventoryItem(ItemID.AMETHYST)==null)
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

        if(config.dropGems() && !bankOpen())
        {
            for (int gem:GEMS)
            {
                if (getInventoryItem(gem)!=null)
                {
                    event.setMenuEntry(dropGemMES(getInventoryItem(gem)));
                    return;
                }
            }
        }

        if(config.useSpec() && !bankOpen()) {
            if (client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000) {
                event.setMenuEntry(specAtk());
                return;
            }
        }

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

        if (getInventoryItem(ItemID.CHISEL)==null)
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
        players.remove(client.getLocalPlayer()); //exempt own player else you move to diff rock if beside one

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

    private MenuEntry dropGemMES(Widget gem){
        return createMenuEntry(
                7,
                MenuAction.CC_OP_LOW_PRIORITY,
                gem.getIndex(),
                WidgetInfo.INVENTORY.getId(),
                false);
    }

    private MenuEntry specAtk(){
        Widget specAtk = client.getWidget(WidgetInfo.MINIMAP_SPEC_CLICKBOX);
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                specAtk.getId(),
                false);
    }

    private MenuEntry useChiselOnAmethystMenuEntry()
    {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(getInventoryItem(ItemID.CHISEL).getIndex());
        client.setSelectedSpellItemId(ItemID.CHISEL);
        return createMenuEntry(
                0,
                MenuAction.WIDGET_TARGET_ON_WIDGET,
                getInventoryItem(ItemID.AMETHYST).getIndex(),
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

    private Widget getInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return getWidgetItem(inventoryWidget,id);
        }
        if (bankInventoryWidget!=null && !bankInventoryWidget.isHidden())
        {
            return getWidgetItem(bankInventoryWidget,id);
        }
        return null;
    }

    private Widget getWidgetItem(Widget widget,int id) {
        for (Widget item : widget.getDynamicChildren())
        {
            if (item.getItemId() == id)
            {
                return item;
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
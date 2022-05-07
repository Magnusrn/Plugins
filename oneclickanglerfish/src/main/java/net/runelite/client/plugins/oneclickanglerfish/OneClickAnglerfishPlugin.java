package net.runelite.client.plugins.oneclickanglerfish;

import javax.inject.Inject;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Extension
@PluginDescriptor(
        name = "One Click Anglerfish",
        description = "Catches Anglerfish and banks",
        tags = {"angler","anglerfish","one","click","oneclick"}
)
public class OneClickAnglerfishPlugin extends Plugin {

    private String state = "BARREL";

    @Inject
    private Client client;

    @Inject
    private OneClickAnglerfishConfig config;

    @Provides
    OneClickAnglerfishConfig provideConfig(ConfigManager configManager) {
        return (OneClickAnglerfishConfig)configManager.getConfig(OneClickAnglerfishConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        state = "BARREL";
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
            return;
        String text = "<col=00ff00>One Click Anglerfish";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (event.getMenuOption().equals("<col=00ff00>One Click Anglerfish"))
            handleClick(event);
    }

    private void handleClick(MenuOptionClicked event) {
        List<Integer> fishingAnimations = Arrays.asList(622,633);
        if (fishingAnimations.stream().anyMatch(animation -> animation == client.getLocalPlayer().getAnimation()))
        {
            System.out.println("consume event as fishing");
            event.consume();
            return;
        }
        if (config.consumeClicks() &&
                (client.getLocalPlayer().isMoving() || client.getLocalPlayer().getPoseAnimation() != client.getLocalPlayer().getIdlePoseAnimation()) && !bankOpen())
        {
            System.out.println("Consume event because not idle?");
            event.consume();
            return;
        }

        if (getEmptySlots()>0 && getFishingSpot()!=null)
        {
            event.setMenuEntry(catchFishMES());
            return;
        }
        if (bankOpen())
        {
            if (getInventoryItem(ItemID.RAW_ANGLERFISH)==null)
            {
                event.consume();
                walkTile();
                return;
            }

            Set<Integer> CLUE_BOTTLE_SET = Set.of(ItemID.CLUE_BOTTLE_BEGINNER,ItemID.CLUE_BOTTLE_EASY,ItemID.CLUE_BOTTLE_MEDIUM,ItemID.CLUE_BOTTLE_HARD,ItemID.CLUE_BOTTLE_ELITE);
            for (int ClueBottle : CLUE_BOTTLE_SET)
            {
                if (getInventoryItem(ClueBottle)!=null)
                {
                    event.setMenuEntry(depositClueBottleMES(ClueBottle));
                    return;
                }
            }
            if (getInventoryItem(ItemID.OPEN_FISH_BARREL) != null) {
                switch (state) {
                    case "BARREL":
                        event.setMenuEntry(emptyBarrelMES());
                        state = "INVENTORY";
                        break;

                    case "INVENTORY":
                        event.setMenuEntry(depositAnglerfishMES());
                        state = "BARREL";
                        break;
                }
            } else {
                event.setMenuEntry(depositAnglerfishMES());
            }
            return;

        }

        if (getEmptySlots()==0)
        {
            event.setMenuEntry(bankMES());
            return;
        }
        event.consume();
        walkTile();
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

    private MenuEntry catchFishMES() {
        state = "BARREL"; //reset state before banking, workaround in case of spam clicking in bank messing up state before game has registered the container change
        return createMenuEntry(
                getFishingSpot().getIndex(),
                MenuAction.NPC_FIRST_OPTION,
                getLocation(getFishingSpot()).getX(),
                getLocation(getFishingSpot()).getY(),
                false);
    }

    private NPC getFishingSpot()
    {
        return new NPCQuery()
                .idEquals(6825)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private Point getLocation(NPC npc)
    {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
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

    private MenuEntry emptyBarrelMES() {
        return createMenuEntry(
                9,
                MenuAction.CC_OP_LOW_PRIORITY,
                getInventoryItem(ItemID.OPEN_FISH_BARREL).getIndex(),
                983043, false);
    }

    private MenuEntry depositClueBottleMES(int ID) {
        return createMenuEntry(
                2,
                MenuAction.CC_OP,
                getInventoryItem(ID).getIndex(),
                983043,
                false);
    }

    private MenuEntry depositAnglerfishMES() {
        return createMenuEntry(
                8,
                MenuAction.CC_OP_LOW_PRIORITY,
                getInventoryItem(ItemID.RAW_ANGLERFISH).getIndex(),
                983043,
                false);
    }

    private MenuEntry bankMES() {
        GameObject bank = getGameObject(27720);
        return createMenuEntry( bank.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, getLocation(bank).getX(), getLocation(bank).getY(), false);
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private void walkTile() {
        WorldPoint sw = new WorldPoint(1824,3774,0);
        WorldPoint ne = new WorldPoint(1831,3780,0);
        WorldArea worldArea = new WorldArea(sw,ne);
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(worldArea.toWorldPointList().size());
        WorldPoint randomPoint = worldArea.toWorldPointList().get(index);
        int x = randomPoint.getX() - client.getBaseX();
        int y = randomPoint.getY() - client.getBaseY();
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}
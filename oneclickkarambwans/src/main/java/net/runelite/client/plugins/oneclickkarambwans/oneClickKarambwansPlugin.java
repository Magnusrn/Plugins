package net.runelite.client.plugins.oneclickkarambwans;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@Extension
@PluginDescriptor(
        name = "One Click Karambwans",
        description = "Computer aided gaming. Make sure you're wearing dramen staff and recent fairy ring is DKP. Untested with Barrel.",
        tags = {"karambwans,one click,zanaris,fishing,oneclick"},
        enabledByDefault = false
)

@Slf4j
public class oneClickKarambwansPlugin extends Plugin {

    private final int FAIRY_RING_KARAMJA_ID = 29495;
    private final int FAIRY_RING_ZANARIS_ID = 29560;
    private final int BANK_ID = 26711;
    private final int KARAMBWANJI_ID = 3150;
    private final int KARAMBWAN_VESSEL_ID = 3159;
    private final int RAW_KARAMBWAN_ID = 3142;
    private final int FISH_BARREL_ID = 25584;
    private final int FISHING_ANIMATION = 1193;
    private final int FAIRY_RING_ANIMATION1 = 3265;
    private final int FAIRY_RING_ANIMATION2 = 3266;
    private final int FISHING_SPOT_ID = 4712;//might not be right
    private String state = "BARREL";

    @Inject
    private Client client;

    @Override
    protected void startUp() throws Exception {
        state = "BARREL";
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Karambwans")) {
            handleClick(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        String text;

        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        } else {
            text = "<col=00ff00>One Click Karambwans";
        }
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);

    }

    private void handleClick(MenuOptionClicked event) {
        if ((client.getLocalPlayer().isMoving()
                || client.getLocalPlayer().getPoseAnimation()
                != client.getLocalPlayer().getIdlePoseAnimation()
                || client.getLocalPlayer().getAnimation() == FISHING_ANIMATION
                || client.getLocalPlayer().getAnimation() == FAIRY_RING_ANIMATION1
                || client.getLocalPlayer().getAnimation() == FAIRY_RING_ANIMATION2)
                & !bankOpen()) {
            System.out.println("Consume event because not idle?");
            event.consume();
            return;
        }
        System.out.println("1");

        if (getInventQuantity(KARAMBWANJI_ID) == 0 || getInventQuantity(KARAMBWAN_VESSEL_ID) == 0) {
            System.out.println("Consume event because no karambwanji or vessel");
            event.consume();
            return;
        }
        System.out.println("2");

        if (getEmptySlots() == 0) {
            if (getGameObject(FAIRY_RING_KARAMJA_ID) != null) {
                event.setMenuEntry(toZanarisMES());
                return;
            } else if (!bankOpen()) {
                event.setMenuEntry(bankMES());
                return;
            }
        }

        if (getInventQuantity(RAW_KARAMBWAN_ID) == 0 && getGameObject(FAIRY_RING_ZANARIS_ID) != null) {
            event.setMenuEntry(toKaramjaMES());
            return;
        }

        if (bankOpen()) {
            Set<Integer> CLUE_BOTTLE_SET = Set.<Integer>of(ItemID.CLUE_BOTTLE_BEGINNER,ItemID.CLUE_BOTTLE_EASY,ItemID.CLUE_BOTTLE_MEDIUM,ItemID.CLUE_BOTTLE_HARD,ItemID.CLUE_BOTTLE_ELITE);
            for (int ClueBottle : CLUE_BOTTLE_SET)
            {
                if (getInventoryItem(ClueBottle)!=null)
                {
                    event.setMenuEntry(depositClueBottleMES(ClueBottle));
                    return;
                }
            }

            if (getInventoryItem(FISH_BARREL_ID) != null) {
                switch (state) {
                    case "BARREL":
                        event.setMenuEntry(emptyBarrelMES());
                        state = "INVENTORY";
                        break;

                    case "INVENTORY":
                        event.setMenuEntry(depositKarambwansMES());
                        state = "BARREL";
                        break;
                }
            } else {
                event.setMenuEntry(depositKarambwansMES());
            }
            return;
        }

        if (getGameObject(FAIRY_RING_KARAMJA_ID) != null) {
            event.setMenuEntry(fishingSpotMES());
        }
    }

    private MenuEntry fishingSpotMES() {
        return new MenuEntry("Fish",
                "<col=ffff00>Fishing spot",
                getFishingSpot().getIndex(),
                MenuAction.NPC_FIRST_OPTION.getId(),
                getNPCLocation(getFishingSpot()).getX(),
                getNPCLocation(getFishingSpot()).getY(),
                false);
    }

    private MenuEntry toZanarisMES() {
        return new MenuEntry("Zanaris",
                "<col=ffff>Fairy ring",
                29495,
                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                getLocation(getGameObject(FAIRY_RING_KARAMJA_ID)).getX(),
                getLocation(getGameObject(FAIRY_RING_KARAMJA_ID)).getY(),
                false);
    }

    private MenuEntry bankMES() {
        return new MenuEntry("Use",
                "<col=ffff>Bank chest",
                26711,
                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                getLocation(getGameObject(BANK_ID)).getX(),
                getLocation(getGameObject(BANK_ID)).getY(),
                false);
    }

    private MenuEntry toKaramjaMES() {
        return new MenuEntry("Last-destination (DKP)",
                "<col=ffff>Fairy ring",
                29560,
                MenuAction.GAME_OBJECT_THIRD_OPTION.getId(),
                getLocation(getGameObject(FAIRY_RING_ZANARIS_ID)).getX(),
                getLocation(getGameObject(FAIRY_RING_ZANARIS_ID)).getY(),
                false);
    }

    private MenuEntry depositKarambwansMES() {
        return new MenuEntry("Deposit-All",
                "<col=ff9040>Raw karambwan</col>",
                8,
                MenuAction.CC_OP_LOW_PRIORITY.getId(),
                getInventoryItem(RAW_KARAMBWAN_ID).getIndex(),
                983043,
                false);
    }

    private MenuEntry depositClueBottleMES(int ID) {
        return new MenuEntry(
                "Deposit-1",
                "<col=ff9040>Clue bottle (medium)</col>",
                2,
                MenuAction.CC_OP.getId(),
                getInventoryItem(ID).getIndex(),
                983043,
                false);
    }

    private MenuEntry emptyBarrelMES() {
        return new MenuEntry("Empty",
                "<col=ff9040>Open fish barrel</col>",
                9,
                MenuAction.CC_OP_LOW_PRIORITY.getId(),
                getInventoryItem(FISH_BARREL_ID).getIndex(),
                983043, false);
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
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

    @Nullable
    private Collection<WidgetItem> getInventoryItems() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
        if (inventory == null) {
            return null;
        }
        return new ArrayList<>(inventory.getWidgetItems());
    }

    public int getInventQuantity(Integer itemId) {
        Collection<WidgetItem> inventoryItems = getInventoryItems();
        if (inventoryItems == null) {
            return 0;
        }
        int count = 0;
        for (WidgetItem inventoryItem : inventoryItems) {
            if (inventoryItem.getId() == itemId) {
                count += 1;
            }
        }
        return count;
    }

    public int getEmptySlots() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryWidget.getWidgetItems().size();
        } else {
            return -1;
        }
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

    private Point getNPCLocation(NPC npc)
    {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }

    private NPC getFishingSpot()
    {
        return new NPCQuery()
                .idEquals(FISHING_SPOT_ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
}
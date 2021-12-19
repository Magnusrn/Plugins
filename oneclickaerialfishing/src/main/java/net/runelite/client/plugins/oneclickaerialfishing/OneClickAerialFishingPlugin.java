package net.runelite.client.plugins.oneclickaerialfishing;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "One Click Aerial Fishing",
        description = "Ensure some form of bait is in invent, either worms or fish chunks and a knife. If there's no knife it will just drop the fish.",
        tags = {"one", "click", "tench", "fishing","aerial","arial"},
        enabledByDefault = false
)
public class OneClickAerialFishingPlugin extends Plugin {

    private static final int TENCH_FISHING_SPOT_NPC_ID = 8523;
    private static final int KNIFE_ID = 946;
    private static final int KING_WORM = 2162;
    private static final int FISH_CHUNKS = 22818;
    private static final List<Integer> LIST_OF_FISH_IDS= Arrays.asList(22826, 22829, 22832,22835);
    private static boolean shouldCut = false;

    @Inject
    private Client client;

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if(event.getMenuOption().equals("<col=00ff00>One Click Aerial Fishing"))
        {
            handleClick(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }
        String text = "<col=00ff00>One Click Aerial Fishing";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }

    private void handleClick(MenuOptionClicked event)
    {
        if (getEmptySlots()>0
                && ((getLastInventoryItem(KING_WORM)!=null)||(getLastInventoryItem(FISH_CHUNKS)!=null)) //if bait exists
                && !shouldCut)
        {
            event.setMenuEntry(catchFishMenuEntry());
            return;
        }
        else
        {
            shouldCut = true;
        }
        //if space in inventory then fish, else cut // add check for bait!

        for (int fish:LIST_OF_FISH_IDS)
        {
            if (getLastInventoryItem(fish)!=null)
            {
                if (getLastInventoryItem(KNIFE_ID)==null)
                {
                    event.setMenuEntry(dropFishMenuEntry(getLastInventoryItem(fish)));
                    return;
                }
                client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
                client.setSelectedItemSlot(getLastInventoryItem(KNIFE_ID).getIndex());
                client.setSelectedItemID(KNIFE_ID);
                event.setMenuEntry(useKnifeOnFishMenuEntry(getLastInventoryItem(fish)));
                return;
            }
        }
        shouldCut=false;
    }

    private WidgetItem getLastInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            int LastIndex = -1;
            WidgetItem LastItem = null;
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                    if (item.getIndex()>LastIndex) {
                        LastIndex = item.getIndex();
                        LastItem = item;
                    }
                }
            }
            return LastItem;
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

    private MenuEntry useKnifeOnFishMenuEntry(WidgetItem Fish){
        return createMenuEntry(
                Fish.getId(),
                MenuAction.ITEM_USE_ON_WIDGET_ITEM,
                Fish.getIndex(),
                9764864,
                false);
    }

    private MenuEntry dropFishMenuEntry(WidgetItem Fish){
        return createMenuEntry(
                Fish.getId(),
                MenuAction.ITEM_FIFTH_OPTION,
                Fish.getIndex(),
                9764864,
                false);
    }

    private MenuEntry catchFishMenuEntry(){
        NPC FishingSpot = getFishingSpot();
        return createMenuEntry(
                FishingSpot.getIndex(),
                MenuAction.NPC_FIRST_OPTION,
                getNPCLocation(FishingSpot).getX(),
                getNPCLocation(FishingSpot).getY(),
                true);
    }

    private NPC getFishingSpot()
    {
        return new NPCQuery()
                .idEquals(TENCH_FISHING_SPOT_NPC_ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private Point getNPCLocation(NPC npc)
    {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}

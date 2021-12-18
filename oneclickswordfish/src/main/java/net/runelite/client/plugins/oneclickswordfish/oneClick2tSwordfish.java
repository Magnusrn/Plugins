package net.runelite.client.plugins.oneclickswordfish;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

@Extension
@PluginDescriptor(
        name = "One Click 2t Swordfish",
        description = "Shutupbitch",
        tags = {"one", "click", "oneclick", "2t", "swordfish"}
)
public class oneClick2tSwordfish extends Plugin {

    private final int SWORDFISH_ID = 371;
    private final int TUNA_ID = 359;
    private int fishingCycle;
    private int timeout;

    @Inject
    private Client client;

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        if (fishingCycle>0)
        {
            fishingCycle--;
        }
        if (timeout>0)
        {
            timeout--;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (event.getMenuTarget().equals("<col=ffff00>Fishing spot"))
        {
            if (timeout>0)
            {
                event.consume();
                return;
            }
            handleClick(event);
            timeout=1; //adds a 1t timeout(consumes clicks during) on any click of the fishing spot
        }
    }

    private void handleClick(MenuOptionClicked event) {
        if (fishingCycle!=1){
            if (getInventoryItem(TUNA_ID)!=null || getInventoryItem(SWORDFISH_ID)!=null)
            {
                event.setMenuEntry(dropFishMES());
                return;
            }
            event.setMenuEntry(walkHereMES(event));
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
        if (hitsplatApplied.getActor() == client.getLocalPlayer())
        {
            fishingCycle=2;
        }
    }

    private MenuEntry dropFishMES(){
        for (WidgetItem item:getInventoryItems()) {
            if (item.getId()==TUNA_ID)
            {
                return createMenuEntry(
                        TUNA_ID, //item ID
                        MenuAction.ITEM_FIFTH_OPTION,
                        item.getIndex(), //inventory index
                        9764864,
                        false);
            }
            else if (item.getId()==SWORDFISH_ID)
            {
                return createMenuEntry(
                        SWORDFISH_ID, //item ID
                        MenuAction.ITEM_FIFTH_OPTION,
                        item.getIndex(), //inventory index
                        9764864,
                        false);
            }
        }
        return null;
    }

    private MenuEntry walkHereMES(MenuOptionClicked event){
        return createMenuEntry(
                0,
                MenuAction.WALK,
                event.getParam0(),
                event.getParam1(), false);
    }

    @Nullable
    private Collection<WidgetItem> getInventoryItems() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
        if (inventory == null)
        {
            return null;
        }
        return new ArrayList<>(inventory.getWidgetItems());
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

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}

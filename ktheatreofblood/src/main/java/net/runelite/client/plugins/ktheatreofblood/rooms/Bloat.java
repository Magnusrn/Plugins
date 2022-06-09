package net.runelite.client.plugins.ktheatreofblood.rooms;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.ktheatreofblood.KTheatreOfBloodConfig;
import net.runelite.client.plugins.ktheatreofblood.KTheatreOfBloodPlugin;
import net.runelite.client.plugins.ktheatreofblood.Room;

import javax.inject.Inject;

public class Bloat extends Room {
    private boolean equippedNeck = false;
    private NPC bloat;

    @Inject
    private Client client;

    @Inject
    private KTheatreOfBloodConfig config;

    @Provides
    KTheatreOfBloodConfig provideConfig(ConfigManager configManager) {
        return (KTheatreOfBloodConfig) configManager.getConfig(KTheatreOfBloodConfig.class);
    }

    @Inject
    protected Bloat(KTheatreOfBloodPlugin plugin, KTheatreOfBloodConfig config) {
        super(plugin, config);
    }

    @Override
    protected void startUp() throws Exception {
        System.out.println("starting plugin bloat");
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
       if (event.getNpc().getName()== null) return;
       if (event.getNpc().getName().contains("Pestilent Bloat"))
       {
           bloat = event.getNpc();
       }
    }
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (!config.PneckHelper()) return;
        if (config.PneckHelperThreshold() < client.getBoostedSkillLevel(Skill.HITPOINTS)) return;
        if (equippedNeck) return;
        Widget necklace = getInventoryItem(ItemID.PHOENIX_NECKLACE);
        if (necklace == null) return;
        if (bloat==null || bloat.getAnimation() == 8082) return; //don't equip necks if bloat is standing still!

        if (event.getMenuTarget().contains("Pestilent Bloat"))
        {
            setMenuEntry(event,equipNecklace());
            equippedNeck = true;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getMessage().contains("Your phoenix necklace heals you, but is destroyed in the process."))
        {
            equippedNeck = false;
        }
    }

    private Widget getInventoryItem(int id) {
        client.runScript(6009, 9764864, 28, 1, -1);
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

    private Widget getWidgetItem(Widget widget, int id) {
        for (Widget item : widget.getDynamicChildren())
        {
            if (item.getItemId() == id)
            {
                return item;
            }
        }
        return null;
    }

    private MenuEntry equipNecklace() {
        Widget necklace = getInventoryItem(ItemID.PHOENIX_NECKLACE);
        if (necklace == null) return null;
        return createMenuEntry(3, MenuAction.CC_OP, necklace.getIndex(), WidgetInfo.INVENTORY.getId(), false);
    }

    private void setMenuEntry(MenuOptionClicked event, MenuEntry menuEntry){
        event.setId(menuEntry.getIdentifier());
        event.setMenuAction(menuEntry.getType());
        event.setParam0(menuEntry.getParam0());
        event.setParam1(menuEntry.getParam1());
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}

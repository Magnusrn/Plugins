package net.runelite.client.plugins.oneclickcustom.utils;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class Inventory {

    @Inject
    private Client client;

    public Widget getItemByInventoryIndex(int index) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return Arrays.stream(inventoryWidget.getDynamicChildren())
                    .filter(item -> item.getIndex() == index)
                    .findAny().orElse(null);
        }
        return null;
    }

    public void setSelectedInventoryItem(Widget item) {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(item.getIndex());
        client.setSelectedSpellItemId(item.getItemId());
    }

    public int getEmptySlots() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());

        if (bankInventory!=null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren()!=null)
        {
            return getEmptySlots(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        }

        if (inventory!=null && inventory.getDynamicChildren()!=null)
        {
            return getEmptySlots(WidgetInfo.INVENTORY);
        }

        return -1;
    }

    private int getEmptySlots(WidgetInfo widgetInfo) {
        client.runScript(6009, 9764864, 28, 1, -1);
        List<Widget> inventoryItems = Arrays.asList(client.getWidget(widgetInfo.getId()).getDynamicChildren());
        return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
    }

    public Widget getLastInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return getLastWidgetItem(inventoryWidget,id);
        }
        return null;
    }

    public Widget getLastWidgetItem(Widget widget,int id) {
        client.runScript(6009, 9764864, 28, 1, -1);
        return Arrays.stream(widget.getDynamicChildren())
                .filter(item -> item.getItemId()==id)
                .reduce((first, second) -> second)
                .orElse(null);
    }
}
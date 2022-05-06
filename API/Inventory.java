public class Inventory {
    private int getEmptySlots() {
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

    private Widget getLastInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return getLastWidgetItem(inventoryWidget,id);
        }
        return null;
    }

    private Widget getLastWidgetItem(Widget widget,int id) {
        return Arrays.stream(widget.getDynamicChildren())
                .filter(item -> item.getItemId()==id)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private Widget getInventoryItem(int id) {
        client.runScript(6009, 9764864, 28, 1, -1); //rebuild inventory ty pajeet
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (bankInventoryWidget!=null && !bankInventoryWidget.isHidden())
        {
            return getWidgetItem(bankInventoryWidget,id);
        }
        if (inventoryWidget!=null) //if hidden check exists then you can't access inventory from any tab except inventory
        {
            return getWidgetItem(inventoryWidget,id);
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

    private void setSelectedInventoryItem(Widget item) {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(item.getIndex());
        client.setSelectedSpellItemId(item.getItemId()); //this might be different inside/outside bank? getID works sometimes idfk
    }
}
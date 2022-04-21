public class Teleporting {
    private MenuEntry teleToPOHMES() {
        Widget tab = getInventoryItem(ItemID.TELEPORT_TO_HOUSE);
        Widget conCape = getInventoryItem(ItemID.CONSTRUCT_CAPE);
        Widget conCapeT = getInventoryItem(ItemID.CONSTRUCT_CAPET);

        if (conCape!=null)
        {
            return createMenuEntry(6, MenuAction.CC_OP_LOW_PRIORITY, conCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (conCapeT!=null)
        {
            return createMenuEntry(6, MenuAction.CC_OP_LOW_PRIORITY, conCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (tab!=null)
        {
            return createMenuEntry(2, MenuAction.CC_OP, tab.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT)!=null)
        {
            if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE_13342))
            {
                return createMenuEntry(5, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
            }
            if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CONSTRUCT_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CONSTRUCT_CAPET))
            {
                return createMenuEntry( 4, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
            }
        }
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_TELEPORT_TO_HOUSE.getId(), false);
    }

    private MenuEntry teleToBankMES() {
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE_13342))
        {
            return createMenuEntry(4, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CRAFTING_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CRAFTING_CAPET))
        {
            return createMenuEntry(3, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
        }

        Widget craftingCape = getInventoryItem(ItemID.CRAFTING_CAPE);
        Widget craftingCapeT = getInventoryItem(ItemID.CRAFTING_CAPET);
        if (craftingCape!=null)
        {
            return createMenuEntry(4, MenuAction.CC_OP, craftingCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (craftingCapeT!=null)
        {
            return createMenuEntry(4, MenuAction.CC_OP, craftingCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (client.getVarbitValue(4070)==0) //if on standard spellbook
        {
            return createMenuEntry(2, MenuAction.CC_OP, -1, WidgetInfo.SPELL_CAMELOT_TELEPORT.getId(), false);
        }
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_MOONCLAN_TELEPORT.getId(), false);
    }


}
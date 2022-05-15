package net.runelite.client.plugins.oneclickcustom;

import net.runelite.client.config.*;

@ConfigGroup("oneclickcustom")
public interface oneClickCustomConfig extends Config {
    @ConfigSection(
            name = "Full Inventory",
            description = "Options for when inventory is full",
            position = 7,
            closedByDefault = true
    )
    String inventoryFullSection = "Full Inventory";

    @ConfigItem(
            position = 0,
            keyName = "oneClickType",
            name = "One Click Type",
            description = "Gather is for woodcutting mining etc."
    )
    default oneClickCustomTypes.methods oneClickType()
    {
        return oneClickCustomTypes.methods.Gather;
    }

    @ConfigItem(
            position = 1,
            keyName = "IDs",
            name = "IDs",
            description = "Input game object or NPC(inc fishign) IDs. Separate with commas",
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Gather||Fish||Attack||Pickpocket||Pick_Up"
    )
    default String IDs()
    {
        return "";
    }
    @Range(
            min = 1,
            max = 5
    )
    @ConfigItem(
            position = 100,
            keyName = "opcode",
            name = "Custom Opcode",
            description = "Useful in some cases where the first action isn't the one that you want",
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Gather||Fish"
    )
    default int opcode() {
        return 1;
    }
    @ConfigItem(
            name = "Item on GameObject",
            keyName = "itemongameobjectstring",
            description = "Input item you wish to use on a gameobject(if multiple, will return then nearest from the list). First ID should be the item you wish to click in your inventory. Comma separate each value and separate each inventory ID item with a newline. See github.com/magnusrn/plugins readme for examples ",
            position = 2,
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Use_Item_On_X"
    )
    default String itemOnGameObjectString()
    {
        return "";
    }

    @ConfigItem(
            name = "Item on NPC",
            keyName = "itemonnpcstring",
            description = "Input item you wish to use on an NPC(if multiple, will return then nearest from the list). First ID should be the item you wish to click in your inventory. Comma separate each value and separate each inventory ID item with a newline. See github.com/magnusrn/plugins readme for examples ",
            position = 3,
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Use_Item_On_X"
    )
    default String itemOnNpcString()
    {
        return "";
    }

    @ConfigItem(
            position = 4,
            keyName = "consumeClick",
            name = "Consume Click",
            description = "Consumes click if player is not idle",
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Gather||Fish||Attack||Pick_Up"
    )
    default boolean consumeClick() {
        return true;
    }

    @ConfigItem(
            position = 5,
            keyName = "InventoryFull",
            name = "Disable",
            description = "Disable on full invent",
            section = inventoryFullSection,
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Gather||Fish||Pickpocket||Pick_Up"
    )
    default boolean InventoryFull() {
        return true;
    }

    @ConfigItem(
            position = 6,
            keyName = "Bank",
            name = "Bank",
            description = "Bank if full inventory and deposit all",
            section = inventoryFullSection,
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Gather||Fish||PickPocket||Pick_Up"
    )
    default boolean Bank() {
        return false;
    }

    @ConfigItem(
            position = 7,
            keyName = "bankType",
            name = "Bank Type",
            description = "Choose",
            section = inventoryFullSection,
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Gather||Fish||PickPocket||Pick_Up"
    )
    default oneClickCustomTypes.bankTypes bankType() { return oneClickCustomTypes.bankTypes.NPC; }

    @ConfigItem(
            position = 8,
            keyName = "bankID",
            name = "Bank ID",
            description = "Input bank ID, supports chests or NPCs",
            section = inventoryFullSection,
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Gather||Fish||PickPocket||Pick_Up"
    )
    default int bankID()
    {
        return 0;
    }

    @ConfigItem(
            position = 9,
            keyName = "withinTiles",
            name = "Distance Radius",
            description = "Search for objects within how many tiles"
    )
    default int withinTiles()
    {
        return 100;
    }
}
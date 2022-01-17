package net.runelite.client.plugins.oneclickcustom;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickcustom")
public interface oneClickCustomConfig extends Config {
    @ConfigItem(
            position = 0,
            keyName = "oneClickType",
            name = "One Click Type",
            description = "Gather is for woodcutting mining etc."
    )
    default oneClickCustomTypes oneClickType()
    {
        return oneClickCustomTypes.Gather;
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
            keyName = "InventoryFull",
            name = "Inventory Full",
            description = "Disable on full invent",
            hidden = true,
            unhide = "oneClickType",
            unhideValue = "Gather||Fish||Pickpocket||Pick_Up"
    )
    default boolean InventoryFull() {
        return true;
    }

    @ConfigItem(
            position = 5,
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


}
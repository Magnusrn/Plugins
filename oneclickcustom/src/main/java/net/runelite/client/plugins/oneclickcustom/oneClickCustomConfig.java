package net.runelite.client.plugins.oneclickcustom;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickcustom")
public interface oneClickCustomConfig extends Config {
    @ConfigItem(
            position = 0,
            keyName = "ID",
            name = "ID",
            description = "Input game object or NPC(inc fishign) ID"
    )
    default int ID()
    {
        return 0;
    }

    @ConfigItem(
            position = 1,
            keyName = "oneClickType",
            name = "One Click Type",
            description = "Gather is for woodcutting mining etc."
    )
    default oneClickCustomTypes oneClickType()
    {
        return oneClickCustomTypes.Gather;
    }

    @ConfigItem(
            position = 2,
            keyName = "InventoryFull",
            name = "InventoryFull",
            description = "Disable on full invent"
    )
    default boolean InventoryFull() {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "consumeClick",
            name = "Consume Click",
            description = "Consumes click if player is not idle"
    )
    default boolean consumeClick() {
        return true;
    }
}
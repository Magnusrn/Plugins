package net.runelite.client.plugins.oneclickbloods;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickbloods")
public interface OneClickBloodsConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "afkchisel",
            name = "AFK chisel",
            description = "Consumes clicks on chisel animation."
    )
    default boolean afkChisel()
    {
        return false;
    }
}

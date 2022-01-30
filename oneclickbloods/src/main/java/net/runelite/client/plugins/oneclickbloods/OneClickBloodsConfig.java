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

    @ConfigItem(
            position = 0,
            keyName = "manualwalk",
            name = "Manually Walk",
            description = "Disable this if you want to oneclick walk towards the altar, Disable with caution as it walks to the altar by using a chisel on the same rockslide every time."
    )
    default boolean manuallyWalk()
    {
        return false;
    }
    @ConfigItem(
            position = -1,
            keyName = "useSpec",
            name = "Use Special Attack",
            description = "Uses special attack if its 100% before mining. Useful for things like dragon pickaxe"
    )
    default boolean useSpec(){
        return false;
    }
}

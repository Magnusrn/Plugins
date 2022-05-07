package net.runelite.client.plugins.oneclickanglerfish;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickanglerfish")
public interface OneClickAnglerfishConfig extends Config {
    @ConfigItem(
            position = 1,
            keyName = "consumeClicks",
            name = "Consume Clicks",
            description = "Consume Clicks while moving"
    )
    default boolean consumeClicks() {
        return true;
    }
}
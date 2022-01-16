package net.runelite.client.plugins.oneclickteaks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickteaks")
public interface OneClickTeaksConfig extends Config {
    @ConfigItem(
            position = 0,
            keyName = "consumeClicks",
            name = "Consume Clicks",
            description = "Consumes click if player is not idle"
    )
    default boolean consumeClicks() {
        return true;
    }
}
package net.runelite.client.plugins.oneclicksandstone;

import net.runelite.client.config.Config;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickzmi")
public interface OneClickSandstoneConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "forceMineNorth",
            name = "North Rocks Only?",
            description = "Will only mine the rocks at the north that people normally mine otherwise will mine the nearest rock at all times."
    )
    default boolean forceMineNorth() {
        return true;
    }
}

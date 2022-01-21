package net.runelite.client.plugins.oneclickchins;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickchins")
public interface oneclickchinsconfig extends Config
{
    @ConfigItem(
            keyName = "playerspotted",
            name = "AFK if player visible",
            description = "Consumes all clicks if another player is spotted. Prevents accidentally trying to pickup their traps. This may cause problems if people are doing agility course. needs to be tested",
            position = 0
    )
    default boolean playerspotted()
    {
        return true;
    }

    @ConfigItem(
            keyName = "chinchompatype",
            name = "Chinchompa Type",
            description = "Choose which chinchompa to catch",
            position = 1
    )
    default ChinchompaType chinchompaType() {
        return ChinchompaType.Red;
    }

    @ConfigItem(
            keyName = "withinxtiles",
            name = "Distance to search for traps",
            description = "input the number of tiles you want the search radius to be from your player.",
            position = 1
    )
    default int withinXtiles() {
        return 4;
    }
}

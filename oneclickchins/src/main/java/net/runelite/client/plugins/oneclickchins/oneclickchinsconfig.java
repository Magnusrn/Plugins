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
}

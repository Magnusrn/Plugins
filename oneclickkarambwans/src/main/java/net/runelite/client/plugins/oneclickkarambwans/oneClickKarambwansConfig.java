package net.runelite.client.plugins.oneclickkarambwans;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickkarambwans")
public interface oneClickKarambwansConfig extends Config {

    @ConfigItem(
            position=1,
            keyName = "bankAtSeers",
            name = "Use Seers Village bank",
            description = "Banks at Seers village instead of Zanaris"
    )
    default boolean bankAtSeers()
    {
        return true;
    }

    @ConfigItem(
            position=2,
            keyName = "pohFairyRing",
            name = "Use POH Fairy Ring",
            description = "Teles to poh using con cape/max cape/standard spellbook tele"
    )
    default boolean pohFairyRing()
    {
        return true;
    }

    @ConfigItem(
            position= 3,
            keyName = "debug",
            name = "Debug",
            description = "Posts debug message to chat if having problems"
    )
    default boolean debug()
    {
        return true;
    }
}

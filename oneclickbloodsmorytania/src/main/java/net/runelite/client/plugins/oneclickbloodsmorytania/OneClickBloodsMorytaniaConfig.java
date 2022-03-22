package net.runelite.client.plugins.oneclickbloodsmorytania;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickbloodsmorytania")
public interface OneClickBloodsMorytaniaConfig extends Config
{
    @ConfigItem(
            position = 1,
            keyName = "",
            name = "",
            description = ""
    )
    default boolean config1()
    {
        return false;
    }



}
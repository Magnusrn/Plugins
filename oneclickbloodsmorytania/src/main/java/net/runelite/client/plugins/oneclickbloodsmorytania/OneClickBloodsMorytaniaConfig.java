package net.runelite.client.plugins.oneclickbloodsmorytania;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickbloodsmorytania")
public interface OneClickBloodsMorytaniaConfig extends Config
{
    @ConfigItem(
            position = 1,
            keyName = "runenergy",
            name = "Run Energy",
            description = "Drink Pool at what Run Energy?"
    )
    default int runEnergy()
    {
        return 50;
    }
}
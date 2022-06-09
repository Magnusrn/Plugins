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

    @ConfigItem(
            position = 2,
            keyName = "overrideagility",
            name = "Override Agility",
            description = "Force use the 93 agility 78 mining shortcut"
    )
    default boolean overrideAgility()
    {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "essenceType",
            name = "Essence",
            description = "Choose Daeyalt or Pure Essence"
    )
    default EssenceType essenceType() {
        return EssenceType.PURE_ESSENCE;
    }
}
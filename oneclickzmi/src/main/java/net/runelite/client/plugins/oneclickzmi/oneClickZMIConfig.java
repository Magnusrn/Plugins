package net.runelite.client.plugins.oneclickzmi;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickzmi")
public interface oneClickZMIConfig extends Config{

    @ConfigItem(
            position = 0,
            keyName = "foodID",
            name = "Food ID",
            description = "Input id for your desired food. Default is Karambwan"
    )
    default int foodID()
    {
        return 3144;
    }

    @ConfigItem(
            position = 1,
            keyName = "essenceType",
            name = "Essence",
            description = "Choose Daeyalt or Pure Essence"
    )
    default EssenceType essenceType() {
        return EssenceType.Daeyalt;
    }

    @ConfigItem(
            position = 2,
            keyName = "drinkStamina",
            name = "Drink Stamina?",
            description = "Withdraws and drinks staminas if run energy below 80 or stamina not currently active"
    )
    default boolean drinkStamina() {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "consumeclicks",
            name = "Consume Clicks when running",
            description = "Consume Clicks"
    )
    default boolean consumeClicks() {
        return false;
    }

    @ConfigItem(
            position = 4,
            keyName = "debug",
            name = "Debug",
            description = "Debug"
    )
    default boolean debug() {
        return false;
    }
}

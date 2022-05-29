package net.runelite.client.plugins.oneclickfurnace;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickfurnace")
public interface OneClickFurnaceConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "method",
            name = "Crafting Method",
            description = "Choose what you want to craft"
    )
    default CraftingMethods method() {
        return CraftingMethods.CannonBalls;
    }

    @ConfigItem(
            position = 1,
            keyName = "consumeClicks",
            name = "Consume Clicks",
            description = "Consume clicks while moving/crafting"
    )
    default boolean consumeClicks() {
        return true;
    }
}
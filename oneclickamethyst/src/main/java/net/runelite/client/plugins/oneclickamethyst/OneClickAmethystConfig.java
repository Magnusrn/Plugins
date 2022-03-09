package net.runelite.client.plugins.oneclickamethyst;

import net.runelite.client.config.*;

@ConfigGroup("oneclickamethyst")
public interface OneClickAmethystConfig extends Config
{
    @ConfigItem(
            keyName = "Product",
            name = "Product",
            description = "Mines and chisels Amethyst.",
            position = 0
    )
    default Product getProduct()
    {
        return Product.DARTS;
    }

    @ConfigItem(
            position = 1,
            keyName = "useSpec",
            name = "Use Special Attack",
            description = "Uses special attack if its 100% before mining."
    )
    default boolean useSpec(){return false;}

    @ConfigItem(
            position = 1,
            keyName = "dropGems",
            name = "Drop Gems",
            description = "Drops gems if enabled "
    )
    default boolean dropGems(){return false;}
}
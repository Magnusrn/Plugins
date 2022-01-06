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
}
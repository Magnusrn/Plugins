package net.runelite.client.plugins.oneclickamethyst;

import net.runelite.client.config.*;

@ConfigGroup("oneclickamethyst")
public interface OneClickAmethystConfig extends Config
{
    @ConfigItem(
            keyName = "Product",
            name = "Product",
            description = "select the chosen product you want to make from the ore",
            position = 0
    )
    default Product getProduct()
    {
        return Product.DARTS;
    }
}
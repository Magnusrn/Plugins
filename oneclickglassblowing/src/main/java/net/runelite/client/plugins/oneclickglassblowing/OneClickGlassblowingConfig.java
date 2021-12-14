package net.runelite.client.plugins.oneclickglassblowing;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickglassblowing")
public interface OneClickGlassblowingConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "glassblowingType",
            name = "Item",
            description = "Choose item to blow"
    )
    default GlassblowingType.GlassblowingItem glassblowingType() {
        return GlassblowingType.GlassblowingItem.Lantern_Lens;
    }

    @ConfigItem(
            position = 1,
            keyName = "Bank",
            name = "Bank",
            description = "Choose which bank use."
    )
    default GlassblowingType.BankChoice bankChoice() {
        return GlassblowingType.BankChoice.Clan_Hall;
    }

}

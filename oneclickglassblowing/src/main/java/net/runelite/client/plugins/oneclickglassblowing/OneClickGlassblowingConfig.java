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
            keyName = "bankType",
            name = "Bank Type",
            description = "Choose"
    )
    default BankType bankType() { return BankType.Booth; }

    @ConfigItem(
            position = 2,
            keyName = "bankID",
            name = "Bank ID",
            description = "Input bank ID, supports chests/NPCs/Booths. Default is Fossil Island north Bank"
    )
    default int bankID()
    {
        return 30796;
    }
}

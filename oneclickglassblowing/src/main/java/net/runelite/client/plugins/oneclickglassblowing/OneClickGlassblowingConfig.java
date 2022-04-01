package net.runelite.client.plugins.oneclickglassblowing;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickglassblowing")
public interface OneClickGlassblowingConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "mode",
            name = "Glass Mode",
            description = "Choose"
    )
    default Types.Mode mode() { return Types.Mode.GLASSBLOWING; }

    @ConfigItem(
            position = 1,
            keyName = "glassblowingType",
            name = "Item",
            description = "Choose item to blow",
            hidden = true,
            unhide = "mode",
            unhideValue = "GLASSBLOWING"
    )
    default Types.Product product() {
        return Types.Product.LANTERN_LENS;
    }

    @ConfigItem(
            position = 2,
            keyName = "bankType",
            name = "Bank Type",
            description = "Choose"
    )
    default Types.Banks bankType() { return Types.Banks.CHEST; }

    @ConfigItem(
            position = 3,
            keyName = "bankID",
            name = "Bank ID",
            description = "Input bank ID, supports chests/NPCs/Booths. Default is Fossil Island north Bank"
    )
    default int bankID()
    {
        return 30796;
    }
}

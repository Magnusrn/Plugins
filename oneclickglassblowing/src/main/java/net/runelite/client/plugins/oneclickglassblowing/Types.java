package net.runelite.client.plugins.oneclickglassblowing;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class Types {
    public enum Banks {
        NPC,
        BOOTH,
        CHEST,
    }

    public enum Product {
        BEER_GLASS(17694734),
        CANDLE_LANTERN(17694735),
        OIL_LAMP(17694736),
        VIAL(17694737),
        FISHBOWL(17694738),
        UNPOWERED_ORB(17694739),
        LANTERN_LENS(17694740),
        LIGHT_ORB(17694741);

        public final int ID;

        Product(int ID) {
            this.ID = ID;
        }
    }

    public enum Mode {
        GLASSBLOWING,
        SUPERGLASS_MAKE
    }

    public enum SuperGlassMakeMethod {
        THIRTEEN_THIRTEEN(1),
        TWO_TWELVE(2),
        THREE_EIGHTEEN(3);

        public final int seaweedCount;

        SuperGlassMakeMethod(int seaweedCount) {
            this.seaweedCount = seaweedCount;
        }
    }
}

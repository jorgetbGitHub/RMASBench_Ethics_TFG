package rescuecore2.standard.entities;

/**
   Constants for the standard entity package.
*/
public final class StandardEntityConstants {
    /**
       Enum defining building codes.
    */
    public enum BuildingCode {
        /** Wooden construction. */
        WOOD,
        /** Steel frame construction. */
        STEEL,
        /** Reinforced concrete construction. */
        CONCRETE;
    }

    /**
       Enum defining different levels of fieryness.
     */
    public enum Fieryness {
        /** Not burnt at all. */
        UNBURNT(0),
        /** On fire a bit. */
        HEATING(1),
        /** On fire a bit more. */
        BURNING(2),
        /** On fire a lot. */
        INFERNO(3),
        /** Not burnt at all, but has water damage. */
        WATER_DAMAGE(4),
        /** Extinguished but minor damage. */
        MINOR_DAMAGE(5),
        /** Extinguished but moderate damage. */
        MODERATE_DAMAGE(6),
        /** Extinguished but major damage. */
        SEVERE_DAMAGE(7),
        /** Completely burnt out. */
        BURNT_OUT(8);
		
		private final int value;

        Fieryness(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
    }

    private StandardEntityConstants() {}
}

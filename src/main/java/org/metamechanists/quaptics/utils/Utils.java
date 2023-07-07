package org.metamechanists.quaptics.utils;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Utils {
    private static final double FLOATING_POINT_THRESHOLD = 0.00001;
    public static double roundTo2dp(final double value) {
        return Math.round(value*Math.pow(10, 2)) / Math.pow(10, 2);
    }

    public static void clampToRange(final @NotNull Vector vector, final float min, final float max) {
        vector.setX(Math.min(vector.getX(), max));
        vector.setY(Math.min(vector.getY(), max));
        vector.setZ(Math.min(vector.getZ(), max));

        vector.setX(Math.max(vector.getX(), min));
        vector.setY(Math.max(vector.getY(), min));
        vector.setZ(Math.max(vector.getZ(), min));
    }

    public static boolean equal(final double a, final double b) {
        return Math.abs(a - b) < FLOATING_POINT_THRESHOLD;
    }

    public static boolean equal(final float a, final float b) {
        return Math.abs(a - b) < FLOATING_POINT_THRESHOLD;
    }
}

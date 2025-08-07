package src.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;

public class Utils {

    // Sleep for a random time between min and max (ms)
    public static void sleep(int min, int max) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(min, max));
        } catch (InterruptedException ignored) {}
    }

    // Sleep until the given condition is true or timeout (ms) is reached
    public static boolean sleepUntil(BooleanSupplier condition, int timeout) {
        long start = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - start > timeout) {
                return false;
            }
            sleep(50, 100);
        }
        return true;
    }

    // Format elapsed time (ms) as HH:MM:SS
    public static String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    // Calculate XP/hr given start XP and elapsed ms
    public static int getXpPerHour(int startXp, int currentXp, long startTime, long now) {
        int gained = currentXp - startXp;
        double hours = (now - startTime) / 3600000.0;
        return hours > 0 ? (int) (gained / hours) : 0;
    }
}

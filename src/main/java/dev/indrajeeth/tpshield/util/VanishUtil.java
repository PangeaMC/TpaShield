package dev.indrajeeth.tpshield.util;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

/**
 * Detects whether a player is in vanish mode.
 *
 * Reads the standard {@code vanished} metadata key set by EssentialsX,
 * SuperVanish/PremiumVanish, CMI and other major vanish plugins, so vanished
 * players can be filtered out without a hard dependency on any of them.
 */
public final class VanishUtil {

    private VanishUtil() {}

    public static boolean isVanished(Player player) {
        if (player == null) return false;
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}

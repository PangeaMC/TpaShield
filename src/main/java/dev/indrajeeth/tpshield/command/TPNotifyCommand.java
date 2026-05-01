package dev.indrajeeth.tpshield.command;

import dev.indrajeeth.tpshield.TpShield;

/**
 * /tpnotify
 * Toggles the post-teleport rating notification on or off.
 * When OFF the player will not receive the "Rate your experience" prompt after teleporting.
 */
public class TPNotifyCommand extends ToggleCommandBase {
    public TPNotifyCommand(TpShield plugin) {
        super(plugin,
                "tpshield.tpa",
                plugin.getDatabaseManager()::isNotificationEnabled,
                plugin.getDatabaseManager()::setNotificationEnabled,
                "notify.enabled",
                "notify.disabled");
    }
}

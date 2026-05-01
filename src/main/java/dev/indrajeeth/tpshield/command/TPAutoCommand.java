package dev.indrajeeth.tpshield.command;

import dev.indrajeeth.tpshield.TpShield;

/**
 * /tpauto
 * Toggles auto-accept mode. When enabled, all incoming TPA requests from other
 * players are automatically accepted without requiring the player to interact.
 */
public class TPAutoCommand extends ToggleCommandBase {
    public TPAutoCommand(TpShield plugin) {
        super(plugin,
                "tpshield.auto",
                plugin.getDatabaseManager()::isAutoAcceptEnabled,
                plugin.getDatabaseManager()::setAutoAcceptEnabled,
                "auto.enabled",
                "auto.disabled");
    }
}

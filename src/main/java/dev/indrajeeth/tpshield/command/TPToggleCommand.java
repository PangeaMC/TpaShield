package dev.indrajeeth.tpshield.command;

import dev.indrajeeth.tpshield.TpShield;

public class TPToggleCommand extends ToggleCommandBase {
    public TPToggleCommand(TpShield plugin) {
        super(plugin,
                "tpshield.toggle",
                plugin.getDatabaseManager()::areRequestsEnabled,
                plugin.getDatabaseManager()::setRequestsEnabled,
                "toggle.enabled",
                "toggle.disabled");
    }
}

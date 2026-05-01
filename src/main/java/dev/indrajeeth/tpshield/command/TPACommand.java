package dev.indrajeeth.tpshield.command;

import dev.indrajeeth.tpshield.TpShield;

public class TPACommand extends SendRequestCommand {
    public TPACommand(TpShield plugin) {
        super(plugin,
                "tpshield.tpa",
                plugin.getTeleportManager()::sendRequest,
                "commands.tpa.usage",
                "commands.tpa.cannot-teleport-self",
                "requests.sent",
                "requests.received");
    }
}

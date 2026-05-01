package dev.indrajeeth.tpshield.command;

import dev.indrajeeth.tpshield.TpShield;

/**
 * /tpahere &lt;player&gt; — requests the target to teleport to the requester.
 * The target accepts/denies with the same /tpaccept and /tpdeny commands.
 */
public class TPAHereCommand extends SendRequestCommand {
    public TPAHereCommand(TpShield plugin) {
        super(plugin,
                "tpshield.tpahere",
                plugin.getTeleportManager()::sendHereRequest,
                "commands.tpahere.usage",
                "commands.tpahere.cannot-teleport-self",
                "requests.here-sent",
                "requests.here-received");
    }
}

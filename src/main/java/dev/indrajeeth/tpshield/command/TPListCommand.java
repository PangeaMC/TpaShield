package dev.indrajeeth.tpshield.command;

import dev.indrajeeth.tpshield.TpShield;
import dev.indrajeeth.tpshield.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TPListCommand extends SimpleCommandHandler {
    public TPListCommand(TpShield plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, configManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "tpshield.tpa")) return true;

        List<UUID> received = requestManager.getPendingRequestsFor(player.getUniqueId());
        List<UUID> sent = requestManager.getSentRequestsBy(player.getUniqueId());

        if (received.isEmpty() && sent.isEmpty()) {
            MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("requests.no-pending-requests"));
            return true;
        }

        MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("list.header"));

        for (UUID requesterId : received) {
            Player requester = Bukkit.getPlayer(requesterId);
            if (requester != null) {
                MessageUtil.sendMessageWithPlaceholders(player,
                        configManager.getMessage("list.received", Map.of("player", requester.getName())));
            }
        }

        for (UUID targetId : sent) {
            Player target = Bukkit.getPlayer(targetId);
            if (target != null) {
                MessageUtil.sendMessageWithPlaceholders(player,
                        configManager.getMessage("list.sent", Map.of("player", target.getName())));
            }
        }

        return true;
    }

}


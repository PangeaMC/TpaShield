package dev.indrajeeth.tpshield.command;

import dev.indrajeeth.tpshield.TpShield;
import dev.indrajeeth.tpshield.manager.TeleportRequestManager;
import dev.indrajeeth.tpshield.manager.TeleportRequestManager.RequestResult;
import dev.indrajeeth.tpshield.util.MessageUtil;
import dev.indrajeeth.tpshield.util.VanishUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.concurrent.CompletableFuture;

/**
 * Shared scaffolding for /tpa and /tpahere. Subclasses supply the permission
 * node, the request method, and the message keys for the on-the-wire flow.
 */
abstract class SendRequestCommand extends SimpleCommandHandler {

    private final String permission;
    private final BiFunction<Player, Player, CompletableFuture<RequestResult>> sender;
    private final String usageKey;
    private final String selfKey;
    private final String sentKey;
    private final String receivedKey;

    protected SendRequestCommand(TpShield plugin,
                                 String permission,
                                 BiFunction<Player, Player, CompletableFuture<RequestResult>> sender,
                                 String usageKey,
                                 String selfKey,
                                 String sentKey,
                                 String receivedKey) {
        super(plugin);
        this.permission  = permission;
        this.sender      = sender;
        this.usageKey    = usageKey;
        this.selfKey     = selfKey;
        this.sentKey     = sentKey;
        this.receivedKey = receivedKey;
    }

    @Override
    public boolean onCommand(CommandSender source, Command command, String label, String[] args) {
        if (!(source instanceof Player player)) {
            MessageUtil.sendMessage(source, configManager.getMessage("general.player-only"));
            return true;
        }

        if (args.length < 1) {
            MessageUtil.sendMessageWithPlaceholders(player,
                    configManager.getPrefix() + configManager.getMessage(usageKey));
            return true;
        }

        if (!checkPermission(player, permission)) return true;
        if (!checkNotInCombat(player)) return true;

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || VanishUtil.isVanished(target)) {
            MessageUtil.sendMessageWithPlaceholders(player,
                    configManager.getPrefix() + configManager.getMessage("general.player-not-found"));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessageUtil.sendMessageWithPlaceholders(player,
                    configManager.getPrefix() + configManager.getMessage(selfKey));
            return true;
        }

        sender.apply(player, target).thenAccept(result ->
            Bukkit.getScheduler().runTask(plugin, () -> handleResult(player, target, result)));

        return true;
    }

    private void handleResult(Player player, Player target, RequestResult result) {
        switch (result) {
            case SUCCESS, AUTO_ACCEPTED -> {
                MessageUtil.sendMessageWithPlaceholders(player,
                        configManager.getPrefix()
                        + configManager.getMessage(sentKey, Map.of("player", target.getName())));
                MessageUtil.sendMessageWithPlaceholders(target,
                        configManager.getPrefix()
                        + configManager.getMessage(receivedKey, Map.of("player", player.getName())));
            }
            case ALREADY_HAS_REQUEST -> MessageUtil.sendMessageWithPlaceholders(player,
                    configManager.getPrefix() + configManager.getMessage("requests.already-has-request"));
            case ON_COOLDOWN -> {
                long lastRequest = requestManager.getCooldown(player.getUniqueId());
                long cooldownMs  = configManager.getCooldown() * 1000L;
                long remaining   = (cooldownMs - (System.currentTimeMillis() - lastRequest)) / 1000;
                MessageUtil.sendMessageWithPlaceholders(player,
                        configManager.getPrefix()
                        + configManager.getMessage("cooldown.message",
                                Map.of("time", String.valueOf(remaining))));
            }
            case REQUESTS_DISABLED -> MessageUtil.sendMessageWithPlaceholders(player,
                    configManager.getPrefix()
                    + configManager.getMessage("toggle.target-has-tp-disabled",
                            Map.of("player", target.getName())));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender source, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterByPrefix(getOnlinePlayerNames(source), args[0]);
        }
        return List.of();
    }
}

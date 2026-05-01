package dev.indrajeeth.tpshield.command;

import dev.indrajeeth.tpshield.TpShield;
import dev.indrajeeth.tpshield.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Shared scaffolding for the boolean per-player toggle commands
 * (/tptoggle, /tpauto, /tpnotify). Each subclass supplies the permission node,
 * the DB getter/setter pair, and the message keys for the on/off states.
 */
abstract class ToggleCommandBase extends SimpleCommandHandler {

    private final String permission;
    private final Function<UUID, CompletableFuture<Boolean>> reader;
    private final BiFunction<UUID, Boolean, CompletableFuture<Void>> writer;
    private final String onMessageKey;
    private final String offMessageKey;

    protected ToggleCommandBase(TpShield plugin,
                                String permission,
                                Function<UUID, CompletableFuture<Boolean>> reader,
                                BiFunction<UUID, Boolean, CompletableFuture<Void>> writer,
                                String onMessageKey,
                                String offMessageKey) {
        super(plugin);
        this.permission    = permission;
        this.reader        = reader;
        this.writer        = writer;
        this.onMessageKey  = onMessageKey;
        this.offMessageKey = offMessageKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, configManager.getMessage("general.player-only"));
            return true;
        }
        if (!checkPermission(player, permission)) return true;

        UUID id = player.getUniqueId();
        reader.apply(id).thenAccept(current -> {
            boolean next = !current;
            writer.apply(id, next).thenRun(() ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    String key = next ? onMessageKey : offMessageKey;
                    MessageUtil.sendMessageWithPlaceholders(player,
                            configManager.getPrefix() + configManager.getMessage(key));
                })
            );
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}

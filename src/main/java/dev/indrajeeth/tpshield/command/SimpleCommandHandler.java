package dev.indrajeeth.tpshield.command;

import dev.indrajeeth.tpshield.TpShield;
import dev.indrajeeth.tpshield.manager.ConfigManager;
import dev.indrajeeth.tpshield.manager.TeleportRequestManager;
import dev.indrajeeth.tpshield.util.MessageUtil;
import dev.indrajeeth.tpshield.util.PermissionManager;
import dev.indrajeeth.tpshield.util.VanishUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SimpleCommandHandler implements CommandExecutor, TabCompleter {
    protected final TpShield plugin;
    protected final TeleportRequestManager requestManager;
    protected final ConfigManager configManager;

    public SimpleCommandHandler(TpShield plugin) {
        this.plugin = plugin;
        this.requestManager = plugin.getTeleportManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }

    /**
     * Checks the permission via LuckPerms (or Bukkit fallback).
     * Sends the configured "no-permission" message and returns {@code false} if denied.
     */
    protected boolean checkPermission(Player player, String node) {
        if (PermissionManager.hasPermission(player, node)) return true;
        MessageUtil.sendMessageWithPlaceholders(player,
                configManager.getPrefix() + configManager.getMessage("general.no-permission"));
        return false;
    }

    /**
     * Returns {@code false} and notifies the player when the combat check is
     * enabled, the player holds the {@code tpshield.incombat} permission (set by
     * an external combat plugin/script), and they do not hold
     * {@code tpshield.combat.bypass}.
     */
    protected boolean checkNotInCombat(Player player) {
        if (!configManager.isCombatEnabled()) return true;
        if (PermissionManager.hasPermission(player, "tpshield.combat.bypass")) return true;
        if (!PermissionManager.hasPermission(player, "tpshield.incombat")) return true;
        MessageUtil.sendMessageWithPlaceholders(player,
                configManager.getPrefix() + configManager.getMessage("combat.blocked"));
        return false;
    }

    protected List<String> getOnlinePlayerNames(CommandSender sender) {
        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : null;
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> senderUuid == null || !p.getUniqueId().equals(senderUuid))
                .filter(p -> !VanishUtil.isVanished(p))
                .map(Player::getName)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Returns the entries from {@code options} whose names start with the typed
     * {@code prefix}, case-insensitive. Used by tab-completers so the client
     * sees only matching suggestions instead of every online player.
     */
    protected List<String> filterByPrefix(List<String> options, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            List<String> sorted = new ArrayList<>(options);
            Collections.sort(sorted);
            return sorted;
        }
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(prefix, options, result);
        Collections.sort(result);
        return result;
    }
}


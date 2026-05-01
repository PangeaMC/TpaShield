package dev.indrajeeth.tpshield.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.indrajeeth.tpshield.TpShield;
import dev.indrajeeth.tpshield.manager.CommandManager;
import dev.indrajeeth.tpshield.util.VanishUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Registers all TpShield commands via Paper's Brigadier Lifecycle API.
 * This ensures the Minecraft client knows about every command, preventing
 * the "unknown command" confirmation dialog when clicking chat run_command
 * click events (e.g. the "[View Request]" notification button).
 */
@SuppressWarnings("UnstableApiUsage")
public final class BrigadierRegistrar {

    private final TpShield plugin;
    private final CommandManager commandManager;

    public BrigadierRegistrar(TpShield plugin, CommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands cmds = event.registrar();

            for (String name : List.of("tptoggle", "tplist",
                                       "tpnotify", "tpauto", "tprate")) {
                cmds.register(literalDelegate(name));
            }

            cmds.register(playerArgCommand("tpa",      onlinePlayersExcludingSelf()));
            cmds.register(playerArgCommand("tpahere",  onlinePlayersExcludingSelf()));
            cmds.register(playerArgCommand("tpcancel", sentRequestTargets()));
            cmds.register(playerArgCommand("tpaccept", pendingRequestSenders()));
            cmds.register(playerArgCommand("tpdeny",   pendingRequestSenders()));
            cmds.register(playerArgCommand("tpaview",  pendingRequestSenders()));
            cmds.register(playerArgCommand("tpstats",  onlinePlayers()));

            cmds.register(
                Commands.literal("tpshield")
                    .then(Commands.argument("sub", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if ("reload".startsWith(builder.getRemaining().toLowerCase()))
                                builder.suggest("reload");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            delegate(ctx.getSource().getSender(), "tpshield",
                                new String[]{StringArgumentType.getString(ctx, "sub")});
                            return 1;
                        }))
                    .executes(ctx -> { delegate(ctx.getSource().getSender(), "tpshield", new String[0]); return 1; })
                    .build()
            );
        });
    }

    /** Builds a no-argument literal that delegates to the legacy handler. */
    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> literalDelegate(String name) {
        return Commands.literal(name)
                .executes(ctx -> { delegate(ctx.getSource().getSender(), name, new String[0]); return 1; })
                .build();
    }

    /**
     * Builds a {@code /name <player>} command whose suggestions come from
     * {@code source}. Both the zero-arg and one-arg forms route through the
     * legacy handler, so existing CommandExecutor implementations stay in charge.
     */
    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack>
            playerArgCommand(String name, Function<CommandSender, Stream<Player>> source) {
        RequiredArgumentBuilder<CommandSourceStack, String> arg =
                Commands.argument("player", StringArgumentType.word())
                        .suggests(playerNameSuggester(source))
                        .executes(ctx -> {
                            delegate(ctx.getSource().getSender(), name,
                                    new String[]{StringArgumentType.getString(ctx, "player")});
                            return 1;
                        });
        return Commands.literal(name)
                .then(arg)
                .executes(ctx -> { delegate(ctx.getSource().getSender(), name, new String[0]); return 1; })
                .build();
    }

    private static SuggestionProvider<CommandSourceStack>
            playerNameSuggester(Function<CommandSender, Stream<Player>> source) {
        return (ctx, builder) -> {
            String prefix = builder.getRemaining().toLowerCase();
            CommandSender sender = ctx.getSource().getSender();
            source.apply(sender)
                    .filter(p -> !VanishUtil.isVanished(p))
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    private static Function<CommandSender, Stream<Player>> onlinePlayers() {
        return sender -> Bukkit.getOnlinePlayers().stream().map(p -> (Player) p);
    }

    private static Function<CommandSender, Stream<Player>> onlinePlayersExcludingSelf() {
        return sender -> {
            UUID self = sender instanceof Player p ? p.getUniqueId() : null;
            return Bukkit.getOnlinePlayers().stream()
                    .map(p -> (Player) p)
                    .filter(p -> self == null || !p.getUniqueId().equals(self));
        };
    }

    private Function<CommandSender, Stream<Player>> sentRequestTargets() {
        return sender -> {
            if (!(sender instanceof Player player)) return Stream.empty();
            return plugin.getTeleportManager()
                    .getSentRequestsBy(player.getUniqueId()).stream()
                    .map(Bukkit::getPlayer)
                    .filter(java.util.Objects::nonNull);
        };
    }

    private Function<CommandSender, Stream<Player>> pendingRequestSenders() {
        return sender -> {
            if (!(sender instanceof Player player)) return Stream.empty();
            return plugin.getTeleportManager()
                    .getPendingRequestsFor(player.getUniqueId()).stream()
                    .map(Bukkit::getPlayer)
                    .filter(java.util.Objects::nonNull);
        };
    }

    private void delegate(CommandSender sender, String name, String[] args) {
        SimpleCommandHandler handler = commandManager.getHandler(name);
        if (handler != null) {
            handler.onCommand(sender, null, name, args);
        } else {
            plugin.getLogger().warning("[BrigadierRegistrar] Command not found: /" + name);
        }
    }
}

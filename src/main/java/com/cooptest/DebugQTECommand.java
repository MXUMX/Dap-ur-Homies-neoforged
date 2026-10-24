package com.cooptest;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
public class DebugQTECommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("debugqte")
                        .executes(ctx -> executeSolo(ctx, 1))
                        .then(Commands.argument("stages", IntegerArgumentType.integer(1, 3))
                                .executes(ctx -> executeSolo(ctx, IntegerArgumentType.getInteger(ctx, "stages")))
                        )
        );
    }
    private static int executeSolo(CommandContext<CommandSourceStack> context, int stages) {
        CommandSourceStack source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can use this command!"));
            return 0;
        }
        if (QTEManager.isInQTE(player.getUUID())) {
            com.cooptest.neoforge.PlayerActions.message(player, Component.literal("§c§lAlready in a QTE!"), false);
            return 0;
        }
        com.cooptest.neoforge.PlayerActions.message(player, Component.literal("§a§l[DEBUG] Starting " + stages + "-stage QTE in SOLO MODE!"), false);
        com.cooptest.neoforge.PlayerActions.message(player, Component.literal("§e§lPress the button that appears!"), false);
        QTEManager.triggerQTESolo(
                player,
                stages,
                (p1, p2) -> {
                    com.cooptest.neoforge.PlayerActions.message(p1, Component.literal("§d§l★ ALL STAGES COMPLETE! ★"), true);
                    com.cooptest.neoforge.PlayerActions.message(p1, Component.literal("§a§lExtender animation would play here!"), false);// PLS WORKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
                },
                (p1, p2) -> {
                    com.cooptest.neoforge.PlayerActions.message(p1, Component.literal("§c§l✖ QTE FAILED! ✖"), true);
                    com.cooptest.neoforge.PlayerActions.message(p1, Component.literal("§7Better luck next time!"), false);
                },
                (p1, p2, completedStage) -> {
                    com.cooptest.neoforge.PlayerActions.message(p1, Component.literal("§a§l✓ Stage " + completedStage + " clear!"), false);
                }
        );
        return 1;
    }
}
package com.github.vaerys.trainer_connection.server.commands;

import com.github.vaerys.trainer_connection.server.battle.BattleUtils;
import com.github.vaerys.trainer_connection.server.data.DataRegister;
import com.github.vaerys.trainer_connection.server.data.TrainerData;
import com.github.vaerys.trainer_connection.server.states.ChallengerLink;
import com.github.vaerys.trainer_connection.server.states.ChallengerStateHandler;
import com.github.vaerys.trainer_connection.server.states.TrainerLink;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TCDebugCommand {

    public static int runResetTrainer(CommandContext<ServerCommandSource> context, String trainerId) {
        MinecraftServer server = context.getSource().getServer();
        ChallengerStateHandler handler = ChallengerStateHandler.getServerState(server);
        if (handler.trainerExists(trainerId)) {
            handler.removeTrainer(trainerId);
            context.getSource().sendFeedback(() -> Text.literal("Cleared all data for trainer: " + trainerId), false);
        } else {
            context.getSource().sendError(Text.literal("Could not find trainerID: " + trainerId));
        }
        return 0;
    }

    public static int runResetPlayer(CommandContext<ServerCommandSource> context, String trainerId, ServerPlayerEntity player) {
        MinecraftServer server = context.getSource().getServer();
        TrainerLink trainerLink = ChallengerStateHandler.getTrainerState(server, trainerId);
        if (trainerLink == null) {
            context.getSource().sendError(Text.literal("Could not find trainerID: " + trainerId));
        } else {
            trainerLink.removeChallenger(player.getGameProfile().getId());
            context.getSource().sendFeedback(() -> Text.literal("Cleared Challenger Data from trainer: " + trainerId + " for player: " + player.getDisplayName().getString()), false);
        }
        return 0;
    }

    public static int runResetPlayerALl(CommandContext<ServerCommandSource> context, ServerPlayerEntity resetPlayer) {
        MinecraftServer server = context.getSource().getServer();
        ChallengerStateHandler handler = ChallengerStateHandler.getServerState(server);
        handler.getAll().forEach((s, trainerLink) -> trainerLink.removeChallenger(resetPlayer.getGameProfile().getId()));
        context.getSource().sendFeedback(() -> Text.literal("Cleared Challenger Data from all trainers for player: " + resetPlayer.getDisplayName().getString()), false);
        return 0;
    }

//    public static int runDebugTest(CommandContext<ServerCommandSource> context, String rest) {
//        context.getSource().sendFeedback(() -> Text.literal("you said: " + rest), false);
//        return 0;
//    }

    public static int doModifyAttempts(CommandContext<ServerCommandSource> context, String trainerId, ServerPlayerEntity player, String modifier, int value, boolean isRematch) {
        ChallengerStateHandler handler = ChallengerStateHandler.getServerState(context.getSource().getServer());
        TrainerLink link = handler.getOrCreateTrainer(trainerId);
        ChallengerLink challengerLink = link.getOrCreateChallenger(player);

        int oldAttempts = isRematch ? challengerLink.rematchAttempts : challengerLink.attempts;
        int newVal = oldAttempts;
        switch (modifier.toLowerCase()) {
            case "add":
                newVal += value;
                break;
            case "remove":
                newVal -= value;
                break;
            case "set":
                newVal = value;
                break;
        }
        if (isRematch) {
            challengerLink.rematchAttempts = newVal;
        } else challengerLink.attempts = newVal;

        context.getSource().sendFeedback(() -> Text.literal(
                String.format("%s %s attempts for: %s was %d is now %d", trainerId, isRematch ? "rematch" : "battle", player.getDisplayName().getString(), oldAttempts, challengerLink.attempts)
        ), false);
        return 0;
    }

    public static int disableBattle(CommandContext<ServerCommandSource> context, String trainerId, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        TrainerLink link = ChallengerStateHandler.getTrainerState(server, trainerId);
        ChallengerLink challengerLink = link.getOrCreateChallenger(player);
        challengerLink.battleUnlocked = false;
        context.getSource().sendError(Text.literal("Battles have been locked for player: " + player.getDisplayName().getString() + "for ncp: " + trainerId));
        return 0;
    }

    public static int enableBattle(CommandContext<ServerCommandSource> context, String trainerId, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        TrainerLink link = ChallengerStateHandler.getTrainerState(server, trainerId);
        ChallengerLink challengerLink = link.getOrCreateChallenger(player);
        challengerLink.battleUnlocked = true;
        context.getSource().sendError(Text.literal("Battles have been unlocked for player: " + player.getDisplayName().getString() + "for ncp: " + trainerId));
        return 0;
    }

    public static int disableRematch(CommandContext<ServerCommandSource> context, String trainerId, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        TrainerLink link = ChallengerStateHandler.getTrainerState(server, trainerId);
        ChallengerLink challengerLink = link.getOrCreateChallenger(player);
        challengerLink.rematchUnlocked = false;
        context.getSource().sendError(Text.literal("Rematches have been locked for player: " + player.getDisplayName().getString() + "for ncp: " + trainerId));
        return 0;
    }

    public static int enableRematch(CommandContext<ServerCommandSource> context, String trainerId, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        TrainerLink link = ChallengerStateHandler.getTrainerState(server, trainerId);
        ChallengerLink challengerLink = link.getOrCreateChallenger(player);
        challengerLink.rematchUnlocked = true;
        context.getSource().sendError(Text.literal("Rematches have been unlocked for player: " + player.getDisplayName().getString() + "for ncp: " + trainerId));
        return 0;
    }

    public static int runStopBattle(CommandContext<ServerCommandSource> context, String trainerId) {
        MinecraftServer server = context.getSource().getServer();
        TrainerLink link = ChallengerStateHandler.getTrainerState(server, trainerId);
        Entity entity = Utils.getEntityFromID(link.lastKnownEntityID, server);
        TrainerData data = DataRegister.trainerDataList.get(trainerId);
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(link.currentChallenger);
        BattleUtils.handleAbandon(entity, trainerId, link, data, player, false);
        context.getSource().sendError(Text.literal("Manually Stopping Battle for: " + trainerId));
        return 0;
    }
}

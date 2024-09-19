package com.github.vaerys.trainer_connection.server.commands;

import com.github.vaerys.trainer_connection.common.Constants;
import com.github.vaerys.trainer_connection.server.battle.BattleUtils;
import com.github.vaerys.trainer_connection.server.data.DataRegister;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TrainerConnectionCommand {

    public static int doInteract(CommandContext<ServerCommandSource> context, String trainerId, Entity entity, ServerPlayerEntity player) {
        if (DataRegister.trainerDataList.containsKey(trainerId)) {
            Utils.updateLastKnownEntity(trainerId, entity, player.server);
            PacketByteBuf buf = Utils.deserializeClientPacket(trainerId, player, player.server);
            ServerPlayNetworking.send(player, Constants.NPC_TC_INTERFACE_PACKET_ID, buf);
            context.getSource().sendFeedback(() -> Text.literal("Sent Packet: " + trainerId), false);
        } else {
            context.getSource().sendError(Text.literal("Could not find trainerID: " + trainerId));
        }
        return 0;
    }

    public static int doChallengerWin(CommandContext<ServerCommandSource> context, String trainerId, Entity entity, ServerPlayerEntity player, boolean isRematch) {
        if (DataRegister.trainerDataList.containsKey(trainerId)) {
            BattleUtils.handleWin(trainerId, entity, player, isRematch);
            context.getSource().sendFeedback(() -> Text.literal("Sent Win"), false);
        } else {
            context.getSource().sendError(Text.literal("Could not find trainerID: " + trainerId));
        }
        return 0;
    }

    public static int doChallengerLose(CommandContext<ServerCommandSource> context, String trainerId, Entity entity, ServerPlayerEntity player, boolean isRematch) {
        if (DataRegister.trainerDataList.containsKey(trainerId)) {
            BattleUtils.handleLose(trainerId, entity, player, isRematch);
            context.getSource().sendFeedback(() -> Text.literal("Sent Loss"), false);
        } else {
            context.getSource().sendError(Text.literal("Could not find trainerID: " + trainerId));
        }
        return 0;
    }

    public static int doTest(CommandContext<ServerCommandSource> context, String trainerId, Entity entity, ServerPlayerEntity player) {
        NbtCompound compound = new NbtCompound();

        // serialize player
        NbtCompound playerData = new NbtCompound();
        NbtHelper.writeGameProfile(playerData, player.getGameProfile());
        compound.put("player", playerData);
        // deserialize player
        GameProfile profile = NbtHelper.toGameProfile(compound.getCompound("player"));


        context.getSource().sendFeedback(() -> Text.literal("Num: " + DataRegister.trainerDataList.size()), false);
        return 0;
    }
}

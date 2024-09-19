package com.github.vaerys.trainer_connection.server.commands;

import com.github.vaerys.trainer_connection.common.Constants;
import com.github.vaerys.trainer_connection.server.data.TrainerData;
import com.github.vaerys.trainer_connection.server.states.ChallengerLink;
import com.github.vaerys.trainer_connection.server.states.ChallengerStateHandler;
import com.github.vaerys.trainer_connection.server.states.TrainerLink;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.vaerys.trainer_connection.NPCTrainerConnection.LOGGER;

public class Utils {

    public static int generateTemplate(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Generating template...\nYour \"npctc_template.json\" file should be found in the root folder of your instance."), true);

        TrainerData data = TrainerData.DEFAULT;

        try (FileWriter writer = new FileWriter("npctc_template.json")) {
            DataResult<JsonElement> result = TrainerData.CODEC.encodeStart(JsonOps.INSTANCE, data);
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(result.resultOrPartial(LOGGER::error).orElseThrow()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return 1;
    }

    public static PacketByteBuf deserializeClientPacket(String trainerId, ServerPlayerEntity player, MinecraftServer server) {
        TrainerLink link = ChallengerStateHandler.getTrainerState(server, trainerId);
        return ClientPacket.fromLink(trainerId, player, link);
    }

    public static void serializeClientPacket(PacketByteBuf buf, MinecraftServer server) {
        ClientPacket packet = new ClientPacket(buf);
        TrainerLink link = ChallengerStateHandler.getTrainerState(server, packet.trainerID);
        link.updateFromPacket(packet);
    }

    public static void sendTrainerMessage(PlayerEntity player, TrainerData data, String message) {
        MutableText name = Text.literal(data.getDisplayName());
        name = name.setStyle(Style.EMPTY.withColor(TextColor.parse(data.getDisplayColor())));
        MutableText text = Text.literal(message);
        player.sendMessage(MutableText.of(TextContent.EMPTY).append("<").append(name).append("> ").append(text));
    }

    public static String applyReplaceTags(String message, PlayerEntity player, TrainerData data, ChallengerLink challengerLink, boolean isRematch) {
        ItemStack unlockItem;
        if (isRematch) {
            unlockItem = data.getRematchData().getUnlockItem().getItem();
        } else {
            unlockItem = data.getBattleData().getUnlockItem().getItem();
        }

        message = message.replace("%player%", player.getDisplayName().getString());
        message = message.replace("%attempts%", challengerLink.attempts + "");
        message = message.replace("%losses%", challengerLink.losses + "");
        message = message.replace("%rematch_attempts%", challengerLink.attempts + "");
        message = message.replace("%rematch_losses%", challengerLink.rematchLosses + "");
        message = message.replace("%rematch_wins%", challengerLink.rematchWins + "");
        message = message.replace("%unlock_item%", unlockItem.getName().getString());
        message = message.replace("%amount_current%", player.getInventory().count(unlockItem.getItem()) + "");
        message = message.replace("%amount_required%", unlockItem.getCount() + "");
        return message;
    }

    public static void updateLastKnownEntity(String trainerId, Entity entity, MinecraftServer server) {
        TrainerLink link = ChallengerStateHandler.getTrainerState(server, trainerId);
        link.lastKnownEntityID = entity.getUuid();
        if (link.trainerState.equals(Constants.TRAINER_STATE_IDLE)) {
            link.fallbackPosition = entity.getBlockPos();
        }
    }

    public static Entity getEntityFromID(UUID entityID, MinecraftServer server) {
        AtomicReference<Entity> entity = new AtomicReference<>();
        for (ServerWorld serverWorld : server.getWorlds()) {
            Entity e = serverWorld.getEntity(entityID);
            if (e != null) {
                entity.set(e);
                break;
            }
        }
        return entity.get();
    }
}
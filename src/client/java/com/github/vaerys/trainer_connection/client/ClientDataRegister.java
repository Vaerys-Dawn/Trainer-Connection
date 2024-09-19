package com.github.vaerys.trainer_connection.client;

import com.github.vaerys.trainer_connection.server.data.TrainerData;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;

public class ClientDataRegister {
    public static Map<String, TrainerData> trainerDataList = new HashMap<>();

    public static void receiveDatapackResources(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        // reset on first load
        trainerDataList.clear();
        NbtCompound nbt = buf.readNbt();
        for (String trainerID : nbt.getKeys()) {
            DataResult<Pair<TrainerData, NbtElement>> decode = TrainerData.CODEC.decode(NbtOps.INSTANCE, nbt.get(trainerID));
            Pair<TrainerData, NbtElement> trainerDataPair = decode.result().orElse(null);
            if (trainerDataPair == null) return;
            trainerDataList.put(trainerID, trainerDataPair.getFirst());
        }
    }
}

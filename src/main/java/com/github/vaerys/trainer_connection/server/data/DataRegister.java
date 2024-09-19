package com.github.vaerys.trainer_connection.server.data;

import com.github.vaerys.trainer_connection.NPCTrainerConnection;
import com.github.vaerys.trainer_connection.common.Constants;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.vaerys.trainer_connection.NPCTrainerConnection.LOGGER;

public class DataRegister {
    public static Map<String, TrainerData> trainerDataList = new HashMap<>();
    public static final String pathId = "trainerdata";

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(NPCTrainerConnection.MOD_ID, pathId);
            }

            @Override
            public void reload(ResourceManager manager) {
                // only run on server

                trainerDataList.clear();
                Map<Identifier, Resource> data = manager.findResources(pathId, identifier -> identifier.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : data.entrySet()) {
                    try (InputStream stream = entry.getValue().getInputStream()) {
                        Reader streamReader = new InputStreamReader(stream);
                        JsonElement json = JsonParser.parseReader(streamReader);
                        DataResult<TrainerData> result = TrainerData.CODEC.parse(JsonOps.INSTANCE, json);
                        TrainerData trainerData = result.resultOrPartial(LOGGER::error).orElseThrow();
                        String id = entry.getKey().getPath().replace(pathId + "/", "").replace(".json", "");
                        if (trainerData != null) trainerDataList.put(id, trainerData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // todo skip file for now, add error handling
                    }
                }
            }
        });
    }

    public static void syncClientsAfterDatapackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : playerList) {
            sendPackets(player);
        }
    }

    public static void sendPackets(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        NbtCompound nbt = new NbtCompound();
        for (Map.Entry<String, TrainerData> entry : trainerDataList.entrySet()) {
            String trainerId = entry.getKey();
            TrainerData trainerData = entry.getValue();

            DataResult<NbtElement> result = TrainerData.CODEC.encodeStart(NbtOps.INSTANCE, trainerData);
            NbtCompound compound = (NbtCompound) result.result().orElse(new NbtCompound());
            nbt.put(trainerId, compound);
        }
        buf.writeNbt(nbt);
        ServerPlayNetworking.send(player, Constants.NPC_TC_DATAPACK_SYNC, buf);
    }
}

package com.github.vaerys.trainer_connection.server.data;

import com.github.vaerys.trainer_connection.NPCTrainerConnection;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerRegistry;

import java.io.*;
import java.util.HashMap;
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
}

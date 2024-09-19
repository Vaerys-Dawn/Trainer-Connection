package com.github.vaerys.trainer_connection.server.states;

import com.github.vaerys.trainer_connection.NPCTrainerConnection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.*;

public class ChallengerStateHandler extends PersistentState {

    private HashMap<String, TrainerLink> trainerLinks;

    public ChallengerStateHandler() {
        trainerLinks = new HashMap<>();
    }

    private ChallengerStateHandler(HashMap<String, TrainerLink> trainerLinks) {
        this.trainerLinks = trainerLinks;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {

        NbtCompound challengerData = new NbtCompound();
        for (Map.Entry<String, TrainerLink> t : trainerLinks.entrySet()) {
            challengerData.put(String.valueOf(t.getKey()), TrainerLink.serialize(t.getValue()));
        }
        nbt.put("challengerData", challengerData);
        return nbt;
    }

    public static ChallengerStateHandler createFromNbt(NbtCompound nbt) {
        HashMap<String, TrainerLink> links = new HashMap<>();
        NbtCompound challengerData = nbt.getCompound("challengerData");
        for (String s : challengerData.getKeys()) {
            links.put(s, TrainerLink.deSerialize(challengerData.getCompound(s)));
        }
        return new ChallengerStateHandler(links);
    }

    public static ChallengerStateHandler getServerState(MinecraftServer server) {
        // (Note: arbitrary choice to use 'World.OVERWORLD' instead of 'World.END' or 'World.NETHER'.  Any work)
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        // The first time the following 'getOrCreate' function is called, it creates a brand new 'StateSaverAndLoader' and
        // stores it inside the 'PersistentStateManager'. The subsequent calls to 'getOrCreate' pass in the saved
        // 'StateSaverAndLoader' NBT on disk to our function 'StateSaverAndLoader::createFromNbt'.

        ChallengerStateHandler state = persistentStateManager.getOrCreate(ChallengerStateHandler::createFromNbt, ChallengerStateHandler::new, NPCTrainerConnection.MOD_ID);

        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.markDirty();

        return state;
    }

    public static TrainerLink getTrainerState(MinecraftServer server, String trainerID) {
        ChallengerStateHandler handler = getServerState(server);
        return handler.getOrCreateTrainer(trainerID);
    }

    public TrainerLink getOrCreateTrainer(String trainerId) {
        if (trainerLinks.containsKey(trainerId)) {
            return trainerLinks.get(trainerId);
        } else {
            TrainerLink trainerLink = new TrainerLink();
            trainerLinks.put(trainerId, trainerLink);
            return trainerLink;
        }
    }

    public HashMap<String, TrainerLink> getAll() {
        return trainerLinks;
    }

    public boolean trainerExists(String trainerId) {
        return trainerLinks.containsKey(trainerId);
    }

    public void removeTrainer(String trainerId) {
        trainerLinks.remove(trainerId);
    }
}

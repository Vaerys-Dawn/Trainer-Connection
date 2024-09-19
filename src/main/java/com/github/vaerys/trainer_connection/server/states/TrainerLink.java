package com.github.vaerys.trainer_connection.server.states;

import com.github.vaerys.trainer_connection.common.Constants;
import com.github.vaerys.trainer_connection.server.commands.ClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrainerLink {

    private HashMap<UUID, ChallengerLink> challengers;
    public String trainerState;
    public UUID currentChallenger;
    public UUID lastKnownEntityID;
    public BlockPos fallbackPosition;

    private static final String KEY_CHALLENGERS = "challengers";
    private static final String KEY_TRAINER_STATE = "trainer_state";
    private static final String KEY_CURRENT_CHALLENGER = "current_challenger";
    private static final String KEY_LAST_KNOWN_ENTITY_ID = "last_known_entity_id";
    private static final String KEY_FALLBACK_POSITION = "fallback_position";

    public void updateFromPacket(ClientPacket packet) {
        this.trainerState = packet.trainerState;
        this.currentChallenger = packet.currentChallenger;
        this.lastKnownEntityID = packet.lastKnownEntityID;
        this.fallbackPosition = packet.fallbackPosition;
        challengers.put(packet.challengerID, packet.challengerLink);
    }

    public TrainerLink() {
        this.challengers = new HashMap<>();
        this.trainerState = Constants.TRAINER_STATE_IDLE;
        this.currentChallenger = new UUID(0L, 0L);
        this.lastKnownEntityID = new UUID(0L, 0L);
        this.fallbackPosition = BlockPos.ORIGIN;
    }

    private TrainerLink(HashMap<UUID, ChallengerLink> challengerLinks, String trainerState, UUID currentChallenger, UUID lastKnownEntityID, BlockPos fallbackPosition) {
        this.challengers = challengerLinks;
        this.trainerState = trainerState;
        this.currentChallenger = currentChallenger;
        this.lastKnownEntityID = lastKnownEntityID;
        this.fallbackPosition = fallbackPosition;
    }

    public static NbtCompound serialize(TrainerLink trainerLink) {
        NbtCompound compound = new NbtCompound();
        NbtCompound challengerList = new NbtCompound();
        for (Map.Entry<UUID, ChallengerLink> c : trainerLink.challengers.entrySet()) {
            challengerList.put(String.valueOf(c.getKey()), ChallengerLink.serialize(c.getValue()));
        }
        compound.put(KEY_CHALLENGERS, challengerList);
        compound.putString(KEY_TRAINER_STATE, trainerLink.trainerState);
        compound.putUuid(KEY_CURRENT_CHALLENGER, trainerLink.currentChallenger);
        compound.putUuid(KEY_LAST_KNOWN_ENTITY_ID, trainerLink.lastKnownEntityID);
        compound.putLong(KEY_FALLBACK_POSITION, trainerLink.fallbackPosition.asLong());
        return compound;
    }

    public static TrainerLink deSerialize(NbtCompound compound) {
        HashMap<UUID, ChallengerLink> challengerLinks;
        String trainerState;
        UUID currentChallenger;
        UUID lastKnownEntityID;
        BlockPos fallbackPosition;

        if (compound.contains(KEY_CHALLENGERS)) {
            NbtCompound challengerList = compound.getCompound(KEY_CHALLENGERS);
            challengerLinks = new HashMap<>();
            for (String k : challengerList.getKeys()) {
                challengerLinks.put(UUID.fromString(k), ChallengerLink.deSerialize(challengerList.getCompound(k)));
            }
        } else {
            challengerLinks = new HashMap<>();
        }


        if (compound.contains(KEY_TRAINER_STATE)) trainerState = compound.getString(KEY_TRAINER_STATE);
        else trainerState = Constants.TRAINER_STATE_IDLE;

        if (compound.contains(KEY_CURRENT_CHALLENGER)) currentChallenger = compound.getUuid(KEY_CURRENT_CHALLENGER);
        else currentChallenger = new UUID(0L, 0L);

        if (compound.contains(KEY_LAST_KNOWN_ENTITY_ID)) lastKnownEntityID = compound.getUuid(KEY_LAST_KNOWN_ENTITY_ID);
        else lastKnownEntityID = new UUID(0L, 0L);

        if (compound.contains(KEY_FALLBACK_POSITION))
            fallbackPosition = BlockPos.fromLong(compound.getLong(KEY_FALLBACK_POSITION));
        else fallbackPosition = BlockPos.ORIGIN;

        return new TrainerLink(challengerLinks, trainerState, currentChallenger, lastKnownEntityID, fallbackPosition);
    }

    public ChallengerLink getOrCreateChallenger(ServerPlayerEntity player) {
        if (challengers.containsKey(player.getGameProfile().getId())) {
            return challengers.get(player.getGameProfile().getId());
        } else {
            ChallengerLink link = new ChallengerLink();
            challengers.put(player.getGameProfile().getId(), link);
            return link;
        }
    }

    public void removeChallenger(UUID id) {
        if (challengers.containsKey(id)) challengers.remove(id);
    }
}

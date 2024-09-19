package com.github.vaerys.trainer_connection.server.commands;

import com.github.vaerys.trainer_connection.server.states.ChallengerLink;
import com.github.vaerys.trainer_connection.server.states.TrainerLink;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class ClientPacket {
    public String trainerID;
    public UUID challengerID;
    public String trainerState;
    public UUID currentChallenger;
    public UUID lastKnownEntityID;
    public BlockPos fallbackPosition;
    public ChallengerLink challengerLink;

    public ClientPacket(PacketByteBuf buf) {
        this.trainerID = buf.readString();
        this.challengerID = buf.readUuid();
        this.trainerState = buf.readString();
        this.currentChallenger = buf.readUuid();
        this.lastKnownEntityID = buf.readUuid();
        this.fallbackPosition = buf.readBlockPos();
        this.challengerLink = ChallengerLink.fromBuffer(buf);
    }

    public static PacketByteBuf fromLink(String trainerID, ServerPlayerEntity entity, TrainerLink link) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(trainerID);
        buf.writeUuid(entity.getGameProfile().getId());
        buf.writeString(link.trainerState);
        buf.writeUuid(link.currentChallenger);
        buf.writeUuid(link.lastKnownEntityID);
        buf.writeBlockPos(link.fallbackPosition);
        ChallengerLink.toBuffer(buf, link.getOrCreateChallenger(entity));
        return buf;
    }

    public static PacketByteBuf toBuf(ClientPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(packet.trainerID);
        buf.writeUuid(packet.challengerID);
        buf.writeString(packet.trainerState);
        buf.writeUuid(packet.currentChallenger);
        buf.writeUuid(packet.lastKnownEntityID);
        buf.writeBlockPos(packet.fallbackPosition);
        ChallengerLink.toBuffer(buf, packet.challengerLink);
        return buf;
    }
}

package com.github.vaerys.trainer_connection.server.states;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;

public class ChallengerLink {
    public int attempts;
    public int losses;
    public boolean battleWon;
    public int abandons;
    public int rematchAttempts;
    public int rematchLosses;
    public int rematchWins;
    public int rematchAbandons;
    public boolean battleUnlocked;
    public boolean rematchUnlocked;


    /**
     * order of entries must be maintained or errors will occur
     */

    public static ChallengerLink fromBuffer(PacketByteBuf buf) {
        int attempts = buf.readInt();
        int losses = buf.readInt();
        boolean battleWon = buf.readBoolean();
        int abandons = buf.readInt();
        int rematchAttempts = buf.readInt();
        int rematchLosses = buf.readInt();
        int rematchWins = buf.readInt();
        int rematchAbandons = buf.readInt();
        boolean battleUnlocked = buf.readBoolean();
        boolean rematchUnlocked = buf.readBoolean();
        return new ChallengerLink(attempts, losses, battleWon, abandons, rematchAttempts, rematchLosses, rematchWins, rematchAbandons, battleUnlocked, rematchUnlocked);
    }

    /**
     * order of entries must be maintained or errors will occur
     */
    public static PacketByteBuf toBuffer(PacketByteBuf buf, ChallengerLink link) {
        buf.writeInt(link.attempts);
        buf.writeInt(link.losses);
        buf.writeBoolean(link.battleWon);
        buf.writeInt(link.abandons);
        buf.writeInt(link.rematchAttempts);
        buf.writeInt(link.rematchLosses);
        buf.writeInt(link.rematchWins);
        buf.writeInt(link.rematchAbandons);
        buf.writeBoolean(link.battleUnlocked);
        buf.writeBoolean(link.rematchUnlocked);
        return buf;
    }

    public ChallengerLink() {
        attempts = 0;
        losses = 0;
        battleWon = false;
        abandons = 0;
        rematchAttempts = 0;
        rematchLosses = 0;
        rematchAbandons = 0;
        battleUnlocked = false;
        rematchUnlocked = false;
        rematchWins = 0;
    }

    public ChallengerLink(int attempts, int losses, boolean battleWon, int abandons, int rematchAttempts, int rematchLosses, int rematchWins, int rematchAbandons, boolean battleUnlocked, boolean rematchUnlocked) {
        this.attempts = attempts;
        this.losses = losses;
        this.battleWon = battleWon;
        this.abandons = abandons;
        this.rematchAttempts = rematchAttempts;
        this.rematchLosses = rematchLosses;
        this.rematchWins = rematchWins;
        this.rematchAbandons = rematchAbandons;
        this.battleUnlocked = battleUnlocked;
        this.rematchUnlocked = rematchUnlocked;
    }

    public static ChallengerLink fromNBT(NbtCompound compound) {
        int attempts;
        int losses;
        boolean battleWon;
        int abandons;
        int rematchAttempts;
        int rematchLosses;
        int rematchWins;
        int rematchAbandons;
        boolean battleUnlocked;
        boolean rematchUnlocked;

        if (compound.contains("attempts")) attempts = compound.getInt("attempts");
        else attempts = 0;
        if (compound.contains("losses")) losses = compound.getInt("losses");
        else losses = 0;
        if (compound.contains("battle_won")) battleWon = compound.getBoolean("battle_won");
        else battleWon = false;
        if (compound.contains("abandons")) abandons = compound.getInt("abandons");
        else abandons = 0;
        if (compound.contains("rematch_attempts")) rematchAttempts = compound.getInt("rematch_attempts");
        else rematchAttempts = 0;
        if (compound.contains("rematch_losses")) rematchLosses = compound.getInt("rematch_losses");
        else rematchLosses = 0;
        if (compound.contains("rematch_abandons")) rematchAbandons = compound.getInt("rematch_abandons");
        else rematchAbandons = 0;
        if (compound.contains("battle_unlocked")) battleUnlocked = compound.getBoolean("battle_unlocked");
        else battleUnlocked = false;
        if (compound.contains("rematch_unlocked")) rematchUnlocked = compound.getBoolean("rematch_unlocked");
        else rematchUnlocked = false;
        if (compound.contains("rematch_wins")) rematchWins = compound.getInt("rematch_wins");
        else rematchWins = 0;
        return new ChallengerLink(attempts, losses, battleWon, abandons, rematchAttempts, rematchLosses, rematchWins, rematchAbandons, battleUnlocked, rematchUnlocked);
    }

    public static NbtElement serialize(ChallengerLink c) {
        NbtCompound compound = new NbtCompound();
        compound.putInt("attempts", c.attempts);
        compound.putInt("losses", c.losses);
        compound.putBoolean("battle_won", c.battleWon);
        compound.putInt("abandons", c.abandons);
        compound.putInt("rematch_attempts", c.rematchAttempts);
        compound.putInt("rematch_losses", c.rematchLosses);
        compound.putInt("rematch_wins", c.rematchWins);
        compound.putInt("rematch_abandons", c.rematchAbandons);
        compound.putBoolean("battle_unlocked", c.battleUnlocked);
        compound.putBoolean("rematch_unlocked", c.rematchUnlocked);
        return compound;
    }

    public static ChallengerLink deSerialize(NbtCompound compound) {
        return ChallengerLink.fromNBT(compound);
    }
}

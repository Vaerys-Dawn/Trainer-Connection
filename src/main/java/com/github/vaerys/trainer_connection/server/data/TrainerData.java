package com.github.vaerys.trainer_connection.server.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class TrainerData {

    public static final TrainerData DEFAULT = new TrainerData("NULL", "#FFFFFF", BattleData.DEFAULT, false, false, BattleData.DEFAULT, false, TeleportData.DEFAULT);
    public static Codec<TrainerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").orElseGet(() -> "NULL").forGetter(TrainerData::getDisplayName),
            Codec.STRING.fieldOf("name_color").orElseGet(() -> "#FFFFFF").forGetter(TrainerData::getDisplayColor),
            BattleData.CODEC.fieldOf("battle_data").orElseGet(() -> BattleData.DEFAULT).forGetter(TrainerData::getBattleData),
            Codec.BOOL.fieldOf("rematch_enabled").orElseGet(() -> false).forGetter(TrainerData::isRematchEnabled),
            Codec.BOOL.fieldOf("lock_rematch_on_win").orElseGet(() -> false).forGetter(TrainerData::isLockRematchOnWin),
            BattleData.CODEC.fieldOf("rematch_data").orElseGet(() -> BattleData.DEFAULT).forGetter(TrainerData::getRematchData),
            Codec.BOOL.fieldOf("teleport_enabled").orElseGet(() -> false).forGetter(TrainerData::isTeleportEnabled),
            TeleportData.CODEC.fieldOf("teleports").orElseGet(() -> TeleportData.DEFAULT).forGetter(TrainerData::getTeleportData)
    ).apply(instance, TrainerData::new));


    private final String displayName;
    private final String displayColor;
    private final BattleData battleData;
    private final boolean rematchEnabled;
    private final boolean lockRematchOnWin;
    private final BattleData rematchData;
    private final boolean teleportEnabled;

    private final TeleportData teleportData;

    public TrainerData(String displayName, String displayColor, BattleData battleData, boolean rematchEnabled, boolean lockRematchOnWin, BattleData rematchData, boolean teleportEnabled, TeleportData teleportData) {
        this.displayName = displayName;
        this.displayColor = displayColor;
        this.battleData = battleData;
        this.rematchEnabled = rematchEnabled;
        this.lockRematchOnWin = lockRematchOnWin;
        this.rematchData = rematchData;
        this.teleportEnabled = teleportEnabled;
        this.teleportData = teleportData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayColor() {
        return displayColor;
    }

    public BattleData getBattleData() {
        return battleData;
    }

    public boolean isRematchEnabled() {
        return rematchEnabled;
    }

    public boolean isLockRematchOnWin() {
        return lockRematchOnWin;
    }

    public BattleData getRematchData() {
        return rematchData;
    }

    public boolean isTeleportEnabled() {
        return teleportEnabled;
    }

    public TeleportData getTeleportData() {
        return teleportData;
    }

}

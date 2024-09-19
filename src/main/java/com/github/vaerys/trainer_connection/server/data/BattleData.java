package com.github.vaerys.trainer_connection.server.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class BattleData {

    public static final BattleData DEFAULT = new BattleData("", ItemData.EMPTY, "", "", "", false, false, MessageData.DEFAULT);
    public static Codec<BattleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("battle_command").orElseGet(() -> "").forGetter(BattleData::getBattleCommand),
            ItemData.CODEC.fieldOf("unlock_item").orElseGet(() -> ItemData.EMPTY).forGetter(BattleData::getUnlockItem),
            Codec.STRING.fieldOf("victory_command").orElseGet(() -> "").forGetter(BattleData::getVictoryCommand),
            Codec.STRING.fieldOf("loss_command").orElseGet(() -> "").forGetter(BattleData::getLossCommand),
            Codec.STRING.fieldOf("abandoned_command").orElseGet(() -> "").forGetter(BattleData::getAbandonedCommand),
            Codec.BOOL.fieldOf("lock_on_loss").orElseGet(() -> false).forGetter(BattleData::isLockOnLoss),
            Codec.BOOL.fieldOf("lock_on_abandon").orElseGet(() -> false).forGetter(BattleData::isLockOnAbandon),
            MessageData.CODEC.fieldOf("messages").orElseGet(() -> MessageData.DEFAULT).forGetter(BattleData::getMessages)
    ).apply(instance, BattleData::new));

    private final String battleCommand;
    private final ItemData unlockItem;
    private final String victoryCommand;
    private final String lossCommand;
    private final String abandonedCommand;
    private final boolean lockOnLoss;
    private final boolean lockOnAbandon;
    private final MessageData messages;

    public BattleData(String battleCommand, ItemData unlockItem, String rewardCommand, String lossCommand, String abandonedCommand, boolean lockOnLoss, boolean lockOnAbandon, MessageData messages) {
        this.battleCommand = battleCommand;
        this.unlockItem = unlockItem;
        this.victoryCommand = rewardCommand;
        this.lossCommand = lossCommand;
        this.abandonedCommand = abandonedCommand;
        this.lockOnLoss = lockOnLoss;
        this.lockOnAbandon = lockOnAbandon;
        this.messages = messages;
    }

    public String getBattleCommand() {
        return battleCommand;
    }

    public ItemData getUnlockItem() {
        return unlockItem;
    }

    public String getVictoryCommand() {
        return victoryCommand;
    }

    public String getLossCommand() {
        return lossCommand;
    }

    public String getAbandonedCommand() {
        return abandonedCommand;
    }

    public boolean isLockOnLoss() {
        return lockOnLoss;
    }

    public boolean isLockOnAbandon() {
        return lockOnAbandon;
    }

    public MessageData getMessages() {
        return messages;
    }
}

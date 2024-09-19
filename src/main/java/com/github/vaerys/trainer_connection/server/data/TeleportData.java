package com.github.vaerys.trainer_connection.server.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class TeleportData {

    public static final TeleportData DEFAULT = new TeleportData(TeleportPos.DEFAULT, TeleportPos.DEFAULT, TeleportPos.DEFAULT, TeleportPos.DEFAULT);
    public static Codec<TeleportData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TeleportPos.CODEC.fieldOf("npc_home_pos").forGetter(TeleportData::getNpcHomePos),
            TeleportPos.CODEC.fieldOf("npc_battle_pos").forGetter(TeleportData::getNpcBattlePos),
            TeleportPos.CODEC.fieldOf("challenger_battle_pos").forGetter(TeleportData::getChallengerBattlePos),
            TeleportPos.CODEC.fieldOf("challenger_return_pos").forGetter(TeleportData::getChallengerReturnPos)
    ).apply(instance, TeleportData::new));

    private final TeleportPos npcHomePos;
    private final TeleportPos npcBattlePos;
    private final TeleportPos challengerBattlePos;
    private final TeleportPos challengerReturnPos;

    public TeleportData(TeleportPos npcHomePos, TeleportPos npcBattlePos, TeleportPos challengerBattlePos, TeleportPos challengerReturnPos) {
        this.npcHomePos = npcHomePos;
        this.npcBattlePos = npcBattlePos;
        this.challengerBattlePos = challengerBattlePos;
        this.challengerReturnPos = challengerReturnPos;
    }
    public TeleportPos getNpcHomePos() {
        return npcHomePos;
    }

    public TeleportPos getNpcBattlePos() {
        return npcBattlePos;
    }

    public TeleportPos getChallengerBattlePos() {
        return challengerBattlePos;
    }

    public TeleportPos getChallengerReturnPos() {
        return challengerReturnPos;
    }




}

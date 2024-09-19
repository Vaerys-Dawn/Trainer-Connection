package com.github.vaerys.trainer_connection.server.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class MessageData {

    public static final MessageData DEFAULT = new MessageData("", "", "", "", "", "", "", "", "");
    public static Codec<MessageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("welcome").orElseGet(() -> "").forGetter(MessageData::getWelcome),
            Codec.STRING.fieldOf("pre_battle").orElseGet(() -> "").forGetter(MessageData::getPreBattle),
            Codec.STRING.fieldOf("win").orElseGet(() -> "").forGetter(MessageData::getWin),
            Codec.STRING.fieldOf("lose").orElseGet(() -> "").forGetter(MessageData::getLose),
            Codec.STRING.fieldOf("reattempt").orElseGet(() -> "").forGetter(MessageData::getReattempt),
            Codec.STRING.fieldOf("locked").orElseGet(() -> "").forGetter(MessageData::getLocked),
            Codec.STRING.fieldOf("busy").orElseGet(() -> "").forGetter(MessageData::getBusy),
            Codec.STRING.fieldOf("post_win").orElseGet(() -> "").forGetter(MessageData::getPostWin),
            Codec.STRING.fieldOf("abandoned").orElseGet(() -> "").forGetter(MessageData::getAbandon)
    ).apply(instance, MessageData::new));

    private final String welcome;
    private final String preBattle;
    private final String win;
    private final String lose;
    private final String reattempt;

    private final String locked;
    private final String busy;
    private final String postWin;
    private final String abandoned;

    public MessageData(String welcome, String preBattle, String win, String lose, String reattempt, String locked, String busy, String postWin, String abandoned) {
        this.welcome = welcome;
        this.preBattle = preBattle;
        this.win = win;
        this.lose = lose;
        this.reattempt = reattempt;
        this.locked = locked;
        this.busy = busy;
        this.postWin = postWin;
        this.abandoned = abandoned;

    }

    public String getWelcome() {
        return welcome;
    }

    public String getPreBattle() {
        return preBattle;
    }

    public String getWin() {
        return win;
    }

    public String getLose() {
        return lose;
    }

    public String getReattempt() {
        return reattempt;
    }

    public String getLocked() {
        return locked;
    }

    public String getBusy() {
        return busy;
    }

    public String getPostWin() {
        return postWin;
    }

    public String getAbandon() {
        return abandoned;
    }
}

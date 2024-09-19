package com.github.vaerys.trainer_connection.common;

import com.github.vaerys.trainer_connection.NPCTrainerConnection;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Constants {
    @NotNull
    public static final Identifier NPC_TC_INTERFACE_PACKET_ID = Objects.requireNonNull(Identifier.of(NPCTrainerConnection.MOD_ID, "display_conversation"));
    @NotNull
    public static final Identifier NPC_TC_REQUEST_BATTLE = Objects.requireNonNull(Identifier.of(NPCTrainerConnection.MOD_ID, "request_battle"));
    @NotNull
    public static final Identifier NPC_TC_SERVER_SYNC = Objects.requireNonNull(Identifier.of(NPCTrainerConnection.MOD_ID, "server_sync"));
    @NotNull
    public static final Identifier NPC_TC_REQUEST_UNLOCK = Objects.requireNonNull(Identifier.of(NPCTrainerConnection.MOD_ID, "request_unlock"));
    public static final String TRAINER_STATE_BATTLE = "Battle";
    public static final String TRAINER_STATE_IDLE = "Idle";
    public static final int MAX_DISTANCE = 100000;

    private Constants() {
        // hide constructor
    }
    public static void initConstants() {
        NPCTrainerConnection.LOGGER.info("Loading...");
    }
}

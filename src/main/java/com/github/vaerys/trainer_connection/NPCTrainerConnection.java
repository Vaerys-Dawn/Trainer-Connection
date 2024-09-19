package com.github.vaerys.trainer_connection;

import com.github.vaerys.trainer_connection.common.Constants;
import com.github.vaerys.trainer_connection.common.items.ItemRegistry;
import com.github.vaerys.trainer_connection.server.battle.BattleUtils;
import com.github.vaerys.trainer_connection.server.commands.CommandRegister;
import com.github.vaerys.trainer_connection.server.data.DataRegister;
import com.github.vaerys.trainer_connection.server.states.ChallengerStateHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCTrainerConnection implements ModInitializer {
    public static final String MOD_ID = "npctc";

    public static final Logger LOGGER = LogManager.getLogger("[NPC-Trainer-Connections]");
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        CommandRegister.registerCommands();
        DataRegister.register();
        ItemRegistry.registryInit();
        Constants.initConstants();
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ChallengerStateHandler.getServerState(server);
        });

        ServerPlayNetworking.registerGlobalReceiver(Constants.NPC_TC_REQUEST_BATTLE, (server, player, handler, buf, responseSender) -> {
            BattleUtils.startBattle(buf, server);
        });

        ServerPlayNetworking.registerGlobalReceiver(Constants.NPC_TC_REQUEST_UNLOCK, (server, player, handler, buf, responseSender) -> {
            BattleUtils.unlockStuff(buf, server);
        });

        ServerTickEvents.START_SERVER_TICK.register(BattleUtils::checkAbandons);
    }
}

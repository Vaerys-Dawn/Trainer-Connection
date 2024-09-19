package com.github.vaerys.trainer_connection;

import com.github.vaerys.trainer_connection.client.ClientDataRegister;
import com.github.vaerys.trainer_connection.client.ClientImpl;
import com.github.vaerys.trainer_connection.common.Constants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class NPCTrainerConnectionClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientImpl.register();
        ClientPlayNetworking.registerGlobalReceiver(Constants.NPC_TC_DATAPACK_SYNC, ClientDataRegister::receiveDatapackResources);
    }
}

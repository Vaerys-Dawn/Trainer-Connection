package com.github.vaerys.trainer_connection;

import com.github.vaerys.trainer_connection.client.ClientImpl;
import com.github.vaerys.trainer_connection.server.data.DataRegister;
import net.fabricmc.api.ClientModInitializer;

public class NPCTrainerConnectionClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientImpl.register();
    }
}

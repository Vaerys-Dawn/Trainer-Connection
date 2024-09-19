package com.github.vaerys.trainer_connection.client;

import com.github.vaerys.trainer_connection.common.Constants;
import com.github.vaerys.trainer_connection.server.commands.ClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class ClientImpl {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Constants.NPC_TC_INTERFACE_PACKET_ID, ((client, handler, buf, responseSender) -> {
            buf.retain();
            ClientPacket packet = new ClientPacket(buf);
            buf.release();
            client.execute(() -> client.setScreen(new ConversationInterface(client.player, packet)));
        }));
    }

}

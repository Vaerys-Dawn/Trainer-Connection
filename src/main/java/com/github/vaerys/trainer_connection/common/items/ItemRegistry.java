package com.github.vaerys.trainer_connection.common.items;

import com.github.vaerys.trainer_connection.NPCTrainerConnection;
import com.github.vaerys.trainer_connection.common.Constants;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ItemRegistry {
    public static final Item VS_SEEKER = register("vs_seeker", new VSSeeker(new Item.Settings().maxDamage(Constants.MAX_DISTANCE).maxDamageIfAbsent(Constants.MAX_DISTANCE).rarity(Rarity.UNCOMMON)));
    public static <T extends Item> T register(String path, T item) {
        return Registry.register(Registries.ITEM, new Identifier(NPCTrainerConnection.MOD_ID, path), item);
    }

    public static void registryInit() {
        NPCTrainerConnection.LOGGER.info("Registering Items...");
    }

}

package com.github.vaerys.trainer_connection.server.data;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

public class ItemData {

    public static final ItemData EMPTY = new ItemData(ItemStack.EMPTY, false, ItemStack.EMPTY);
    public static Codec<ItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").orElseGet(() -> ItemStack.EMPTY).forGetter(ItemData::getItem),
            Codec.BOOL.fieldOf("is_consumed").orElseGet(() -> false).forGetter(ItemData::isConsumed),
            ItemStack.CODEC.fieldOf("consumed_result").orElseGet(() -> ItemStack.EMPTY).forGetter(ItemData::getConsumedResult)
    ).apply(instance, ItemData::new));

    private final ItemStack item;

    private final boolean consumed;

    private final ItemStack consumedResult;

    public ItemData(ItemStack stack, boolean consumed, ItemStack consumedResult) {
        this.consumed = consumed;
        this.consumedResult = consumedResult;
        this.item = stack;
    }

    public boolean isValid() {
        return !item.isEmpty();
    }

    public boolean isConsumed() {
        return consumed;
    }

    public ItemStack getConsumedResult() {
        return consumedResult;
    }

    public ItemStack getItem() {
        return item;
    }
}

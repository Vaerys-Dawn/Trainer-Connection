package com.github.vaerys.trainer_connection.server.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

public class TeleportPos {

    public static final TeleportPos DEFAULT = new TeleportPos(BlockPos.ORIGIN, 0f, 0f);
    public static Codec<TeleportPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("block_pos").forGetter(TeleportPos::getBlockPos),
            Codec.FLOAT.fieldOf("pitch").forGetter(TeleportPos::getPitch),
            Codec.FLOAT.fieldOf("yaw").forGetter(TeleportPos::getYaw)
    ).apply(instance, TeleportPos::new));
    private final BlockPos blockPos;
    private final float pitch;
    private final float yaw;

    public TeleportPos(BlockPos blockPos, float pitch, float yaw) {
        this.blockPos = blockPos;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }
}

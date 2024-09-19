package com.github.vaerys.trainer_connection.common.items;

import com.github.vaerys.trainer_connection.common.Constants;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VSSeeker extends TrinketItem {

    private static final String LAST_DISTANCE_TAG = "last_distance";
    public static final String TOTAL_DISTANCE_TAG = "total_distance";
    public static final String COMPLETE_TAG = "charge_complete";

    int ticks;

    public VSSeeker(Settings settings) {
        super(settings);
        ticks = 0;
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        super.tick(stack, slot, entity);
        if (entity.getEntityWorld().isClient) return;
        if (!entity.isPlayer()) return;
        ticks++;
        // only tick every 8 ticks
        if (ticks % 8 != 0) return;
        updateDistance(stack, slot, entity);
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        super.onEquip(stack, slot, entity);
        if (entity.getEntityWorld().isClient) return;
        if (!entity.isPlayer()) return;
        if (entity.getServer() == null) return;
        ServerPlayerEntity player = entity.getServer().getPlayerManager().getPlayer(entity.getUuid());
        if (player == null) return;
        ServerStatHandler serverStatHandler = player.getStatHandler();

        // get total distance
        long distanceWalked = serverStatHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM));
        long distanceRan = serverStatHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SPRINT_ONE_CM));
        long distance = distanceWalked + distanceRan;
        NbtCompound compound = stack.getNbt();
        // set the last distance to the distance, do not update total distance
        if (compound == null) compound = new NbtCompound();
        compound.putLong(LAST_DISTANCE_TAG, distance);
        stack.setNbt(compound);
        updateDamage(player, slot, stack);
    }

    private void updateDamage(ServerPlayerEntity player, SlotReference slot, ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        long distance = 0;
        if (nbt != null && nbt.contains(TOTAL_DISTANCE_TAG)) distance = nbt.getLong(TOTAL_DISTANCE_TAG);
        stack.setDamage(Constants.MAX_DISTANCE - (int) distance);
        nbt = stack.getNbt();
        if (stack.getDamage() == 0 && !(nbt != null && nbt.contains(COMPLETE_TAG) && nbt.getBoolean(COMPLETE_TAG))) {
            if (nbt == null) nbt = new NbtCompound();
            nbt.putBoolean(COMPLETE_TAG, true);
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 0.75f);
            player.sendMessage(Text.literal("Your VS. Seeker is fully charged.").setStyle(Style.EMPTY.withColor(Formatting.BLUE)));
        }
        stack.setNbt(nbt);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(COMPLETE_TAG) && nbt.getBoolean(COMPLETE_TAG)) {
            tooltip.add(Text.literal("Fully Charged").setStyle(Style.EMPTY.withColor(Formatting.BLUE)));
        } else {
            tooltip.add(Text.literal("Needs Charging").setStyle(Style.EMPTY.withColor(Formatting.RED)));
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains(COMPLETE_TAG) && nbt.getBoolean(COMPLETE_TAG);
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        super.onUnequip(stack, slot, entity);
        if (entity.getEntityWorld().isClient) return;
        if (!entity.isPlayer()) return;
        updateDistance(stack, slot, entity);
    }

    private void updateDistance(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity.getEntityWorld().isClient) return;
        if (entity.getServer() == null) return;
        ServerPlayerEntity player = entity.getServer().getPlayerManager().getPlayer(entity.getUuid());
        if (player == null) return;
        ServerStatHandler serverStatHandler = player.getStatHandler();

        // get total distance traveled by the player
        long distanceWalked = serverStatHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM));
        long distanceRan = serverStatHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SPRINT_ONE_CM));
        long distance = distanceWalked + distanceRan;

        NbtCompound compound = stack.getNbt();
        if (compound == null) compound = new NbtCompound();
        // update the last distance traveled to the new distance
        long lastDistance = distance;
        if (compound.contains(LAST_DISTANCE_TAG)) lastDistance = compound.getLong(LAST_DISTANCE_TAG);
        compound.putLong(LAST_DISTANCE_TAG, distance);

        // get the total distance
        long totalDistance = 0;
        if (compound.contains(TOTAL_DISTANCE_TAG)) totalDistance = compound.getLong(TOTAL_DISTANCE_TAG);
        // add in the new distance by comparing the last distance to current distance
        totalDistance += distance - lastDistance;
        // update the distance clamping it to max distance
        if (totalDistance >= Constants.MAX_DISTANCE) totalDistance = Constants.MAX_DISTANCE;
        compound.putLong(TOTAL_DISTANCE_TAG, totalDistance);
        // update stack
        stack.setNbt(compound);
        updateDamage(player, slot, stack);
    }
}

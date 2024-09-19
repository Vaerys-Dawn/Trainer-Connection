package com.github.vaerys.trainer_connection.server.battle;

import com.github.vaerys.trainer_connection.common.Constants;
import com.github.vaerys.trainer_connection.common.items.ItemRegistry;
import com.github.vaerys.trainer_connection.common.items.VSSeeker;
import com.github.vaerys.trainer_connection.server.commands.Utils;
import com.github.vaerys.trainer_connection.server.data.DataRegister;
import com.github.vaerys.trainer_connection.server.data.TeleportPos;
import com.github.vaerys.trainer_connection.server.data.TrainerData;
import com.github.vaerys.trainer_connection.server.states.ChallengerLink;
import com.github.vaerys.trainer_connection.server.states.ChallengerStateHandler;
import com.github.vaerys.trainer_connection.server.states.TrainerLink;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BattleUtils {

    private BattleUtils() {
        // do not implement
    }


    public static void startBattle(PacketByteBuf buf, MinecraftServer server) {
        String trainerID = buf.readString();
        UUID challengerID = buf.readUuid();
        UUID entityID = buf.readUuid();
        boolean isRematch = buf.readBoolean();
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(challengerID);
        TrainerLink trainerLink = ChallengerStateHandler.getServerState(server).getOrCreateTrainer(trainerID);
        ChallengerLink challengerLink = trainerLink.getOrCreateChallenger(player);
        TrainerData data = DataRegister.trainerDataList.get(trainerID);

        String command;
        if (isRematch) {
            command = data.getRematchData().getBattleCommand();
        } else {
            command = data.getBattleData().getBattleCommand();
        }

        command = prepCommand(command, trainerID, entityID, player);

        // set the trainer's state
        trainerLink.trainerState = Constants.TRAINER_STATE_BATTLE;
        trainerLink.currentChallenger = challengerID;

        // increment attempts
        if (isRematch) {
            challengerLink.rematchAttempts++;
        } else {
            challengerLink.attempts++;
        }

        // teleport player and npc if enabled
        if (data.isTeleportEnabled()) {
            handleEntityBattleTeleport(entityID, data, server);
            handlePlayerBattleTeleport(player, data, server);
        }

        CommandManager manager = server.getCommandManager();
        ServerCommandSource commandSource = server.getCommandSource();

        String message = Utils.applyReplaceTags(isRematch ? data.getRematchData().getMessages().getPreBattle() : data.getBattleData().getMessages().getPreBattle(), player, data, challengerLink, isRematch);
        // send intro message
        Utils.sendTrainerMessage(player, data, message);

        // send battle start
        manager.executeWithPrefix(commandSource, command);
    }


    public static void unlockStuff(PacketByteBuf buf, MinecraftServer server) {
        String trainerID = buf.readString();
        UUID challengerID = buf.readUuid();
        boolean isRematch = buf.readBoolean();

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(challengerID);
        TrainerLink trainerLink = ChallengerStateHandler.getServerState(server).getOrCreateTrainer(trainerID);
        ChallengerLink challengerLink = trainerLink.getOrCreateChallenger(player);
        TrainerData data = DataRegister.trainerDataList.get(trainerID);

        ItemStack stack;
        ItemStack returnItem;
        boolean consume;

        if (isRematch) {
            stack = data.getRematchData().getUnlockItem().getItem();
            returnItem = data.getRematchData().getUnlockItem().getConsumedResult();
            consume = data.getRematchData().getUnlockItem().isConsumed();
        } else {
            stack = data.getBattleData().getUnlockItem().getItem();
            returnItem = data.getBattleData().getUnlockItem().getConsumedResult();
            consume = data.getBattleData().getUnlockItem().isConsumed();
        }

        if (stack.getItem() == Items.BARRIER) return;

        if (stack.getItem() == ItemRegistry.VS_SEEKER) {
            // check player inv by iterating through inv
            boolean found = false;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack toTest = player.getInventory().getStack(i);
                // find vs seeker
                if (checkAndResetVsSeeker(toTest, player)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
                if (component.isPresent()) {
                    // get and check all equipped trinkets for the vs seeker.
                    List<Pair<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();

                    for (Pair<SlotReference, ItemStack> slot : allEquipped) {
                        if (checkAndResetVsSeeker(slot.getRight(), player)) {
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (found) {
                if (isRematch) {
                    challengerLink.rematchUnlocked = true;
                } else {
                    challengerLink.battleUnlocked = true;
                }
            }

        } else {
            int count = player.getInventory().count(stack.getItem());
            if (count >= stack.getCount()) {
                if (consume) {
                    player.getInventory().remove(itemStack -> itemStack.itemMatches(stack.getRegistryEntry()), stack.getCount(), player.playerScreenHandler.getCraftingInput());
                    player.giveItemStack(returnItem);
                }

                if (isRematch) {
                    challengerLink.rematchUnlocked = true;
                } else {
                    challengerLink.battleUnlocked = true;
                }
            }
        }

        PacketByteBuf packet = Utils.deserializeClientPacket(trainerID, player, server);
        ServerPlayNetworking.send(player, Constants.NPC_TC_SERVER_SYNC, packet);
    }

    private static boolean checkAndResetVsSeeker(ItemStack stack, ServerPlayerEntity player) {
        if (stack.getItem() != ItemRegistry.VS_SEEKER) return false;
        NbtCompound nbt = stack.getNbt();

        // if nbt is present and complete tag is true
        if (nbt != null && nbt.contains(VSSeeker.COMPLETE_TAG) && nbt.getBoolean(VSSeeker.COMPLETE_TAG)) {
            nbt.putBoolean(VSSeeker.COMPLETE_TAG, false);
            nbt.putLong(VSSeeker.TOTAL_DISTANCE_TAG, 0);
            stack.setNbt(nbt);

            player.sendMessage(Text.literal("Your VS. Seeker's charge has been used up.").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            return true;
        }
        return false;
    }

    public static void handleWin(String trainerId, Entity entity, ServerPlayerEntity player, boolean isRematch) {
        TrainerLink trainerLink = ChallengerStateHandler.getServerState(player.server).getOrCreateTrainer(trainerId);
        ChallengerLink challengerLink = trainerLink.getOrCreateChallenger(player);
        TrainerData data = DataRegister.trainerDataList.get(trainerId);
        MinecraftServer server = player.server;
        if (!trainerLink.trainerState.equals(Constants.TRAINER_STATE_BATTLE)) {
            String message = "This should not have happened! If this message occurred, the trainer triggered it's win condition while not in the battle state.";
            // send intro message
            Utils.sendTrainerMessage(player, data, message);
            return;
        }

        handleEndBattle(trainerLink, data, player, entity, server);

        String command;
        if (isRematch) {
            challengerLink.rematchWins++;
            if (data.isLockRematchOnWin()) {
                challengerLink.rematchUnlocked = false;
            }
            command = data.getRematchData().getVictoryCommand();
        } else {
            challengerLink.battleWon = true;
            command = data.getBattleData().getVictoryCommand();
        }
        command = prepCommand(command, trainerId, entity.getUuid(), player);

        CommandManager manager = server.getCommandManager();
        ServerCommandSource commandSource = server.getCommandSource();

        String message = Utils.applyReplaceTags(isRematch ? data.getRematchData().getMessages().getWin() :
                data.getBattleData().getMessages().getWin(), player, data, challengerLink, isRematch);
        // send intro message
        Utils.sendTrainerMessage(player, data, message);

        // send battle start
        manager.executeWithPrefix(commandSource, command);
    }


    public static void handleLose(String trainerId, Entity entity, ServerPlayerEntity player, boolean isRematch) {
        TrainerLink trainerLink = ChallengerStateHandler.getServerState(player.server).getOrCreateTrainer(trainerId);
        ChallengerLink challengerLink = trainerLink.getOrCreateChallenger(player);
        TrainerData data = DataRegister.trainerDataList.get(trainerId);
        MinecraftServer server = player.server;
        if (!trainerLink.trainerState.equals(Constants.TRAINER_STATE_BATTLE)) {
            String message = "This should not have happened! If this message occurred, the trainer triggered it's lose condition while not in the battle state.";
            // send intro message
            Utils.sendTrainerMessage(player, data, message);
            return;
        }

        handleEndBattle(trainerLink, data, player, entity, server);

        String command;
        if (isRematch) {
            challengerLink.rematchLosses++;
            command = data.getRematchData().getLossCommand();
            if (data.getRematchData().isLockOnLoss()) {
                challengerLink.rematchUnlocked = false;
            }
        } else {
            challengerLink.losses++;
            command = data.getBattleData().getLossCommand();
            if (data.getBattleData().isLockOnLoss()) {
                challengerLink.battleUnlocked = false;
            }
        }
        command = prepCommand(command, trainerId, entity.getUuid(), player);

        CommandManager manager = server.getCommandManager();
        ServerCommandSource commandSource = server.getCommandSource();

        String message = Utils.applyReplaceTags(isRematch ? data.getRematchData().getMessages().getLose() :
                data.getBattleData().getMessages().getLose(), player, data, challengerLink, isRematch);
        // send intro message
        Utils.sendTrainerMessage(player, data, message);

        // send battle start
        manager.executeWithPrefix(commandSource, command);
    }

    public static void handleAbandon(Entity entity, String trainerID, TrainerLink trainerLink, TrainerData data, ServerPlayerEntity player, boolean awardAbandon) {
        if (entity.getWorld().isClient) return;

        // try to get the server, if both are null, something fucking weird has happened
        MinecraftServer server = entity.getServer();
        if (server == null) player.getServer();
        if (server == null) return;
        boolean isRematch = false;
        if (player != null) {
            ChallengerLink challengerLink = trainerLink.getOrCreateChallenger(player);
            isRematch = challengerLink.battleWon && data.isRematchEnabled();

            if (isRematch) {
                if (awardAbandon) challengerLink.rematchAbandons++;
                if (data.getRematchData().isLockOnAbandon()) {
                    challengerLink.rematchUnlocked = false;
                }
            } else {
                if (awardAbandon) challengerLink.abandons++;
                if (data.getBattleData().isLockOnAbandon()) {
                    challengerLink.battleUnlocked = false;
                }
            }

            String message = Utils.applyReplaceTags(isRematch ? data.getRematchData().getMessages().getAbandon() :
                    data.getBattleData().getMessages().getAbandon(), player, data, challengerLink, isRematch);
            // send intro message
            Utils.sendTrainerMessage(player, data, message);
        }
        String command;
        if (isRematch) {
            command = data.getRematchData().getAbandonedCommand();

        } else {
            command = data.getBattleData().getAbandonedCommand();
        }
        prepCommand(command, trainerID, entity.getUuid(), player);

        CommandManager manager = server.getCommandManager();
        ServerCommandSource commandSource = server.getCommandSource();


        // send battle start
        manager.executeWithPrefix(commandSource, command);

        if (data.isTeleportEnabled()) {
            handleEntityReturnTeleport(entity, data, server);
        }
        trainerLink.trainerState = Constants.TRAINER_STATE_IDLE;

    }

    private static String prepCommand(String command, String trainerID, UUID entityID, ServerPlayerEntity player) {
        // replace data
        command = command.replace("%entity%", entityID.toString());
        command = command.replace("%trainer_id%", trainerID);
        return command.replace("%player%", player == null ? "@p" : player.getDisplayName().getString());
    }

    private static void handleEndBattle(TrainerLink trainerLink, TrainerData data, ServerPlayerEntity player, Entity entity, MinecraftServer server) {
        if (data.isTeleportEnabled()) {
            handleEntityReturnTeleport(entity, data, server);
            handlePlayerReturnTeleport(player, data, server);
        }
        trainerLink.trainerState = Constants.TRAINER_STATE_IDLE;
    }



    private static void handleEntityBattleTeleport(UUID entityID, TrainerData data, MinecraftServer server) {
        CommandManager manager = server.getCommandManager();
        ServerCommandSource source = server.getCommandSource();
        TeleportPos pos = data.getTeleportData().getNpcBattlePos();
        manager.executeWithPrefix(source, String.format("tp %s %f %f %f %f %f",
                entityID.toString(),
                pos.getBlockPos().getX() + 0.5f,
                pos.getBlockPos().getY() + 0.0f,
                pos.getBlockPos().getZ() + 0.5f,
                pos.getPitch(),
                pos.getYaw()));
    }

    private static void handlePlayerBattleTeleport(ServerPlayerEntity player, TrainerData data, MinecraftServer server) {
        CommandManager manager = server.getCommandManager();
        ServerCommandSource source = server.getCommandSource();
        TeleportPos pos = data.getTeleportData().getChallengerBattlePos();
        manager.executeWithPrefix(source, String.format("tp %s %f %f %f %f %f",
                player.getDisplayName().getString(),
                pos.getBlockPos().getX() + 0.5f,
                pos.getBlockPos().getY() + 0.0f,
                pos.getBlockPos().getZ() + 0.5f,
                pos.getPitch(),
                pos.getYaw()));
    }

    private static void handlePlayerReturnTeleport(ServerPlayerEntity player, TrainerData data, MinecraftServer server) {
        CommandManager manager = server.getCommandManager();
        ServerCommandSource source = server.getCommandSource();
        TeleportPos pos = data.getTeleportData().getChallengerReturnPos();
        manager.executeWithPrefix(source, String.format("tp %s %f %f %f %f %f",
                player.getDisplayName().getString(),
                pos.getBlockPos().getX() + 0.5f,
                pos.getBlockPos().getY() + 0.0f,
                pos.getBlockPos().getZ() + 0.5f,
                pos.getPitch(),
                pos.getYaw()));
    }

    private static void handleEntityReturnTeleport(Entity entity, TrainerData data, MinecraftServer server) {
        CommandManager manager = server.getCommandManager();
        ServerCommandSource source = server.getCommandSource();
        TeleportPos pos = data.getTeleportData().getNpcHomePos();
        manager.executeWithPrefix(source, String.format("tp %s %f %f %f %f %f",
                entity.getUuid().toString(),
                pos.getBlockPos().getX() + 0.5f,
                pos.getBlockPos().getY() + 0.0f,
                pos.getBlockPos().getZ() + 0.5f,
                pos.getPitch(),
                pos.getYaw()));
    }


    public static void checkAbandons(MinecraftServer server) {
        // check every 30 seconds
        if (server.getTicks() % 600 == 0) {

            ChallengerStateHandler handler = ChallengerStateHandler.getServerState(server);
            Map<Entity, Pair<String, TrainerLink>> toTest = new HashMap<>();
            for (Map.Entry<String, TrainerLink> entry : handler.getAll().entrySet()) {
                String s = entry.getKey();
                TrainerLink trainerLink = entry.getValue();
                // if trainer is not in a battle skip
                if (!trainerLink.trainerState.equals(Constants.TRAINER_STATE_BATTLE)) continue;
                // check each world for the trainer
                for (ServerWorld serverWorld : server.getWorlds()) {
                    // once trainer has been found add to map to test
                    Entity entity = serverWorld.getEntity(trainerLink.lastKnownEntityID);
                    if (entity != null) {
                        toTest.put(entity, new Pair<>(s, trainerLink));
                        break;
                    }
                }
            }
            // isolated trainers to test, for abandonment.

            for (Map.Entry<Entity, Pair<String, TrainerLink>> links : toTest.entrySet()) {
                Entity entity = links.getKey();

                ServerPlayerEntity player = server.getPlayerManager().getPlayer(links.getValue().getRight().currentChallenger);
                TrainerData data = DataRegister.trainerDataList.get(links.getValue().getLeft());
                if (data == null) continue;
                if (player == null || player.isDisconnected()) {
                    handleAbandon(entity, links.getValue().getLeft(), links.getValue().getRight(), data, player);
                } else {
                    BlockPos blockPos;
                    if (data.isTeleportEnabled()) {
                        blockPos = data.getTeleportData().getChallengerBattlePos().getBlockPos();
                    } else {
                        blockPos = links.getValue().getRight().fallbackPosition;
                    }
                    double dist = player.getPos().distanceTo(blockPos.toCenterPos());
                    if (dist > 50) {
                        handleAbandon(entity, links.getValue().getLeft(), links.getValue().getRight(), data, player);
                    }
                }
            }

        }
    }

    public static void handleAbandon(Entity entity, String trainerId, TrainerLink link, TrainerData data, ServerPlayerEntity player) {
        handleAbandon(entity, trainerId, link, data, player, true);
    }
}

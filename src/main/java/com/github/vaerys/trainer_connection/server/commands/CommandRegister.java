package com.github.vaerys.trainer_connection.server.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class CommandRegister {

    private CommandRegister() {
        // hide constructor
    }


    private static final String IS_REMATCH = "is_rematch";
    public static final String TRAINER_ID = "trainer_id";
    public static final String PLAYER = "player";
    public static final String ENTITY = "entity";

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            LiteralCommandNode<ServerCommandSource> npcDebugCommand = literal("npctc_debug").requires(source -> source.hasPermissionLevel(2)).build();
            npcDebugCommand.addChild(literal("reset_player_all")
                    .then(argument(PLAYER, EntityArgumentType.player())
                            .executes(context -> TCDebugCommand.runResetPlayerALl(context, EntityArgumentType.getPlayer(context, PLAYER)))
                    ).build());
            npcDebugCommand.addChild(literal("reset_player")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .executes(context -> TCDebugCommand.runResetPlayer(context, StringArgumentType.getString(context, TRAINER_ID), EntityArgumentType.getPlayer(context, PLAYER))))
                    ).build());
            npcDebugCommand.addChild(literal("reset_trainer")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .executes(context -> TCDebugCommand.runResetTrainer(context, StringArgumentType.getString(context, TRAINER_ID)))
                    ).build());
            npcDebugCommand.addChild(literal("stop_battle")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .executes(context -> TCDebugCommand.runStopBattle(context, StringArgumentType.getString(context, TRAINER_ID)))
                    ).build());


//            npcDebugCommand.addChild(literal("modify_battle_attempts")
//                    .then(argument(PLAYER, EntityArgumentType.player())
//                            .then(argument(TRAINER_ID, StringArgumentType.word())
//                                    .then(argument("modifier", StringArgumentType.word())
//                                            .suggests((context, builder) -> {
//                                                builder.suggest("add");
//                                                builder.suggest("remove");
//                                                builder.suggest("set");
//                                                return builder.buildFuture();
//                                            })
//                                            .then(argument("value", IntegerArgumentType.integer(0))
//                                                    .executes(context -> TCDebugCommand.doModifyAttempts(context,
//                                                            StringArgumentType.getString(context, TRAINER_ID),
//                                                            EntityArgumentType.getPlayer(context, PLAYER),
//                                                            StringArgumentType.getString(context, "modifier"),
//                                                            IntegerArgumentType.getInteger(context, "value"), false))
//
//                                            )))).build());

//            npcDebugCommand.addChild(literal("modify_rematch_attempts")
//                    .then(argument(PLAYER, EntityArgumentType.player())
//                            .then(argument(TRAINER_ID, StringArgumentType.word())
//                                    .then(argument("modifier", StringArgumentType.word())
//                                            .suggests((context, builder) -> {
//                                                builder.suggest("add");
//                                                builder.suggest("remove");
//                                                builder.suggest("set");
//                                                return builder.buildFuture();
//                                            })
//                                            .then(argument("value", IntegerArgumentType.integer(0))
//                                                    .executes(context -> TCDebugCommand.doModifyAttempts(context,
//                                                            StringArgumentType.getString(context, TRAINER_ID),
//                                                            EntityArgumentType.getPlayer(context, PLAYER),
//                                                            StringArgumentType.getString(context, "modifier"),
//                                                            IntegerArgumentType.getInteger(context, "value"), true))
//
//                                            )))).build());

//            npcDebugCommand.addChild(literal("test")
//                    .then(argument("rest", StringArgumentType.greedyString())
//                            .executes(context -> TCDebugCommand.runDebugTest(context, StringArgumentType.getString(context, "rest")))
//                    ).build());

            npcDebugCommand.addChild(literal("disable_battle")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .executes(context -> TCDebugCommand.disableBattle(
                                            context,
                                            StringArgumentType.getString(context, TRAINER_ID),
                                            EntityArgumentType.getPlayer(context, PLAYER))))).build());

            npcDebugCommand.addChild(literal("enable_battle")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .executes(context -> TCDebugCommand.enableBattle(
                                            context,
                                            StringArgumentType.getString(context, TRAINER_ID),
                                            EntityArgumentType.getPlayer(context, PLAYER))))).build());

            npcDebugCommand.addChild(literal("disable_rematch")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .executes(context -> TCDebugCommand.disableRematch(
                                            context,
                                            StringArgumentType.getString(context, TRAINER_ID),
                                            EntityArgumentType.getPlayer(context, PLAYER))))).build());

            npcDebugCommand.addChild(literal("enable_rematch")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .executes(context -> TCDebugCommand.enableRematch(
                                            context,
                                            StringArgumentType.getString(context, TRAINER_ID),
                                            EntityArgumentType.getPlayer(context, PLAYER))))).build());

            npcDebugCommand.addChild(literal("generate_template").executes(Utils::generateTemplate).build());

            dispatcher.getRoot().addChild(npcDebugCommand);

            LiteralCommandNode<ServerCommandSource> npctcCommand = literal("npctc").requires(source -> source.hasPermissionLevel(2)).build();

//            npctcCommand.addChild(literal("test")
//                    .then(argument(TRAINER_ID, StringArgumentType.word())
//                            .then(argument(PLAYER, EntityArgumentType.player())
//                                    .then(argument(ENTITY, EntityArgumentType.entity())
//                                            .executes(context -> TrainerConnectionCommand.doTest(
//                                                    context,
//                                                    StringArgumentType.getString(context, TRAINER_ID),
//                                                    EntityArgumentType.getEntity(context, ENTITY),
//                                                    EntityArgumentType.getPlayer(context, PLAYER)))))).build());

            npctcCommand.addChild(literal("interact")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .then(argument(ENTITY, EntityArgumentType.entity())
                                            .executes(context -> TrainerConnectionCommand.doInteract(
                                                    context,
                                                    StringArgumentType.getString(context, TRAINER_ID),
                                                    EntityArgumentType.getEntity(context, ENTITY),
                                                    EntityArgumentType.getPlayer(context, PLAYER)))))).build());

            npctcCommand.addChild(literal("battle_win")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .then(argument(ENTITY, EntityArgumentType.entity())
                                            .executes(context -> TrainerConnectionCommand.doChallengerWin(
                                                    context,
                                                    StringArgumentType.getString(context, TRAINER_ID),
                                                    EntityArgumentType.getEntity(context, ENTITY),
                                                    EntityArgumentType.getPlayer(context, PLAYER), false))))).build());

            npctcCommand.addChild(literal("battle_lose")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .then(argument(ENTITY, EntityArgumentType.entity())
                                            .executes(context -> TrainerConnectionCommand.doChallengerLose(
                                                    context,
                                                    StringArgumentType.getString(context, TRAINER_ID),
                                                    EntityArgumentType.getEntity(context, ENTITY),
                                                    EntityArgumentType.getPlayer(context, PLAYER), false))))).build());

            npctcCommand.addChild(literal("rematch_win")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .then(argument(ENTITY, EntityArgumentType.entity())
                                            .executes(context -> TrainerConnectionCommand.doChallengerWin(
                                                    context,
                                                    StringArgumentType.getString(context, TRAINER_ID),
                                                    EntityArgumentType.getEntity(context, ENTITY),
                                                    EntityArgumentType.getPlayer(context, PLAYER), true))))).build());

            npctcCommand.addChild(literal("rematch_lose")
                    .then(argument(TRAINER_ID, StringArgumentType.word())
                            .then(argument(PLAYER, EntityArgumentType.player())
                                    .then(argument(ENTITY, EntityArgumentType.entity())
                                            .executes(context -> TrainerConnectionCommand.doChallengerLose(
                                                    context,
                                                    StringArgumentType.getString(context, TRAINER_ID),
                                                    EntityArgumentType.getEntity(context, ENTITY),
                                                    EntityArgumentType.getPlayer(context, PLAYER), true))))).build());



            dispatcher.getRoot().addChild(npctcCommand);

        });
    }
}

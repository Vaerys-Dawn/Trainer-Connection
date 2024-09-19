package com.github.vaerys.trainer_connection.client;

import com.github.vaerys.trainer_connection.common.Constants;
import com.github.vaerys.trainer_connection.common.items.ItemRegistry;
import com.github.vaerys.trainer_connection.common.items.VSSeeker;
import com.github.vaerys.trainer_connection.server.commands.ClientPacket;
import com.github.vaerys.trainer_connection.server.commands.Utils;
import com.github.vaerys.trainer_connection.server.data.DataRegister;
import com.github.vaerys.trainer_connection.server.data.MessageData;
import com.github.vaerys.trainer_connection.server.data.TrainerData;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;


import java.util.*;

import static com.github.vaerys.trainer_connection.NPCTrainerConnection.MOD_ID;

@Environment(EnvType.CLIENT)
public class ConversationInterface extends Screen {

    TrainerData trainerData;
    ClientPacket packet;
    ClientPlayerEntity player;
    CustomButton cancel;
    CustomButton battle;
    CustomButton unlock;
    ItemStack unlockItem;
    private final boolean itemValid;

    boolean isRematch;

    boolean showUnlock;

    String message;

    boolean busy;
    private boolean overrideUnlock = false;
    boolean unlockBlocked = false;

    public ConversationInterface(ClientPlayerEntity player, ClientPacket packet) {
        super(Text.literal("Conversation Interface"));
        this.packet = packet;
        this.player = player;
        this.trainerData = ClientDataRegister.trainerDataList.get(packet.trainerID);

        isRematch = packet.challengerLink.battleWon && trainerData.isRematchEnabled();

        itemValid = isRematch ? trainerData.getRematchData().getUnlockItem().isValid() : trainerData.getBattleData().getUnlockItem().isValid();
        unlockItem = isRematch ? trainerData.getRematchData().getUnlockItem().getItem() : trainerData.getBattleData().getUnlockItem().getItem();
        if (unlockItem.getItem() == ItemRegistry.VS_SEEKER) {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean(VSSeeker.COMPLETE_TAG, true);
            unlockItem.setNbt(nbt);
        }
        if (unlockItem.getItem() == Items.BARRIER) unlockBlocked = true;

        busy = packet.trainerState.equals(Constants.TRAINER_STATE_BATTLE);
    }

    @Override
    protected void init() {
        super.init();
        ClientPlayNetworking.registerReceiver(Constants.NPC_TC_SERVER_SYNC, (client1, handler, buf, responseSender) -> {
            packet = new ClientPacket(buf);
            overrideUnlock = false;
        });

        battle = new CustomButton((width / 2) + -157, (height / 2) + 80, 82, 26, Text.literal("Battle"), b -> {
            requestBattle();
            this.close();
        }, "conversation/battle_button.png", 14, 1);
        cancel = new CustomButton((width / 2) + -73, (height / 2) + 80, 82, 26, Text.literal("Leave"), b -> {
            this.close();
        }, "conversation/leave_button.png", 14, 1);
        unlock = new CustomButton((width / 2) + 19, (height / 2) + 80, 110, 26, Text.literal(isRematch ? "Unlock: Rematch" : "Unlock: Battle"), b -> {
            requestUnlock();
        }, "conversation/unlock_button.png", 14, 1);
        addDrawableChild(battle);
        addDrawableChild(cancel);
        addDrawableChild(unlock);

        tick();

        unlock.setTooltip(Tooltip.of(Text.literal("Consumes Item to unlock " + (isRematch ? "rematch." : "battle."))));
    }

    @Override
    public void close() {
        ClientPlayNetworking.unregisterReceiver(Constants.NPC_TC_SERVER_SYNC);
        super.close();
    }

    private void requestUnlock() {
        overrideUnlock = true;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(packet.trainerID);
        buf.writeUuid(packet.challengerID);
        buf.writeBoolean(isRematch);
        ClientPlayNetworking.send(Constants.NPC_TC_REQUEST_UNLOCK, buf);
    }

    private void requestBattle() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(packet.trainerID);
        buf.writeUuid(packet.challengerID);
        buf.writeUuid(packet.lastKnownEntityID);
        buf.writeBoolean(isRematch);
        ClientPlayNetworking.send(Constants.NPC_TC_REQUEST_BATTLE, buf);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);


        centeredTexture(context, 0, 0, 400, 300, "background.png");

        centeredText(context, -148, 15, trainerData.getDisplayName(), trainerData.getDisplayColor());
        drawTextWrapped(context, -148, 33, 290, StringVisitable.plain(message));

        if (showUnlock && !unlockBlocked) {
            centeredTexture(context, 144, 93, 26, 26, "item_slot.png");
            drawCenteredItem(context, unlockItem, 136, 85, mouseX, mouseY);
        }
    }

    @Override
    public void tick() {

        showUnlock = itemValid && (isRematch ? !packet.challengerLink.rematchUnlocked && trainerData.isRematchEnabled() :
                !packet.challengerLink.battleUnlocked);

        unlock.active = checkItems() && !overrideUnlock;

        battle.active = isRematch ? packet.challengerLink.rematchUnlocked : packet.challengerLink.battleUnlocked;

        MessageData messages = isRematch ? trainerData.getRematchData().getMessages() : trainerData.getBattleData().getMessages();

        if (showUnlock) message = messages.getLocked();
        else if (packet.challengerLink.rematchWins > 0) message = messages.getPostWin();
        else message = messages.getWelcome();

        if (busy) {
            showUnlock = false;
            battle.active = false;
            message = messages.getBusy();
        }

        unlock.visible = showUnlock && !unlockBlocked;
        message = Utils.applyReplaceTags(message, player, trainerData, packet.challengerLink, isRematch);

    }

    private boolean checkItems() {
        if (unlockItem.getItem() == ItemRegistry.VS_SEEKER) {
            // check if is present
            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
            if (component.isPresent()) {
                // get and check all equipped trinkets for the vs seeker.
                List<Pair<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
                for (Pair<SlotReference, ItemStack> slot : allEquipped) {
                    if (slot.getRight().getItem() == ItemRegistry.VS_SEEKER) {
                        NbtCompound nbt = slot.getRight().getNbt();

                        // if nbt is present and complete tag is true
                        if (nbt != null && nbt.contains(VSSeeker.COMPLETE_TAG) && nbt.getBoolean(VSSeeker.COMPLETE_TAG)) {
                            return true;
                        }
                    }
                }
            }
            // if vs seeker isn't in trinkets inv check main inv
            return player.getInventory().containsAny(stack -> {
                // if find vs seeker check if complete tag is present and true
                if (stack.getItem() == ItemRegistry.VS_SEEKER) {
                    NbtCompound nbt = stack.getNbt();
                    if (nbt == null || !nbt.contains(VSSeeker.COMPLETE_TAG)) return false;
                    return nbt.getBoolean(VSSeeker.COMPLETE_TAG);
                } else return false;
            });
        } else {
            // check if player has item and if they have the amount needed
            return (player.getInventory().contains(unlockItem) && player.getInventory().count(unlockItem.getItem()) >= unlockItem.getCount());
        }
    }

    private void drawCenteredItem(DrawContext context, ItemStack itemStack, int x, int y, int mouseX, int mouseY) {
        int right = (width / 2) + x + 16;
        int left = (width / 2) + x;
        int top = (height / 2) + y + 16;
        int bottom = (height / 2) + y;
        context.drawItem(itemStack, (width / 2) + x, (height / 2) + y);
        context.drawItemInSlot(textRenderer, itemStack, (width / 2) + x, (height / 2) + y);

        if (mouseX >= left && mouseX <= right && mouseY >= bottom && mouseY <= top) {
            context.drawItemTooltip(textRenderer, itemStack, mouseX, mouseY);
        }

    }

    public void centeredTexture(DrawContext context, int xOffset, int yOffset, int texWidth, int texHeight, String path) {
        String finalPath = "textures/gui/conversation/" + path;
        context.drawTexture(Identifier.of(MOD_ID, finalPath), (width / 2) - (texWidth / 2) + xOffset, (height / 2) - (texHeight / 2) + yOffset, 0, 0, texWidth, texHeight, texWidth, texHeight);
    }

    public void centeredText(DrawContext context, int xOffset, int yOffset, String text, String color) {
        TextColor textColor = TextColor.parse(color.toLowerCase());
        if (textColor == null) textColor = TextColor.parse("white");
        context.drawTextWithShadow(textRenderer, Text.literal(text), (width / 2) + xOffset, (height / 2) + yOffset, textColor.getRgb());
    }

    public void centeredText(DrawContext context, int xOffset, int yOffset, String text) {
        centeredText(context, xOffset, yOffset, text, "white");
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public void drawTextWrapped(DrawContext context, int xOffset, int yOffset, int textWidth, StringVisitable text) {
        for (Iterator var7 = textRenderer.wrapLines(text, textWidth).iterator(); var7.hasNext(); yOffset += 9) {
            OrderedText orderedText = (OrderedText) var7.next();
            context.drawText(textRenderer, orderedText, (width / 2) + xOffset, (height / 2) + yOffset, 0xffffff, true);
            Objects.requireNonNull(textRenderer);
        }

    }
}

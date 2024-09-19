package com.github.vaerys.trainer_connection.client;

import java.util.function.Supplier;

import com.github.vaerys.trainer_connection.NPCTrainerConnection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value = EnvType.CLIENT)
public class CustomButton extends PressableWidget {
    private final Identifier texture;
    protected static final NarrationSupplier DEFAULT_NARRATION_SUPPLIER = textSupplier -> (MutableText) textSupplier.get();
    protected final PressAction onPress;
    protected final NarrationSupplier narrationSupplier;
    private int xTextOffset;
    private int yTextOffset;

    protected CustomButton(int x, int y, int width, int height, Text message, PressAction onPress, String texPath, int xTextOffset, int yTextOffset) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.narrationSupplier = DEFAULT_NARRATION_SUPPLIER;
        this.texture = new Identifier(NPCTrainerConnection.MOD_ID, "textures/gui/" + texPath);
        this.xTextOffset = xTextOffset;
        this.yTextOffset = yTextOffset;
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        context.drawTexture(texture, getX(), getY(), getWidth(), getHeight(), 0, getTextureY(), getWidth(), getHeight(), getWidth(), getHeight() * 3);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int i = this.active ? 0xFFFFFF : 0xA0A0A0;
        this.drawMessage(context, minecraftClient.textRenderer, i | MathHelper.ceil((float) (this.alpha * 255.0f)) << 24);
    }
    @Override
    protected void drawScrollableText(DrawContext context, TextRenderer textRenderer, int xMargin, int color) {
        int i = this.getX() - xTextOffset;
        int j = this.getX() + this.getWidth() - xTextOffset;
        int k = this.getY() - yTextOffset;
        int l = this.getY() + this.getHeight() - yTextOffset;
        ClickableWidget.drawScrollableText(context, textRenderer, this.getMessage(), i, k, j, l, color);
    }

    private int getTextureY() {
        int i = 0;
        if (!this.active) {
            i = 2;
        } else if (this.isSelected()) {
            i = 1;
        }
        return i * height;
    }


    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected MutableText getNarrationMessage() {
        return this.narrationSupplier.createNarrationMessage(() -> super.getNarrationMessage());
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Environment(value = EnvType.CLIENT)
    public static interface PressAction {
        public void onPress(CustomButton var1);
    }

    @Environment(value = EnvType.CLIENT)
    public static interface NarrationSupplier {
        public MutableText createNarrationMessage(Supplier<MutableText> var1);
    }
}


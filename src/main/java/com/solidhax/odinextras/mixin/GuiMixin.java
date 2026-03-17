package com.solidhax.odinextras.mixin;

import com.solidhax.odinextras.events.PlayerGuiEvent;
import com.solidhax.odinextras.features.impl.render.HUD;
import com.solidhax.odinextras.features.impl.render.HudType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"), cancellable = true)
    private void onRenderSlot(GuiGraphics guiGraphics, int i, int j, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int k, CallbackInfo ci) {
        if(new PlayerGuiEvent.DrawSlot((Gui)(Object)this, guiGraphics, i, j, itemStack).postAndCatch()) ci.cancel();
    }

    @Inject(method = "renderEffects", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if(HUD.shouldCancelHud(HudType.POTION_EFFECT)) ci.cancel();
    }

    @Inject(method = "renderSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"), cancellable = true)
    private void onRenderSelectedItemName(GuiGraphics guiGraphics, CallbackInfo ci) {
        if(HUD.shouldCancelHud(HudType.TOOLTIP)) ci.cancel();
    }

    @Inject(method = "renderVignette", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderVignette(GuiGraphics guiGraphics, Entity entity, CallbackInfo ci) {
        if(HUD.shouldCancelHud(HudType.VIGNETTE)) ci.cancel();
    }
}

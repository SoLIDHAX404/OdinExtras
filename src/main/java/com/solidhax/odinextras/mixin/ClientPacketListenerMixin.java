package com.solidhax.odinextras.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.solidhax.odinextras.events.SetEntityMetadataEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleSetEntityData", at = @At("TAIL"), cancellable = true)
    private void onHandleSetEntityData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket, CallbackInfo ci, @Local Entity entity) {
        if(entity == null) return;

        if(new SetEntityMetadataEvent(entity, clientboundSetEntityDataPacket).postAndCatch()) ci.cancel();
    }

}

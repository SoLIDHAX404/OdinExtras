package com.solidhax.odinextras.mixin;

import com.solidhax.odinextras.features.impl.skyblock.PreventPlacingSkyblockItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void onCanPlace(BlockPlaceContext blockPlaceContext, BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        if(PreventPlacingSkyblockItems.shouldCancelPlacement(blockPlaceContext.getItemInHand())) cir.setReturnValue(false);
    }

}

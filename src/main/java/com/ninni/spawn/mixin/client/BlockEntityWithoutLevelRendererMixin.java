package com.ninni.spawn.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ninni.spawn.block.PigmentShifterBlock;
import com.ninni.spawn.block.entity.PigmentShifterBlockEntity;
import com.ninni.spawn.registry.SpawnBlocks;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BlockEntityWithoutLevelRendererMixin {
    @Shadow
    @Final
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    @Unique
    private final PigmentShifterBlockEntity pigmentShifter = new PigmentShifterBlockEntity(BlockPos.ZERO, SpawnBlocks.PIGMENT_SHIFTER.defaultBlockState().setValue(PigmentShifterBlock.WATERLOGGED, false));


    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    private void DD$renderShulkerItems(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {

        if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PigmentShifterBlock) {
            ci.cancel();
            this.blockEntityRenderDispatcher.renderItem(pigmentShifter, poseStack, multiBufferSource, i, j);
        }
    }
}
package me.ajh123.metro_rail.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.ajh123.metro_rail.content.minecart.CartLinkingComponent;
import me.ajh123.metro_rail.foundation.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    // interact
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    public void interact(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        World world = entity.getWorld();

        PlayerEntity self = (PlayerEntity)(Object) this;
        ItemStack handItem = self.getStackInHand(hand);

        boolean isMinecart = entity instanceof AbstractMinecartEntity;
        boolean isHoldingChain = handItem.getItem() == Items.CHAIN;
        boolean startingLink = handItem.get(ModComponents.CART_LINKING) == null;
        boolean isLinking = isMinecart && isHoldingChain && self.isSneaking();

        if (isLinking && world.isClient) {
            ci.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (isLinking) {
            if (startingLink) {
                self.sendMessage(Text.literal("Starting minecart link"), true);
                CartLinkingComponent linking = new CartLinkingComponent(entity.getUuid().toString(), true);
                handItem.set(ModComponents.CART_LINKING, linking);
            } else {
                // TODO: store links on minecart entity
                self.sendMessage(Text.literal("Finished minecart link"), true);
                handItem.remove(ModComponents.CART_LINKING);                
            }
            ci.setReturnValue(ActionResult.SUCCESS);
        }
    }
}

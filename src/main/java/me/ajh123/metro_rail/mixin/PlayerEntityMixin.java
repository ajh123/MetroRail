package me.ajh123.metro_rail.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.ajh123.metro_rail.content.minecart.CartLinkingComponent;
import me.ajh123.metro_rail.content.minecart.MinecartLinkable;
import me.ajh123.metro_rail.content.minecart.MinecartLinkable.LinkFailure;
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
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    public void interact(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        World world = entity.getWorld();

        PlayerEntity self = (PlayerEntity)(Object) this;
        ItemStack handItem = self.getStackInHand(hand);

        boolean isMinecart = entity instanceof AbstractMinecartEntity;
        boolean isHoldingChain = handItem.getItem() == Items.CHAIN;
        boolean isHoldingShears = handItem.getItem() == Items.SHEARS;

        boolean startingLink = handItem.get(ModComponents.CART_LINKING) == null;
        boolean isLinking = isMinecart && isHoldingChain && self.isSneaking();
        boolean isUnlinking = isMinecart && isHoldingShears && self.isSneaking();

        if ((isLinking || isUnlinking) && world.isClient) {
            ci.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (isLinking) {
            if (startingLink) {
                self.sendMessage(Text.literal("Starting minecart link"), true);
                CartLinkingComponent linking = new CartLinkingComponent(entity.getUuid().toString(), true);
                handItem.set(ModComponents.CART_LINKING, linking);
                ci.setReturnValue(ActionResult.SUCCESS);
                return;
            } else {
                CartLinkingComponent linkData = handItem.get(ModComponents.CART_LINKING);
                Entity parent = world.getEntity(UUID.fromString(linkData.startingEntityId()));
                LinkFailure failure = LinkFailure.NONE;
                ActionResult result = ActionResult.SUCCESS;

                if (parent == null) {
                    self.sendMessage(Text.literal("Invalid minecart link, parent cart does not exist."), true);
                } else {                    
                    if (parent instanceof MinecartLinkable parentLink) {
                        if (entity instanceof MinecartLinkable childLink) {
                            failure = childLink.metroRail$addParent(parentLink);
                        }
                    }
                }

                switch (failure) {
                    case NONE:
                        self.sendMessage(Text.literal("Finished minecart link"), true);
                        break;
                    case ALREADY_HAS_PARENT:
                        self.sendMessage(Text.literal("You cannot connect this minecart; the source already has a parent."), true);
                        result = ActionResult.FAIL;
                        break;
                    case CANNOT_LINK_TO_SELF:
                        self.sendMessage(Text.literal("You cannot connect this minecart; the source cannot be connected to it self."), true);
                        result = ActionResult.FAIL;
                        break;
                    case ALREADY_HAS_CHILD:
                        self.sendMessage(Text.literal("You cannot connect this minecart; the source already has a child."), true);
                        result = ActionResult.FAIL;
                        break;
                    default:
                        break;
                }

                handItem.remove(ModComponents.CART_LINKING);
                ci.setReturnValue(result);
                return;
            }
        }

        if (isUnlinking) {
            if (entity instanceof MinecartLinkable childLink) {
                childLink.metroRail$unlinkNeighbors();
                self.sendMessage(Text.literal("Unlinked from parent"), true);
                ci.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }
}

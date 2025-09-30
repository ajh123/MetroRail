package me.ajh123.metro_rail.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import me.ajh123.metro_rail.content.minecart.MinecartLinkable;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public class MinecartEntityMixin implements MinecartLinkable {
    @Unique private UUID parent;
    @Unique private UUID child;

    @Override
    public LinkFailure metroRail$addParent(MinecartLinkable other) {
        if (other == this) {
            return LinkFailure.CANNOT_LINK_TO_SELF;
        }

        if (this.parent != null) {
            return LinkFailure.ALREADY_HAS_PARENT;
        }

        if (other.metroRail$getChild() != null) {
            return LinkFailure.SOURCE_ALREADY_HAS_CHILD;
        }

        metroRail$setParentInternal(other.metroRail$getUuid());
        other.metroRail$setChildInternal(this.metroRail$getUuid());

        return LinkFailure.NONE;
    }

    @Override
    public MinecartLinkable metroRail$getParent() {
        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        World world = self.getWorld();
        Entity parent = world.getEntity(this.parent);
        if (parent instanceof MinecartLinkable) {
            return (MinecartLinkable) parent;
        }
        return null;
    }

    @Override
    public MinecartLinkable metroRail$getChild() {
        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        World world = self.getWorld();
        Entity child = world.getEntity(this.child);
        if (child instanceof MinecartLinkable) {
            return (MinecartLinkable) child;
        }
        return null;
    }

    @Override
    public void metroRail$unlinkNeighbors() {
        if (parent != null) {
            metroRail$getParent().metroRail$setChildInternal(null);
            parent = null;
        }

        if (child != null) {
            metroRail$getChild().metroRail$setParentInternal(null);
            child = null;
        }
    }

    @Override
    public List<MinecartLinkable> metroRail$getWholeTrain() {
        List<MinecartLinkable> train = new ArrayList<>();

        // Walk back to head
        MinecartLinkable current = this;
        while (current.metroRail$getParent() != null) {
            current = current.metroRail$getParent();
        }

        // Collect forward to tail
        while (current != null) {
            train.add(current);
            current = current.metroRail$getChild();
        }

        return train;
    }

    @Override
    public MinecartLinkable metroRail$getHead() {
        MinecartLinkable current = this;
        while (current.metroRail$getParent() != null) {
            current = current.metroRail$getParent();
        }
        return current;
    }

    @Override
    public MinecartLinkable metroRail$getTail() {
        MinecartLinkable current = this;
        while (current.metroRail$getChild() != null) {
            current = current.metroRail$getChild();
        }
        return current;
    }

    @Override
    public UUID metroRail$getUuid() {
        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        if (self.getUuid() != null) {
            return self.getUuid();
        }
        return null;
    }

    @Unique
    public void metroRail$setChildInternal(UUID child) {
        this.child = child;
    }

    @Unique
    public void metroRail$setParentInternal(UUID parent) {
        this.parent = parent;
    }

    @Inject(method = "writeCustomData", at = @At("HEAD"))
    public void writeCustomData(WriteView view, CallbackInfo ci) {
        if (parent != null) {
            view.putString("MR-Parent", parent.toString());
        }

        if (child != null) {
            view.putString("MR-Child", child.toString());
        }
    }

    @Inject(method = "readCustomData", at = @At("HEAD"))
    public void readCustomData(ReadView view, CallbackInfo ci) {
        if (view.contains("MR-Parent")) {
            String parentId = view.getString("MR-Parent", null);
            if (parentId != null) {
                UUID parentUuid = UUID.fromString(parentId);
                this.metroRail$setParentInternal(parentUuid);
            }
        }

        if (view.contains("MR-Child")) {
            String childId = view.getString("MR-Child", null);
            if (childId != null) {
                UUID childUuid = UUID.fromString(childId);
                this.metroRail$setChildInternal(childUuid);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickLinkedTrain(CallbackInfo ci) {
        AbstractMinecartEntity self = (AbstractMinecartEntity)(Object)this;

        if (parent == null) {
            // Head drives the train
            pullChildChain(self);
        }
    }

    @Unique
    private void pullChildChain(AbstractMinecartEntity parentEntity) {
        MinecartLinkable childLink = ((MinecartLinkable) parentEntity).metroRail$getChild();
        if (childLink == null) return;

        AbstractMinecartEntity child = (AbstractMinecartEntity) childLink;

        // Desired spacing between carts
        double desiredDistance = 1.5;

        // Compute horizontal vector from child to parent
        double dx = parentEntity.getX() - child.getX();
        double dz = parentEntity.getZ() - child.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 0) {
            // Calculate the movement needed to maintain spacing
            double targetDistance = distance - desiredDistance;
            double moveFactor = targetDistance / distance;

            double moveX = dx * moveFactor;
            double moveZ = dz * moveFactor;

            // Force-position the child along the rail respecting collisions
            child.move(MovementType.SELF, new Vec3d(moveX, 0, moveZ));

            // Reset velocity to prevent child from pushing parent
            child.setVelocity(Vec3d.ZERO);
            child.velocityModified = true;
        }

        // Recursive call for the next child
        pullChildChain(child);
    }
}

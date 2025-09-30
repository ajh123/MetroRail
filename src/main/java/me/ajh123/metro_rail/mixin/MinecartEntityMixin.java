package me.ajh123.metro_rail.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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
    @Unique
    private MinecartLinkable parent;

    @Unique
    private MinecartLinkable child;

    @Override
    public LinkFailure metroRail$addParent(MinecartLinkable other) {
        if (other == this) {
            return LinkFailure.CANNOT_LINK_TO_SELF;
        }

        if (this.parent != null) {
            return LinkFailure.ALREADY_HAS_PARENT;
        }

        if (other.metroRail$getChild() != null) {
            return LinkFailure.ALREADY_HAS_CHILD;
        }

        setParentInternal(other);
        ((MinecartEntityMixin) other).setChildInternal(this);

        return LinkFailure.NONE;
    }

    @Override
    public MinecartLinkable metroRail$getParent() {
        return parent;
    }

    @Override
    public MinecartLinkable metroRail$getChild() {
        return child;
    }

    @Override
    public boolean metroRail$unlinkNeighbors() {
        if (parent != null) {
            ((MinecartEntityMixin) parent).child = null;
            parent = null;
        }

        if (child != null) {
            ((MinecartEntityMixin) child).parent = null;
            child = null;
        }

        return true;
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
    private void setChildInternal(MinecartLinkable child) {
        this.child = child;
    }

    @Unique
    private void setParentInternal(MinecartLinkable parent) {
        this.parent = parent;
    }

    @Unique
    private void setChildInternal(Entity child) {
        if (child instanceof MinecartLinkable childLink) {
            setChildInternal(childLink);
        }
    }

    @Unique
    private void setParentInternal(Entity parent) {
        if (parent instanceof MinecartLinkable parentLink) {
            setParentInternal(parentLink);
        }
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    public void writeCustomData(WriteView view, CallbackInfo ci) {
        if (parent != null) {
            view.putString("Parent", parent.metroRail$getUuid().toString());
        }

        if (child != null) {
            view.putString("Child", child.metroRail$getUuid().toString());
        }
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    public void readCustomData(ReadView view, CallbackInfo ci) {
        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        World world = self.getWorld();

        if (world == null) {
            return; // World is not available, cannot restore links
        }

        if (view.contains("Parent")) {
            String parentId = view.getString("Parent", null);
            if (parentId != null) {
                UUID parentUuid = UUID.fromString(parentId);
                Entity parentEntity = world.getEntity(parentUuid);
                this.setParentInternal(parentEntity);
            }
        }

        if (view.contains("Child")) {
            String childId = view.getString("Child", null);
            if (childId != null) {
                UUID childUuid = UUID.fromString(childId);
                Entity childEntity = world.getEntity(childUuid);
                this.setChildInternal(childEntity);
            }
        }
    }
}

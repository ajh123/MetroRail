package me.ajh123.metro_rail.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;

import me.ajh123.metro_rail.content.minecart.MinecartLinkable;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractMinecartEntity.class)
public class MinecartEntityMixin implements MinecartLinkable {
    @Unique
    private MinecartLinkable parent; // TODO: store parent on minecart entity with NBT

    @Override
    public LinkFailure metroRail$addParent(MinecartLinkable other) {
        if (other == parent) {
            return LinkFailure.CANNOT_LINK_TO_SELF;
        }

        if (parent != null) {
            return LinkFailure.ALREADY_HAS_PARENT;
        }

        this.parent = other;

        return LinkFailure.NONE;
    }

    @Override
    public List<MinecartLinkable> metroRail$getChildren() {
        System.out.println("Get Children");
        // TODO: figure this out
        return null;
    }

    @Override
    public MinecartLinkable metroRail$getParent() {
        return parent;
    }

    @Override
    public boolean metroRail$unlinkNeighbors() {
        this.parent = null;
        // TODO: unlink immedieate child
        return true;
    }
}

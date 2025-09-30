package me.ajh123.metro_rail.content.minecart;

import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.UUID;

public interface MinecartLinkable {
    /**
     * Attempt to attach this minecart to the given parent.
     * Updates both sides of the relationship if successful.
     */
    @Unique
    LinkFailure metroRail$addParent(MinecartLinkable other);

    /** The immediate parent of this cart, or null if none. */
    @Unique
    MinecartLinkable metroRail$getParent();

    /** The immediate child of this cart, or null if none. */
    @Unique
    MinecartLinkable metroRail$getChild();

    /**
     * Break links to both parent and child.
     */
    @Unique
    void metroRail$unlinkNeighbors();

    /** All minecarts in the same train, ordered from head to tail. */
    @Unique
    List<MinecartLinkable> metroRail$getWholeTrain();

    /** The head cart (furthest ancestor). */
    @Unique
    MinecartLinkable metroRail$getHead();

    /** The tail cart (furthest descendant). */
    @Unique
    MinecartLinkable metroRail$getTail();

    /** The UUID of this cart's entity. */
    @Unique
    UUID metroRail$getUuid();

    /** Internal method to set the child without restrictions */
    @Unique
    void metroRail$setChildInternal(UUID child);

    /** Internal method to set the parent without restrictions */
    @Unique
    void metroRail$setParentInternal(UUID parent);

    enum LinkFailure {
        NONE,
        ALREADY_HAS_PARENT,
        SOURCE_ALREADY_HAS_CHILD,
        CANNOT_LINK_TO_SELF
    }
}


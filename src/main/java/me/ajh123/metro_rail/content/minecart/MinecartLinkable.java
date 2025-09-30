package me.ajh123.metro_rail.content.minecart;

import java.util.List;

public interface MinecartLinkable {
    LinkFailure metroRail$addParent(MinecartLinkable other);
    List<MinecartLinkable> metroRail$getChildren();
    MinecartLinkable metroRail$getParent();
    boolean metroRail$unlinkNeighbors();

    enum LinkFailure {
        NONE,
        ALREADY_HAS_PARENT,
        CANNOT_LINK_TO_SELF
    }
}

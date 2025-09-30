package me.ajh123.metro_rail.content.minecart;

import java.util.List;

public interface MinecartLinkable {
    LinkFailure addParent(MinecartLinkable other);
    List<MinecartLinkable> getChildren();
    MinecartLinkable getParent();
    boolean unlinkNeighbors();

    public static enum LinkFailure {
        NONE,
        ALREADY_HAS_PARENT,
        CANNOT_LINK_TO_SELF
    }
}

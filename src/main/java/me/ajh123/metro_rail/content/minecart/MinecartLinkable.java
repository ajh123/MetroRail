package me.ajh123.metro_rail.content.minecart;

import java.util.List;

public interface MinecartLinkable {
    // TODO: implement this interface with mixin and store links on minecart entity
    void addParent(MinecartLinkable other);
    List<MinecartLinkable> getChildren();
    MinecartLinkable getParent();
}

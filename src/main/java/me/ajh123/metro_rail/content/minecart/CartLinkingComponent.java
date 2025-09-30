package me.ajh123.metro_rail.content.minecart;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CartLinkingComponent(
    String startingEntityId,
    boolean linkStarted
) {

    public static final Codec<CartLinkingComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("startingEntityId").forGetter(CartLinkingComponent::startingEntityId),
            Codec.BOOL.fieldOf("linkStarted").forGetter(CartLinkingComponent::linkStarted)
    ).apply(builder, CartLinkingComponent::new));
}

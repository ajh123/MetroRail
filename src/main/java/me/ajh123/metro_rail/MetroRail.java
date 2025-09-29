package me.ajh123.metro_rail;

import me.ajh123.metro_rail.foundation.Registration;
import me.ajh123.metro_rail.networking.GetTicketPayload;
import me.ajh123.metro_rail.utils.TaskScheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class MetroRail implements ModInitializer {
    public static final String MOD_ID = "metro_rail";

    @Override
    public void onInitialize() {
        Registration.initialise();
        TaskScheduler.init();
    }
}

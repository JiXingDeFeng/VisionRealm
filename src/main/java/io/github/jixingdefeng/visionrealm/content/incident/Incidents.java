package io.github.jixingdefeng.visionrealm.content.incident;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistry;
import io.github.jixingdefeng.visionrealm.core.util.selector.TargetSelectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class Incidents {
    public static final DeferredRegister<RegisteredIncident<?, ?>> INCIDENTS = DeferredRegister.create(ModRegistry.INCIDENT, VisionRealm.MOD_ID);

    public static final DeferredHolder<RegisteredIncident<?, ?>, RegisteredIncident<?, ?>> TEST_INCIDENT = INCIDENTS.register(
            "test_incident",
            () -> new RegisteredIncident.Builder<>(
                    context -> {
                        ServerLevel level = context.getLevel();
                        VisionRealm.LOGGER.warn("\n\n—————————————— TestIncident ——————————————\nLevel: {}", level);
                        for (Entity entity : context.getAllTarget()) {
                            VisionRealm.LOGGER.warn("Entity: {}", entity);
                        }

                        VisionRealm.LOGGER.warn("——————————————————————————————————————————");
                        return true;
                    },
                    (level, random) -> {
                        List<ServerPlayer> players = level.players();
                        TargetSelector<Entity, ?> selector = TargetSelectors.entityAll(level);

                        if (!players.isEmpty()) {
                            Player player = players.get(random.nextInt(players.size()));
                            selector.centerAt(l -> player.position());
                        }

                        return selector
                                .inRange(Vec3.ZERO, 20)
                                .randomObtain(random);
                    }
            ).multiExtractor(TargetSelector::toList)
                    .runInAllDimensions()
                    .build()
    );

    public static void registry(IEventBus bus) {
        INCIDENTS.register(bus);
    }
}

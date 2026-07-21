package io.github.jixingdefeng.visionrealm.core.incident;

import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Incidents {
    public static final DeferredRegister<RegisteredIncident<?, ?>> INCIDENTS = DeferredRegister.create(ModRegistry.INCIDENT, VisionRealm.MOD_ID);

//    public static final DeferredHolder<RegisteredIncident<?, ?>, RegisteredIncident<?, ?>> TEST_INCIDENT = INCIDENTS.register(
//            "test_incident",
//            () -> new RegisteredIncident.Builder<>(
//                    context -> {
//                        ServerLevel level = context.getLevel();
//                        VisionRealm.LOGGER.warn("\n\n—————————————— TestIncident ——————————————\nLevel: {}", level);
//                        for (Entity entity : context.getAllSource()) {
//                            VisionRealm.LOGGER.warn("Entity: {}", entity);
//                        }
//
//                        VisionRealm.LOGGER.warn("——————————————————————————————————————————");
//                        return true;
//                    },
//                    (level, random) -> TargetSelectors.entityAll(level)
//                            .inRange(Vec3.ZERO, 0)
//                            .randomObtain(random),
//                    List.of(Level.OVERWORLD)
//            ).extractor(TargetSelector::toList).build()
//    );

    public static void registry(IEventBus bus) {
        INCIDENTS.register(bus);
    }
}

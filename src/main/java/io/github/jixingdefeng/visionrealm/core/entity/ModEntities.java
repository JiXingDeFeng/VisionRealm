package io.github.jixingdefeng.visionrealm.core.entity;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.entity.custom.Administrator;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.ForgottenShadow;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.NightmareApostle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, VisionRealm.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Administrator>> ADMINISTRATOR = ENTITY_TYPES.register(
            "administrator",
            () -> EntityType.Builder.of(Administrator::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.917F)
                    .build("administrator")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ForgottenShadow>> FORGOTTEN_SHADOW = ENTITY_TYPES.register(
            "forgotten_shadow",
            () -> EntityType.Builder.of(ForgottenShadow::new, MobCategory.MONSTER)
                    .sized(0.65F, 2.373F)
                    .build("forgotten_shadow")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<NightmareApostle>> NIGHTMARE_APOSTLE = ENTITY_TYPES.register(
            "nightmare_apostle",
            () -> EntityType.Builder.of(NightmareApostle::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.814F)
                    .build("nightmare_apostle")
    );

    public static void registry(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}

package io.github.fengguoshuzhu.visionrealm.core.entity;

import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import io.github.fengguoshuzhu.visionrealm.core.entity.custom.AdministratorEntity;
import io.github.fengguoshuzhu.visionrealm.core.entity.custom.monster.TheForgottenEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Entities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, VisionRealm.MOD_ID);

    public static final Supplier<EntityType<AdministratorEntity>> ADMINISTRATOR = ENTITY_TYPES.register(
            "administrator",
            () -> EntityType.Builder.of(AdministratorEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.85F)
                    .build("administrator")
    );
    public static final Supplier<EntityType<TheForgottenEntity>> WANDERER = ENTITY_TYPES.register(
            "the_forgotten",
            () -> EntityType.Builder.of(TheForgottenEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 1.5F)
                    .build("the_forgotten")
    );

    public static void registry(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}

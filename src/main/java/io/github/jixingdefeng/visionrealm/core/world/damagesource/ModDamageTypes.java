package io.github.jixingdefeng.visionrealm.core.world.damagesource;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> SPIRIT_EROSION = ResourceKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "spirit_erosion")
    );

    public static Optional<Holder.Reference<DamageType>> getDamageType(Level level, ResourceKey<DamageType> resourceKey) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(resourceKey);
    }
}

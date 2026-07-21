package io.github.jixingdefeng.visionrealm.core.sound;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, VisionRealm.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> FORGOTTEN_SHADOW_HURT = register("forgotten_shadow_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FORGOTTEN_SHADOW_DEATH = register("forgotten_shadow_death");

    // 语音
    public static final DeferredHolder<SoundEvent, SoundEvent> OBSERVATION_ADMIN_RENAMED = register("observation_admin_renamed");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}

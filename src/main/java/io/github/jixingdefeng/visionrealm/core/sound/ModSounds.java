package io.github.jixingdefeng.visionrealm.core.sound;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, VisionRealm.MOD_ID);

    public static final Supplier<SoundEvent> THE_FORGOTTEN_HURT = register("the_forgotten_hurt");
    public static final Supplier<SoundEvent> THE_FORGOTTEN_DEATH = register("the_forgotten_death");

    // 语音
    public static final Supplier<SoundEvent> OBSERVATION_ADMIN_RENAMED = register("observation_admin_renamed");

    private static Supplier<SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}

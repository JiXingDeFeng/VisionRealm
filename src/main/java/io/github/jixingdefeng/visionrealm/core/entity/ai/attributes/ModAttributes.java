package io.github.jixingdefeng.visionrealm.core.entity.ai.attributes;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, VisionRealm.MOD_ID);

    public static final Holder<Attribute> SANITY = ATTRIBUTES.register(
            "player.sane",
            () -> new RangedAttribute("attribute.name.player.sane", 100, 0, 1000).setSyncable(true)
    );

    public static void registry(IEventBus bus) {
        ATTRIBUTES.register(bus);
    }
}

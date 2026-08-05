package io.github.jixingdefeng.visionrealm.content.world.item;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.content.world.level.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VisionRealm.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS = CREATIVE_MODE_TABS
            .register("visionrealm_block", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.visionrealm.block.title"))
                    .icon(() -> new ItemStack(ModBlocks.BLOOD_CORRODED_CHERRY_LOG.get()))
                    .displayItems(((parameters, output) -> {
                        output.accept(ModBlocks.ERROR_BLOCK);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_LOG);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_WOOD);
                        output.accept(ModBlocks.STRIPPED_BLOOD_CORRODED_CHERRY_LOG);
                        output.accept(ModBlocks.STRIPPED_BLOOD_CORRODED_CHERRY_WOOD);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_PETALS);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_STAIRS);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_SLAB);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_DOOR);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_FENCE);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_FENCE_GATE);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_SAPLING);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_LEAVES);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_PLANKS);
                        output.accept(ModItems.BLOOD_CORRODED_CHERRY_WALL_SIGN);
                        output.accept(ModItems.BLOOD_CORRODED_CHERRY_WALL_HANGING_SIGN);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_BUTTON);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_PRESSURE_PLATE);
                        output.accept(ModBlocks.BLOOD_CORRODED_CHERRY_TRAPDOOR);
                        output.accept(ModBlocks.CURSED_CACTUS);
                    }))
                    .build());
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ENTITY = CREATIVE_MODE_TABS
            .register("visionrealm_entity", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.visionrealm.entity.title"))
                    .icon(() -> new ItemStack(ModItems.FORGOTTEN_SHADOW_SPAWN_EGG.get()))
                    .displayItems(((parameters, output) -> {
                        output.accept(ModItems.OBSERVATION_ADMIN_SPAWN_EGG);
                        output.accept(ModItems.INTERFERENCE_ADMIN_SPAWN_EGG);
                        output.accept(ModItems.FORGOTTEN_SHADOW_SPAWN_EGG);
                    }))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS = CREATIVE_MODE_TABS
            .register("visionrealm_item", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.visionrealm.item.title"))
                    .icon(() -> new ItemStack(ModItems.CONVEYOR_ITEM.get()))
                    .displayItems(((parameters, output) -> {
                        output.accept(ModItems.ERROR);
                        output.accept(ModItems.CONVEYOR_ITEM);
                    }))
                    .build());

    public static void registry(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

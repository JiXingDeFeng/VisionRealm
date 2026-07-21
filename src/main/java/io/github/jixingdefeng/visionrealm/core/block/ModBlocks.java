package io.github.jixingdefeng.visionrealm.core.block;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.block.grower.ModTreeGrower;
import io.github.jixingdefeng.visionrealm.core.block.state.BlockBehaviourExpand;
import io.github.jixingdefeng.visionrealm.core.block.state.properties.ModBlockSetTypes;
import io.github.jixingdefeng.visionrealm.core.block.state.properties.ModWoodTypes;
import io.github.jixingdefeng.visionrealm.core.item.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VisionRealm.MOD_ID);

    // —————————————————————————————— 自定义方块 ——————————————————————————————
    public static final DeferredBlock<Block> ERROR_BLOCK = registerBlockWithItem(
            "error_block",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .sound(SoundType.STONE)
                            .noLootTable()
                            .strength(10F)
            )
    );

    // 只注册方块

    // —————————————————————————————— 原版方块变体 ——————————————————————————————
    public static final DeferredBlock<Block> ANOMALY_CACTUS = registerBlockWithItem(
            "anomaly_cactus",
            () -> new AnomalyCactusBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CACTUS)
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_SAPLING = registerBlockWithItem(
            "blood_corroded_cherry_sapling",
            () -> new SaplingBlock(
                    ModTreeGrower.BLOOD_CORRODED_CHERRY,
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_SAPLING)
                            .mapColor(MapColor.COLOR_RED)
                            .sound(SoundType.CHERRY_SAPLING)
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_LOG = registerBlockWithItem(
            "blood_corroded_cherry_log",
            () -> log(MapColor.TERRACOTTA_RED, MapColor.TERRACOTTA_GRAY, SoundType.CHERRY_WOOD, false, true)
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_WOOD = registerBlockWithItem(
            "blood_corroded_cherry_wood",
            () -> new RotatedPillarBlock(
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_WOOD)
                            .sound(SoundType.CHERRY_WOOD)
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_LEAVES = registerBlockWithItem(
            "blood_corroded_cherry_leaves",
            () -> new BloodCorrodedCherryLeavesBlock(
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_LEAVES)
                            .mapColor(MapColor.COLOR_RED)
                            .sound(SoundType.CHERRY_LEAVES)
                    )
            )
    );
    public static final DeferredBlock<Block> STRIPPED_BLOOD_CORRODED_CHERRY_LOG = registerBlockWithItem(
            "stripped_blood_corroded_cherry_log",
            () -> log(MapColor.TERRACOTTA_RED, MapColor.TERRACOTTA_RED, SoundType.CHERRY_WOOD, false, true)
    );
    public static final DeferredBlock<Block> STRIPPED_BLOOD_CORRODED_CHERRY_WOOD = registerBlockWithItem(
            "stripped_blood_corroded_cherry_wood",
            () -> new RotatedPillarBlock(
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_CHERRY_WOOD)
                            .mapColor(MapColor.TERRACOTTA_RED)
                            .sound(SoundType.CHERRY_WOOD)
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_PLANKS = registerBlockWithItem(
            "blood_corroded_cherry_planks",
            () -> new Block(
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS)
                            .mapColor(MapColor.TERRACOTTA_RED)
                            .sound(SoundType.CHERRY_WOOD)
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_PRESSURE_PLATE = registerBlockWithItem(
            "blood_corroded_cherry_pressure_plate",
            () -> new PressurePlateBlock(
                    ModBlockSetTypes.BLOOD_CORRODED_CHERRY,
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PRESSURE_PLATE)
                            .mapColor(BLOOD_CORRODED_CHERRY_PLANKS.get().defaultMapColor())
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_TRAPDOOR = registerBlockWithItem(
            "blood_corroded_cherry_trapdoor",
            () -> new TrapDoorBlock(
                    ModBlockSetTypes.BLOOD_CORRODED_CHERRY,
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_TRAPDOOR)
                            .mapColor(MapColor.TERRACOTTA_PINK)
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_BUTTON = registerBlockWithItem(
            "blood_corroded_cherry_button", woodenButton(ModBlockSetTypes.BLOOD_CORRODED_CHERRY, true)
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_STAIRS = registerBlockWithItem(
            "blood_corroded_cherry_stairs", stair(BLOOD_CORRODED_CHERRY_PLANKS, true)
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_SLAB = registerBlockWithItem(
            "blood_corroded_cherry_slab",
            () -> new SlabBlock(
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_SLAB)
                            .mapColor(MapColor.TERRACOTTA_PINK)
                            .sound(SoundType.CHERRY_WOOD)
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_FENCE_GATE = registerBlockWithItem(
            "blood_corroded_cherry_fence_gate",
            () -> new FenceGateBlock(
                    ModWoodTypes.BLOOD_CORRODED_CHERRY,
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_FENCE_GATE)
                            .mapColor(BLOOD_CORRODED_CHERRY_PLANKS.get().defaultMapColor())
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_FENCE = registerBlockWithItem(
            "blood_corroded_cherry_fence",
            () -> new FenceBlock(
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_FENCE)
                            .mapColor(BLOOD_CORRODED_CHERRY_PLANKS.get().defaultMapColor())
                            .sound(SoundType.CHERRY_WOOD)
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_DOOR = registerBlockWithItem(
            "blood_corroded_cherry_door",
            () -> new DoorBlock(
                    BlockSetType.CHERRY,
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_DOOR)
                            .mapColor(BLOOD_CORRODED_CHERRY_PLANKS.get().defaultMapColor())
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_PETALS = registerBlockWithItem(
            "blood_corroded_cherry_petals",
            () -> new PinkPetalsBlock(
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_PETALS)
                            .mapColor(MapColor.TERRACOTTA_RED)
                    )
            )
    );

    // 只注册方块，物品单独注册
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_SIGN = registerBlock(
            "blood_corroded_cherry_sign",
            () -> new StandingSignBlock(
                    ModWoodTypes.BLOOD_CORRODED_CHERRY,
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_SIGN)
                            .mapColor(BLOOD_CORRODED_CHERRY_PLANKS.get().defaultMapColor())
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_HANGING_SIGN = registerBlock(
            "blood_corroded_cherry_hanging_sign",
            () -> new CeilingHangingSignBlock(
                    ModWoodTypes.BLOOD_CORRODED_CHERRY,
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_HANGING_SIGN)
                            .mapColor(MapColor.TERRACOTTA_RED)
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_WALL_SIGN = registerBlock(
            "blood_corroded_cherry_wall_sign",
            () -> new WallSignBlock(
                    ModWoodTypes.BLOOD_CORRODED_CHERRY,
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_WALL_SIGN)
                            .mapColor(BLOOD_CORRODED_CHERRY_LOG.get().defaultMapColor())
                            .lootFrom(BLOOD_CORRODED_CHERRY_SIGN)
                    )
            )
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_CHERRY_WALL_HANGING_SIGN = registerBlock(
            "blood_corroded_cherry_wall_hanging_sign",
            () -> new WallHangingSignBlock(
                    ModWoodTypes.BLOOD_CORRODED_CHERRY,
                    immuneErosion(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_WALL_HANGING_SIGN)
                            .mapColor(MapColor.TERRACOTTA_RED)
                            .lootFrom(BLOOD_CORRODED_CHERRY_HANGING_SIGN)
                    )
            )
    );

    // 只注册方块，不需要注册物品
    public static final DeferredBlock<Block> POTTED_ANOMALY_CACTUS = registerBlock(
            "potted_anomaly_cactus", flowerPot(ANOMALY_CACTUS, false)
    );
    public static final DeferredBlock<Block> BLOOD_CORRODED_POTTED_CHERRY_SAPLING = registerBlock(
            "blood_corroded_potted_cherry_sapling", flowerPot(BLOOD_CORRODED_CHERRY_SAPLING, true)
    );


    public static BlockBehaviour.Properties immuneErosion(BlockBehaviour.Properties properties) {
        return ((BlockBehaviourExpand.PropertiesExpand) properties).immuneErosion();
    }

    private static Block log(MapColor topMapColor, MapColor sideMapColor, SoundType soundType, boolean ignitedByLava, boolean immuneErosion) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y
                                   ? topMapColor
                                   : sideMapColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .sound(soundType);
        if (ignitedByLava) properties.ignitedByLava();
        if (immuneErosion) properties = immuneErosion(properties);
        return new RotatedPillarBlock(properties);
    }

    @SuppressWarnings("deprecation")
    private static Supplier<Block> flowerPot(Supplier<Block> potted, boolean immuneErosion) {
        return () -> new FlowerPotBlock(
                potted.get(), immuneErosion ? immuneErosion(flowerPot()) : flowerPot()
        );
    }

    private static BlockBehaviour.Properties flowerPot() {
        return BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    private static Supplier<Block> woodenButton(BlockSetType type, boolean immuneErosion) {
        return () -> new ButtonBlock(
                type, 30, immuneErosion ? immuneErosion(woodenButton()) : woodenButton()
        );
    }

    private static BlockBehaviour.Properties woodenButton() {
        return BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY);
    }

    private static Supplier<Block> stair(Supplier<Block> baseBlock, boolean immuneErosion) {
        return () -> new StairBlock(
                baseBlock.get().defaultBlockState(),
                immuneErosion
                ? immuneErosion(BlockBehaviour.Properties.ofFullCopy(baseBlock.get()))
                : BlockBehaviour.Properties.ofFullCopy(baseBlock.get())
        );
    }

    private static <T extends Block> DeferredBlock<T> registerBlockWithItem(String name, Supplier<T> block) {
        DeferredBlock<T> block1 = registerBlock(name, block);
        registerItemForBlock(name, block1);
        return block1;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> void registerItemForBlock(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void registry(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

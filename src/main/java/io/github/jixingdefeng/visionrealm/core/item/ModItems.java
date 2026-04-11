package io.github.jixingdefeng.visionrealm.core.item;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.block.ModBlocks;
import io.github.jixingdefeng.visionrealm.core.entity.Entities;
import io.github.jixingdefeng.visionrealm.core.entity.custom.AdministratorEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VisionRealm.MOD_ID);

    // 工具

    // 生物蛋
    public static final DeferredItem<Item> OBSERVATION_ADMIN_SPAWN_EGG = ITEMS.register(
            "observation_administrator_spawn_egg",
            () -> new InitializedSpawnEggItem(
                    Entities.ADMINISTRATOR,
                    0xFF00FF,
                    0x000000,
                    false,
                    new Item.Properties(),
                    (player, entity, pos) -> {
                        if (entity instanceof AdministratorEntity mob) {
                            AdministratorEntity.initEntity(mob, player, AdministratorEntity.BehaviorMode.OBSERVATION);
                        }
                    }
            )
    );
    public static final DeferredItem<Item> INTERFERENCE_ADMIN_SPAWN_EGG = ITEMS.register(
            "interference_administrator_spawn_egg",
            () -> new InitializedSpawnEggItem(
                    Entities.ADMINISTRATOR,
                    0xFF00FF,
                    0x000000,
                    false,
                    new Item.Properties(),
                    (player, entity, pos) -> {
                        if (entity instanceof AdministratorEntity mob) {
                            AABB aabb = entity.getBoundingBox().inflate(50);
                            List<Player> players = player.level().getEntitiesOfClass(Player.class, aabb, LivingEntity::isAlive);
                            AdministratorEntity.initEntity(mob, players.get(player.getRandom().nextInt(players.size())), AdministratorEntity.BehaviorMode.INTERFERENCE);
                        }
                    }
            )
    );
    public static final DeferredItem<Item> THE_FORGOTTEN_SPAWN_EGG = ITEMS.register(
            "the_forgotten_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    Entities.WANDERER,
                    0x000000,
                    0x808080,
                    new Item.Properties()
            )
    );

    // 方块物品
    public static final DeferredItem<Item> LURKER_DISGUISE_BLOCK = ITEMS.register(
            "lurker_disguise_block",
            () -> new StandingAndWallBlockItem(
                    ModBlocks.CustomBlocks.LURKER_DISGUISE_BLOCK.get(),
                    ModBlocks.CustomBlocks.LURKER_DISGUISE_WALL_BLOCK.get(),
                    new Item.Properties(),
                    Direction.NORTH
            )
    );
    public static final DeferredItem<Item> BLOOD_CORRODED_CHERRY_WALL_SIGN = ITEMS.register(
            "blood_corroded_cherry_sign",
            () -> new SignItem(
                    new Item.Properties(),
                    ModBlocks.VanillaBlockVariants.BLOOD_CORRODED_CHERRY_SIGN.get(),
                    ModBlocks.VanillaBlockVariants.BLOOD_CORRODED_CHERRY_WALL_SIGN.get()
            )
    );
    public static final DeferredItem<Item> BLOOD_CORRODED_CHERRY_WALL_HANGING_SIGN = ITEMS.register(
            "blood_corroded_cherry_hanging_sign",
            () -> new HangingSignItem(
                    ModBlocks.VanillaBlockVariants.BLOOD_CORRODED_CHERRY_HANGING_SIGN.get(),
                    ModBlocks.VanillaBlockVariants.BLOOD_CORRODED_CHERRY_WALL_HANGING_SIGN.get(),
                    new Item.Properties()
            )
    );

    public static void registry(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

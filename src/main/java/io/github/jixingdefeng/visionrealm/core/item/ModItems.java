package io.github.jixingdefeng.visionrealm.core.item;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.block.ModBlocks;
import io.github.jixingdefeng.visionrealm.core.entity.ModEntities;
import io.github.jixingdefeng.visionrealm.core.entity.custom.Administrator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VisionRealm.MOD_ID);

    public static final DeferredItem<Item> TEST_ITEM = ITEMS.register(
            "test_item",
            () -> new TestItem(new Item.Properties())
    );

    // 工具

    // 生物蛋
    public static final DeferredItem<Item> OBSERVATION_ADMIN_SPAWN_EGG = ITEMS.register(
            "observation_administrator_spawn_egg",
            () -> new InitializedSpawnEggItem(
                    ModEntities.ADMINISTRATOR,
                    0xFF00FF,
                    0x000000,
                    false,
                    new Item.Properties(),
                    (player, entity, pos) -> {
                        if (entity instanceof Administrator mob) {
                            Administrator.initEntity(mob, player, Administrator.BehaviorMode.OBSERVATION);
                        }
                    }
            )
    );
    public static final DeferredItem<Item> INTERFERENCE_ADMIN_SPAWN_EGG = ITEMS.register(
            "interference_administrator_spawn_egg",
            () -> new InitializedSpawnEggItem(
                    ModEntities.ADMINISTRATOR,
                    0xFF00FF,
                    0x000000,
                    false,
                    new Item.Properties(),
                    (player, entity, pos) -> {
                        if (entity instanceof Administrator mob) {
                            AABB aabb = entity.getBoundingBox().inflate(50);
                            List<Player> players = player.level().getEntitiesOfClass(Player.class, aabb, LivingEntity::isAlive);
                            Administrator.initEntity(mob, players.get(player.getRandom().nextInt(players.size())), Administrator.BehaviorMode.INTERFERENCE);
                        }
                    }
            )
    );
    public static final DeferredItem<Item> FORGOTTEN_SHADOW_SPAWN_EGG = ITEMS.register(
            "forgotten_shadow_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.FORGOTTEN_SHADOW,
                    0x000000,
                    0x808080,
                    new Item.Properties()
            )
    );

    // 方块物品
    public static final DeferredItem<Item> BLOOD_CORRODED_CHERRY_WALL_SIGN = ITEMS.register(
            "blood_corroded_cherry_sign",
            () -> new SignItem(
                    new Item.Properties(),
                    ModBlocks.BLOOD_CORRODED_CHERRY_SIGN.get(),
                    ModBlocks.BLOOD_CORRODED_CHERRY_WALL_SIGN.get()
            )
    );
    public static final DeferredItem<Item> BLOOD_CORRODED_CHERRY_WALL_HANGING_SIGN = ITEMS.register(
            "blood_corroded_cherry_hanging_sign",
            () -> new HangingSignItem(
                    ModBlocks.BLOOD_CORRODED_CHERRY_HANGING_SIGN.get(),
                    ModBlocks.BLOOD_CORRODED_CHERRY_WALL_HANGING_SIGN.get(),
                    new Item.Properties()
            )
    );

    public static void registry(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

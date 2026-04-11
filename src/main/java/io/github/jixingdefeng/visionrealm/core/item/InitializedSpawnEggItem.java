package io.github.jixingdefeng.visionrealm.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class InitializedSpawnEggItem extends DeferredSpawnEggItem {
    private final boolean supportsSpawnCages;
    private final EntityInitialize initialize;

    public InitializedSpawnEggItem(
            Supplier<? extends EntityType<? extends Mob>> entityType,
            int backgroundColor,
            int highlightColor,
            boolean supportsSpawnCages,
            Properties props,
            EntityInitialize initialize
    ) {
        super(entityType, backgroundColor, highlightColor, props);
        this.initialize = initialize;
        this.supportsSpawnCages = supportsSpawnCages;
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack itemStack = context.getItemInHand();
            BlockPos blockPos = context.getClickedPos();
            Direction direction = context.getClickedFace();
            BlockState blockState = level.getBlockState(blockPos);
            if (this.supportsSpawnCages && level.getBlockEntity(blockPos) instanceof Spawner spawner) {
                EntityType<?> entitytype1 = this.getType(itemStack);
                spawner.setEntityId(entitytype1, level.getRandom());
                level.sendBlockUpdated(blockPos, blockState, blockState, 3);
                level.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
                itemStack.shrink(1);
                return InteractionResult.CONSUME;
            } else {
                BlockPos blockPos1;
                if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
                    blockPos1 = blockPos;
                } else {
                    blockPos1 = blockPos.relative(direction);
                }

                EntityType<?> entitytype = this.getType(itemStack);
                Entity entity = entitytype.spawn(
                        serverLevel,
                        itemStack,
                        context.getPlayer(),
                        blockPos1,
                        MobSpawnType.SPAWN_EGG,
                        true,
                        !Objects.equals(blockPos, blockPos1) && direction == Direction.UP
                );
                if (entity != null) {
                    itemStack.shrink(1);
                    level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
                    this.initialize.init(context.getPlayer(), entity, blockPos1);
                }

                return InteractionResult.CONSUME;
            }
        }
    }

    @Override
    public boolean spawnsEntity(@NotNull ItemStack stack, @NotNull EntityType<?> entityType) {
        return super.spawnsEntity(stack, entityType);
    }

    @FunctionalInterface
    public interface EntityInitialize {
        void init(Player player, Entity entity, BlockPos pos);
    }
}

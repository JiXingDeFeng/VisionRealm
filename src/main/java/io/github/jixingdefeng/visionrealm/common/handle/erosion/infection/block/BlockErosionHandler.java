package io.github.jixingdefeng.visionrealm.common.handle.erosion.infection.block;

import io.github.jixingdefeng.visionrealm.api.erosion.infection.CanBeErosion;
import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionEvent;
import io.github.jixingdefeng.visionrealm.common.context.erosion.ErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.common.handle.erosion.infection.BaseErosionHandle;
import io.github.jixingdefeng.visionrealm.common.util.block.BlockStateHelper;
import io.github.jixingdefeng.visionrealm.common.util.erosion.ErosionUtil;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.BaseBlockErosionKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public class BlockErosionHandler {

    public static boolean tryErosion(Block block, Level level, BlockPos pos, ErosionType type) {
        return process(block, level, pos, type, ErosionUtil::getCanBeErosion);
    }

    public static boolean tryErosion(BaseBlockErosionKey<?, ?> key, Level level, BlockPos pos, ErosionType type) {
        return process(key.getSource(), level, pos, type, source -> key);
    }

    public static <T, R> boolean process(
            Block block,
            Level level,
            BlockPos pos,
            ErosionType type,
            BaseErosionHandle.ErosionWrapper<Block, R, T> wrapper
    ) {
        CanBeErosion<Block, R, T> canBeErosion = wrapper.getErosion(block);
        if (canBeErosion != null && canBeErosion.canBeEroded(type)) {
            return BaseErosionHandle.process(block, level, pos.getCenter(), type, wrapper, new BaseErosionHandle.EventSender<>() {

                @Override
                public BaseErosionHandle.PreEventResult<T> sendPreEvent(Block source, T target, Level level, Vec3 pos, ErosionType type) {
                    BlockErosionEvent.Pre<T> event = new BlockErosionEvent.Pre<>(source, target, level, BlockPos.containing(pos), type);
                    NeoForge.EVENT_BUS.post(event);
                    return BaseErosionHandle.PreEventResult.of(event.getTarget(), !event.isCanceled());
                }

                @Override
                public void sendPostEvent(Block source, R result, T target, Level level, Vec3 pos, ErosionType type, boolean success) {
                    NeoForge.EVENT_BUS.post(new BlockErosionEvent.Post<>(source, result, target, level, BlockPos.containing(pos), type, success));
                }
            });
        } else {
            return false;
        }
    }

    public static BlockState transformBlock(Block target, Level level, BlockPos pos, ErosionType type, ErosionContext<Block> context) {
        BlockState sourceState = level.getBlockState(pos);
        BlockState targetState = target.defaultBlockState();
        targetState = BlockStateHelper.copyProperties(sourceState, targetState);
        level.setBlock(pos, targetState, Block.UPDATE_ALL_IMMEDIATE);
        return targetState;
    }

    public static <T extends Entity> T transformEntity(EntityType<T> target, Level level, BlockPos pos, ErosionType type, ErosionContext<Block> context) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        T entity = target.create(level);
        if (entity != null) {
            entity.setPos(context.pos());
            level.addFreshEntity(entity);
        }

        return entity;
    }
}

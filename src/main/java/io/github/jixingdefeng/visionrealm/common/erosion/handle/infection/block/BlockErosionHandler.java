package io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.block;

import io.github.jixingdefeng.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionEvent;
import io.github.jixingdefeng.visionrealm.common.erosion.context.ErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.BaseErosionHandle;
import io.github.jixingdefeng.visionrealm.common.util.block.BlockStateHelper;
import io.github.jixingdefeng.visionrealm.common.util.erosion.ErosionUtil;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
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
        return process(block, level, pos, type, source -> ErosionUtil.getCanBeErosion(source, level, pos, type));
    }

    public static boolean tryErosion(CanBeErosionBlock<?, ?> key, Level level, BlockPos pos, ErosionType type) {
        return process(key.getSource(), level, pos, type, source -> key);
    }

    public static <RT, R> boolean process(
            Block block,
            Level level,
            BlockPos pos,
            ErosionType type,
            BaseErosionHandle.ErosionWrapper<Block, RT, R, CanBeErosionBlock<RT, R>> wrapper
    ) {
        CanBeErosionBlock<RT, R> canBeErosion = wrapper.getErosion(block);
        if (canBeErosion != null
                && canBeErosion.canBeEroded(level, pos, type)
                && level.random.nextFloat() < canBeErosion.conversionProbability()
        ) {
            return BaseErosionHandle.process(block, level, pos.getCenter(), type, wrapper, new BaseErosionHandle.EventSender<>() {

                @Override
                public BaseErosionHandle.PreEventResult<RT> sendPreEvent(Block source, RT target, Level level, Vec3 pos, ErosionType type) {
                    BlockErosionEvent.Pre<RT> event = new BlockErosionEvent.Pre<>(source, target, level, BlockPos.containing(pos), type);
                    NeoForge.EVENT_BUS.post(event);
                    return BaseErosionHandle.PreEventResult.of(event.getTarget(), !event.isCanceled());
                }

                @Override
                public void sendPostEvent(Block source, R result, RT target, Level level, Vec3 pos, ErosionType type, boolean success) {
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

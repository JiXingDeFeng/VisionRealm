package io.github.fengguoshuzhu.visionrealm.core.block;

import io.github.fengguoshuzhu.visionrealm.core.block.state.properties.ModBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

public class LurkerDisguiseBlock extends TorchBlock {
    protected final SimpleParticleType flameParticle2;

    public LurkerDisguiseBlock(SimpleParticleType flameParticle, SimpleParticleType flameParticle2, Properties properties) {
        super(flameParticle, properties);
        this.flameParticle2 = flameParticle2;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ModBlockStateProperties.HAVE_ENTITY, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ModBlockStateProperties.HAVE_ENTITY);
    }

    @Override
    public void animateTick(@NotNull BlockState state, Level level, BlockPos pos, @NotNull RandomSource random) {
        SimpleParticleType flameParticle = state.getValue(ModBlockStateProperties.HAVE_ENTITY) ? this.flameParticle2 : this.flameParticle;
        double d0 = (double)pos.getX() + 0.5;
        double d1 = (double)pos.getY() + 0.7;
        double d2 = (double)pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
        level.addParticle(flameParticle, d0, d1, d2, 0.0, 0.0, 0.0);
    }

    public static class WallBlock extends WallTorchBlock {
        protected final SimpleParticleType flameParticle2;

        public WallBlock(SimpleParticleType flameParticle, SimpleParticleType flameParticle2, Properties properties) {
            super(flameParticle, properties);
            this.flameParticle2 = flameParticle2;
            this.registerDefaultState(this.stateDefinition.any()
                    .setValue(ModBlockStateProperties.HAVE_ENTITY, false)
            );
        }

        @Override
        protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(ModBlockStateProperties.HAVE_ENTITY);
        }

        @Override
        public void animateTick(@NotNull BlockState state, Level level, BlockPos pos, @NotNull RandomSource random) {
            SimpleParticleType flameParticle = state.getValue(ModBlockStateProperties.HAVE_ENTITY) ? this.flameParticle2 : this.flameParticle;
            Direction direction = state.getValue(FACING);
            double d0 = (double)pos.getX() + 0.5;
            double d1 = (double)pos.getY() + 0.7;
            double d2 = (double)pos.getZ() + 0.5;
            double d3 = 0.22;
            double d4 = 0.27;
            Direction direction1 = direction.getOpposite();
            level.addParticle(
                    ParticleTypes.SMOKE, d0 + d4 * (double)direction1.getStepX(), d1 + d3, d2 + d4 * (double)direction1.getStepZ(), 0.0, 0.0, 0.0
            );
            level.addParticle(
                    flameParticle, d0 + d4 * (double)direction1.getStepX(), d1 + d3, d2 + d4 * (double)direction1.getStepZ(), 0.0, 0.0, 0.0
            );
        }
    }
}

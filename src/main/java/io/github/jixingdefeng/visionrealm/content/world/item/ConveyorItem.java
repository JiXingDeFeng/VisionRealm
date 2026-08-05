package io.github.jixingdefeng.visionrealm.content.world.item;

import io.github.jixingdefeng.visionrealm.api.entity.LivingEntityExtensions;
import io.github.jixingdefeng.visionrealm.content.world.entity.ai.attributes.ModAttributes;
import io.github.jixingdefeng.visionrealm.content.world.level.ModLevels;
import io.github.jixingdefeng.visionrealm.content.world.level.block.ModBlocks;
import io.github.jixingdefeng.visionrealm.core.particle_config.singleton.ModifiableParticleConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

public class ConveyorItem extends Item {
    protected final ModifiableParticleConfig PARTICLE = new ModifiableParticleConfig(ParticleTypes.LAVA, 50, 0, 0.5, 0.5, 0.5, 0, 0, 0);

    public ConveyorItem(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (level instanceof ServerLevel serverLevel) {
            ItemStack itemStack = player.getItemInHand(usedHand);
            IEnergyStorage storage = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (storage != null && storage.getEnergyStored() >= 30) {
                ResourceKey<Level> levelKey = this.getTargetLevel(serverLevel);
                this.teleport(player, serverLevel, levelKey);
                player.getCooldowns().addCooldown(this, 200);
                player.getFoodData().addExhaustion(20);
                storage.extractEnergy(35, false);
            }
        }

        return super.use(level, player, usedHand);
    }

    @NotNull
    @Override
    public InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player != null && player.isShiftKeyDown()) {
            if (level instanceof ServerLevel serverLevel) {
                BlockPos blockpos = context.getClickedPos();
                BlockState blockstate = level.getBlockState(blockpos);
                if (blockstate.is(ModBlocks.ERROR_BLOCK.get())) {
                    ItemStack itemstack = player.getItemInHand(context.getHand());
                    IEnergyStorage storage = itemstack.getCapability(Capabilities.EnergyStorage.ITEM);
                    if (storage != null && storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                        storage.receiveEnergy(10, false);
                        level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 11);
                        this.PARTICLE.particleType(new BlockParticleOption(ParticleTypes.BLOCK, blockstate))
                                .spawnParticles(blockpos.getCenter(), level);
                        serverLevel.playLocalSound(
                                blockpos, SoundEvents.STONE_FALL, SoundSource.BLOCKS, 10.0F, 1.0F, true
                        );
                        return InteractionResult.SUCCESS;
                    }
                }
            } else {
                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(
            @NotNull ItemStack stack,
            @NotNull Player player,
            @NotNull LivingEntity interactionTarget,
            @NotNull InteractionHand usedHand
    ) {
        if (player.isShiftKeyDown()) {
            if (player.level() instanceof ServerLevel serverLevel) {
                ItemStack itemstack = player.getItemInHand(usedHand);
                IEnergyStorage storage = itemstack.getCapability(Capabilities.EnergyStorage.ITEM);
                if (storage != null && storage.getEnergyStored() >= 100) {
                    ResourceKey<Level> levelKey = this.getTargetLevel(serverLevel);
                    this.teleport(interactionTarget, serverLevel, levelKey);
                    player.getCooldowns().addCooldown(this, 600);
                    player.getFoodData().addExhaustion(40);
                    storage.extractEnergy(100, false);
                }

                AttributeInstance attribute = player.getAttribute(ModAttributes.PLAYER_SANITY);
                if (attribute != null) {
                    attribute.setBaseValue(attribute.getValue() - 20);
                }
            } else {
                return InteractionResult.SUCCESS;
            }
        }

        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    protected ResourceKey<Level> getTargetLevel(Level level) {
        return level.dimension().equals(Level.OVERWORLD) ? ModLevels.VISIONREALM : Level.OVERWORLD;
    }

    protected void teleport(Entity target, ServerLevel level, ResourceKey<Level> targetLevelKey) {
        ServerLevel targetLevel = level.getServer().getLevel(targetLevelKey);
        if (targetLevel != null) {
            double d0 = DimensionType.getTeleportationScale(level.dimensionType(), targetLevel.dimensionType());
            WorldBorder worldborder = targetLevel.getWorldBorder();
            BlockPos blockpos = worldborder.clampToBounds(target.getX() * d0, target.getY(), target.getZ() * d0);
            ServerChunkCache chunkSource = targetLevel.getChunkSource();
            int height = chunkSource.getGenerator().getBaseHeight(
                    blockpos.getX(),
                    blockpos.getZ(),
                    Heightmap.Types.WORLD_SURFACE,
                    targetLevel,
                    chunkSource.randomState()
            );
            Vec3 position = new Vec3(blockpos.getX() + 0.5, height + 0.5, blockpos.getZ() + 0.5);
            target.changeDimension(
                    new DimensionTransition(
                            targetLevel,
                            position,
                            target.getDeltaMovement(),
                            target.getXRot(),
                            0,
                            entity -> {
                                if (entity instanceof LivingEntity livingEntity) {
                                    ((LivingEntityExtensions) livingEntity).setProtectionTime(100);
                                    AttributeInstance attribute = livingEntity.getAttribute(ModAttributes.PLAYER_SANITY);
                                    if (attribute != null) {
                                        attribute.setBaseValue(attribute.getValue() - 20);
                                    }
                                }
                            }
                    )
            );
        }
    }
}

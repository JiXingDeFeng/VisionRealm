package io.github.jixingdefeng.visionrealm.content.world.item;

import io.github.jixingdefeng.visionrealm.content.world.entity.player.MirrorPlayer;
import io.github.jixingdefeng.visionrealm.core.world.entity.player.MirrorPlayerFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ErrorItem extends Item {
    public ErrorItem(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (level instanceof ServerLevel serverLevel) {
            MirrorPlayer livingEntity = MirrorPlayerFactory.addPlayer(serverLevel, player);
            livingEntity.setPos(player.position());
            MirrorPlayerFactory.addToLevel(serverLevel, livingEntity);
        }

        return super.use(level, player, usedHand);
    }
}

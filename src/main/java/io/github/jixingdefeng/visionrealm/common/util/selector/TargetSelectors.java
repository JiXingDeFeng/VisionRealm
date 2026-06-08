package io.github.jixingdefeng.visionrealm.common.util.selector;

import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.block.BlockSelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.entity.EntitySelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.position.PositionSelector;
import io.github.jixingdefeng.visionrealm.impl.selector.game.AbstractSelector;
import io.github.jixingdefeng.visionrealm.impl.selector.game.BaseSelector;
import io.github.jixingdefeng.visionrealm.impl.selector.game.block.BlockSelectorImpl;
import io.github.jixingdefeng.visionrealm.impl.selector.game.entity.EntitySelectorImpl;
import io.github.jixingdefeng.visionrealm.impl.selector.game.world.PositionSelectorImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TargetSelectors {

    public static <T, S extends TargetSelector<T, S>> BaseSelector<T, S> create(Level level) {
        return new BaseSelector<>(level);
    }

    public static <T, S extends TargetSelector<T, S>> AbstractSelector<T, S> create(AbstractSelector<?, ?> selector, @Nullable Level level) {
        return new BaseSelector<>(selector, level);
    }

    public static <T, S extends TargetSelector<T, S>> AbstractSelector<T, S> create(BaseSelector<T, S> selector, @Nullable Level level) {
        return new BaseSelector<>(selector, level);
    }

    public static PositionSelector position(Level level) {
        return create(level).converted(PositionSelectorImpl::new);
    }

    public static PositionSelector position(PositionSelectorImpl selector, @Nullable Level level) {
        return new PositionSelectorImpl(selector, level);
    }

    public static EntitySelector<Entity> entityAll(Level level) {
        return EntitySelectorImpl.createAll(create(level), level);
    }

    public static <T extends Entity> EntitySelector<T> entity(@NotNull EntityType<T> type, @Nullable Level level) {
        return new EntitySelectorImpl<>(create(level), type, level);
    }

    public static <T extends Entity> EntitySelector<T> entity(EntitySelectorImpl<T> selector, @Nullable Level level) {
        return new EntitySelectorImpl<>(selector, level);
    }

    public static EntitySelector<Player> player(Level level) {
        return create(level).converted(EntitySelectorImpl.PlayerSelector::new);
    }

    public static EntitySelector<Player> player(EntitySelectorImpl<Player> selector, @Nullable EntityType<Player> type, @Nullable Level level) {
        if (type != null) {
            return new EntitySelectorImpl.PlayerSelector(selector, type ,level);
        } else {
            return new EntitySelectorImpl.PlayerSelector(selector, level);
        }
    }

    public static BlockSelector block(Level level) {
        return create(level).converted(BlockSelectorImpl::new);
    }

    public static BlockSelector block(BlockSelectorImpl selector, @Nullable Level level) {
        return new BlockSelectorImpl(selector, level);
    }

    private TargetSelectors() {
    }
}

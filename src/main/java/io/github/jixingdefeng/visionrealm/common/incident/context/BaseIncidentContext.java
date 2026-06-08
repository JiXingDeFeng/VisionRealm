package io.github.jixingdefeng.visionrealm.common.incident.context;

import io.github.jixingdefeng.visionrealm.api.incident.IncidentContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class BaseIncidentContext<T> implements IncidentContext<T> {
    protected final Collection<T> allSource;
    protected final T source;
    protected final ServerLevel level;
    protected final RandomSource random;

    public BaseIncidentContext(@Nullable T source, Collection<T> allSource, ServerLevel level, RandomSource random) {
        this.source = source != null ? source : allSource.iterator().next();
        this.allSource = allSource;
        this.level = level;
        this.random = random;
    }

    @Override
    public Collection<T> getAllSource() {
        return this.allSource;
    }

    @Override
    public T getSource() {
        return this.source;
    }

    @Override
    public ServerLevel getLevel() {
        return this.level;
    }

    @Override
    public RandomSource getRandom() {
        return this.random;
    }
}

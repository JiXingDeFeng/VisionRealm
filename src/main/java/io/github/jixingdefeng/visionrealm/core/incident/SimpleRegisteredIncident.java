package io.github.jixingdefeng.visionrealm.core.incident;

import io.github.jixingdefeng.visionrealm.api.incident.Incident;
import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.api.selector.game.RegisteredTargetSelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import net.minecraft.util.random.Weight;
import org.jetbrains.annotations.NotNull;

public class SimpleRegisteredIncident<T, S extends TargetSelector<T, S>> implements RegisteredIncident<T, S> {
    protected final Incident<T> incident;
    protected final RegisteredTargetSelector<T, S> targetSelector;
    protected final Weight weight;
    protected final float probability;
    protected final boolean runInAllDimensions;

    public SimpleRegisteredIncident(Builder<T, S> builder) {
        this.incident = builder.incident();
        this.targetSelector = builder.buildTargetSelector();
        this.weight = Weight.of(builder.weight());
        this.probability = builder.probability();
        this.runInAllDimensions = builder.isRunInAllDimensions();
    }

    @Override
    public Incident<T> getIncident() {
        return this.incident;
    }

    @Override
    public RegisteredTargetSelector<T, S> getTargetSelector() {
        return this.targetSelector;
    }

    @Override
    public float getProbability() {
        return this.probability;
    }

    @Override
    @NotNull
    public Weight getWeight() {
        return this.weight;
    }

    @Override
    public boolean runInAllDimensions() {
        return this.runInAllDimensions;
    }
}

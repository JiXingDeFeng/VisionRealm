package io.github.jixingdefeng.visionrealm.core.incident;

import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.common.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistryKeys;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IncidentHandler {
    private volatile static IncidentHandler INSTANCE;
    @NotNull private RandomSource random = RandomSource.create();
    @NotNull private IntProvider intervalProvider;
    private ArrayWeightRandomList<RegisteredIncident<?, ?>> incidents;
    private long registryVersion;
    private int currentInterval;
    private boolean closed = false;

    public static IncidentHandler getInstance() {
        return INSTANCE;
    }

    public static IncidentHandler create(@Nullable IntProvider intervalProvider) {
        IncidentHandler handler = new IncidentHandler();
        if (intervalProvider != null) {
            handler.setCurrentInterval(intervalProvider);
        }

        return handler;
    }

    @SubscribeEvent
    public static void onServerTickEvent(ServerTickEvent.Pre event) {
        if (event.hasTime()) {
            IncidentHandler.getInstance().tick(event.getServer());
        }
    }

    @ApiStatus.Internal
    public static void close(ServerStoppingEvent event) {
        replaceInstance(null);
    }

    private static void replaceInstance(@Nullable IncidentHandler handler) {
        if (INSTANCE != null) {
            INSTANCE.close();
        }

        INSTANCE = handler;
    }

    private IncidentHandler() {
        this.intervalProvider = UniformInt.of(10000, 20000);
        this.currentInterval = intervalProvider.sample(this.random);
    }

    public void open() {
        replaceInstance(this);
    }

    public void setCurrentInterval(@NotNull IntProvider currentInterval) {
        this.intervalProvider = currentInterval;
    }

    public void setRandom(@NotNull RandomSource random) {
        this.random = random;
    }

    public boolean executeImmediately(ServerLevel level) {
        if (!this.closed) {
            if (this.execute(level)) {
                this.resetInterval();
                return true;
            }
        }

        return false;
    }

    private void close() {
        this.closed = true;
    }

    private void resetInterval() {
        this.currentInterval = this.intervalProvider.sample(this.random);
    }

    private void tick(MinecraftServer server) {
        if (!this.closed) {
            if (this.currentInterval <= 0) {
                if (this.execute(server)) {
                    this.resetInterval();
                }
            } else {
                --this.currentInterval;
            }
        }
    }

    private boolean execute(MinecraftServer server) {
        Optional<RegisteredIncident<?, ?>> registeredIncident = this.randomIncident(server);
        if (registeredIncident.isPresent()) {
            RegisteredIncident<?, ?> incident = registeredIncident.get();
            Collection<ResourceKey<Level>> dimensions = incident.getTargetSelector().getDimension();
            List<ServerLevel> levelList = new ArrayList<>();
            for (ResourceKey<Level> dimension : dimensions) {
                levelList.add(server.getLevel(dimension));
            }

            return this.execute(incident, levelList);
        } else {
            return false;
        }
    }

    private boolean execute(ServerLevel serverLevel) {
        Optional<RegisteredIncident<?, ?>> incident = this.randomIncident(serverLevel.getServer());
        return incident.filter(registeredIncident -> this.execute(registeredIncident, Collections.singletonList(serverLevel))).isPresent();
    }

    private boolean execute(RegisteredIncident<?, ?> incident, Collection<ServerLevel> serverLevel) {
        boolean result = true;
        for (ServerLevel level : serverLevel) {
            result = incident.execute(level, this.random);
        }

        return result;
    }

    private Optional<RegisteredIncident<?, ?>> randomIncident(MinecraftServer server) {
        if (this.incidents == null) {
            Optional<Registry<RegisteredIncident<?, ?>>> registry = server.registryAccess()
                    .registry(ModRegistryKeys.INCIDENT);
            if (registry.isPresent()) {
                this.incidents = ArrayWeightRandomList.create(registry.get().stream().toList());
            } else {
                return Optional.empty();
            }
        }

        return this.incidents.getRandom(this.random);
    }
}

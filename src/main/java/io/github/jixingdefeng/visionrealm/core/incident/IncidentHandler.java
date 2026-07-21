package io.github.jixingdefeng.visionrealm.core.incident;

import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.common.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class IncidentHandler {
    private volatile static IncidentHandler INSTANCE;
    @NotNull private RandomSource random = RandomSource.create();
    @NotNull private IntProvider intervalProvider;
    private volatile ArrayWeightRandomList<RegisteredIncident<?, ?>> incidents;
    private int currentInterval;
    private boolean closed = false;

    public static Optional<IncidentHandler> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    public static void start(@Nullable IntProvider intProvider) {
        IncidentHandler handler = new IncidentHandler(intProvider);
        replaceInstance(handler);
    }

    @SubscribeEvent
    public static void onServerTickEvent(ServerTickEvent.Pre event) {
        if (event.hasTime()) {
            IncidentHandler.getInstance().ifPresent(handler -> handler.tick(event.getServer()));
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

    protected IncidentHandler(@Nullable IntProvider intProvider) {
        this.intervalProvider = intProvider != null ? intProvider : UniformInt.of(10000, 20000);
        this.currentInterval = intervalProvider.sample(this.random);
    }

    public void setInterval(@NotNull IntProvider intProvider) {
        this.intervalProvider = intProvider;
        this.resetInterval();
    }

    public void setRandom(@NotNull RandomSource random) {
        this.random = random;
    }

    public boolean executeImmediately(ServerLevel level) {
        if (!this.closed) {
            this.resetInterval();
            return this.execute(level);
        } else {
            return false;
        }
    }

    protected void close() {
        this.closed = true;
        this.incidents = null;
    }

    protected void resetInterval() {
        this.currentInterval = this.intervalProvider.sample(this.random);
    }

    protected void tick(MinecraftServer server) {
        if (!this.closed) {
            if (this.currentInterval <= 0) {
                this.execute(server);
                this.resetInterval();
            } else {
                --this.currentInterval;
            }
        }
    }

    protected boolean execute(MinecraftServer server) {
        Optional<RegisteredIncident<?, ?>> registeredIncident = this.randomIncident(server);
        if (registeredIncident.isPresent()) {
            RegisteredIncident<?, ?> incident = registeredIncident.get();
            return incident.execute(server, this.random);
        } else {
            return false;
        }
    }

    protected boolean execute(ServerLevel serverLevel) {
        Optional<RegisteredIncident<?, ?>> optional = this.randomIncident(serverLevel.getServer());
        return optional.filter(incident -> incident.execute(serverLevel, serverLevel.getRandom()))
                .isPresent();
    }

    protected Optional<RegisteredIncident<?, ?>> randomIncident(MinecraftServer server) {
        if (this.closed) {
            return Optional.empty();
        } else {
            if (this.incidents == null) {
                Optional<Registry<RegisteredIncident<?, ?>>> registry = server.registryAccess()
                        .registry(ModRegistries.INCIDENT);
                if (registry.isEmpty()) {
                    VisionRealm.LOGGER.error("Incident registry not found. Ensure the registry '{}' is properly loaded.", ModRegistries.INCIDENT.location());
                    return Optional.empty();
                } else {
                    List<RegisteredIncident<?, ?>> incidentList = registry.get().stream().toList();
                    if (incidentList.isEmpty()) {
                        VisionRealm.LOGGER.warn("No incidents registered in the incident registry. Incident handler has been disabled.");
                        this.closed = true;
                        return Optional.empty();
                    } else {
                        this.incidents = ArrayWeightRandomList.create(incidentList);
                        VisionRealm.LOGGER.debug("Initialized weighted random selection pool with {} incidents.", incidentList.size());
                    }
                }
            }

            return this.incidents.getRandom(this.random);
        }
    }
}

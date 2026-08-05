package io.github.jixingdefeng.visionrealm.core.incident;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistries;
import io.github.jixingdefeng.visionrealm.core.util.random.ArrayWeightRandomList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IncidentScheduler {
    public static final int DEFAULT_MIN_INTERVAL = 1000;
    public static final int DEFAULT_MAX_INTERVAL = 2000;
    private static final RandomSource random = RandomSource.create();
    private static IntervalProvider interval = null;
    private static ArrayWeightRandomList<RegisteredIncident<?, ?>> incidents;
    private static int currentInterval = 0;
    private static boolean closed = false;

    public static void tick(ServerTickEvent.Pre event) {
        if (event.hasTime()) {
            IncidentScheduler.tick(event.getServer());
        }
    }

    @ApiStatus.Internal
    public static void start(ServerStartingEvent event) {
        interval = event.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        () -> new IntervalProvider(DEFAULT_MIN_INTERVAL, DEFAULT_MAX_INTERVAL),
                        (compoundTag, provider) -> {
                            int min = compoundTag.getInt("min_interval");
                            int max = compoundTag.getInt("max_interval");
                            return new IntervalProvider(min, max);
                        },
                        DataFixTypes.LEVEL
                ),
                "IncidentScheduler_IntervalProvider"
        );
        closed = false;
        incidents = null;
        resetInterval();
    }

    @ApiStatus.Internal
    public static void close() {
        interval = null;
        closed = true;
        incidents = null;
    }

    public static boolean executeImmediately(MinecraftServer server) {
        if (!checkOpenOrWarn()) {
            resetInterval();
            return execute(server, false);
        } else {
            return false;
        }
    }

    public static boolean executeImmediately(ServerLevel level) {
        if (!checkOpenOrWarn()) {
            resetInterval();
            return execute(level, false);
        } else {
            return false;
        }
    }

    @Nullable
    public static RegisteredIncident<?, ?> randomIncident(MinecraftServer server) {
        if (checkOpenOrWarn() && incidents == null && reloadIncidents(server)) {
            return incidents.getRandom(random).orElse(null);
        }

        return null;
    }

    public static boolean reloadIncidents(MinecraftServer server) {
        incidents = null;
        Registry<RegisteredIncident<?, ?>> registry = server.registryAccess()
                .registry(ModRegistries.INCIDENT)
                .orElse(null);
        if (registry == null) {
            VisionRealm.LOGGER.error("Incident registry not found. Ensure the registry '{}' is properly loaded.", ModRegistries.INCIDENT.location());
            return false;
        } else {
            List<RegisteredIncident<?, ?>> incidentList = registry.stream().toList();
            if (incidentList.isEmpty()) {
                VisionRealm.LOGGER.warn("No incidents registered in the incident registry. Incident handler has been disabled.");
                closed = true;
                return false;
            } else {
                incidents = ArrayWeightRandomList.create(incidentList);
                VisionRealm.LOGGER.info("Initialized weighted randomInt selection pool with {} incidents.", incidentList.size());
            }
        }

        return incidents != null;
    }

    public static void setInterval(int min, int max) {
        interval.setInterval(min, max);
        resetInterval();
    }

    public static IntProvider getInterval() {
        return UniformInt.of(interval.getMin(), interval.getMax());
    }

    private static void resetInterval() {
        currentInterval = interval.sample(random);
    }

    private static void tick(MinecraftServer server) {
        if (checkOpenOrWarn()) {
            if (currentInterval <= 0) {
                execute(server, true);
                resetInterval();
            } else {
                --currentInterval;
            }
        }
    }

    private static boolean checkOpenOrWarn() {
        if (closed) {
            VisionRealm.LOGGER.warn("IncidentScheduler closed");
            return false;
        } else {
            return true;
        }
    }

    private static boolean execute(MinecraftServer server, boolean activationProbability) {
        RegisteredIncident<?, ?> incident = randomIncident(server);
        return incident != null
                && (!activationProbability || random.nextFloat() >= incident.getProbability())
                && incident.execute(server, random);
    }

    private static boolean execute(ServerLevel serverLevel, boolean activationProbability) {
        RegisteredIncident<?, ?> incident = randomIncident(serverLevel.getServer());
        return incident != null
                && (!activationProbability || random.nextFloat() >= incident.getProbability())
                && incident.execute(serverLevel, random);
    }

    private static class IntervalProvider extends SavedData {
        private int min;
        private int max;

        public IntervalProvider(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @NotNull
        @Override
        public CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
            tag.putInt("min_interval", this.min);
            tag.putInt("max_interval", this.max);
            return tag;
        }

        public int sample(RandomSource random) {
            return Mth.randomBetweenInclusive(random, this.min, this.max);
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return this.max;
        }

        public void setInterval(int min, int max) {
            this.min = min;
            this.max = max;
            this.setDirty();
        }
    }

    private IncidentScheduler() {
    }
}

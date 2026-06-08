package io.github.jixingdefeng.visionrealm.common.erosion.context;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ErosionContext<T> {
    private final T source;
    private final Level level;
    private final Vec3 pos;
    private final BlockPos blockPos;

    public ErosionContext(T source, Level level, Vec3 pos, BlockPos blockPos) {
        this.source = source;
        this.level = level;
        this.pos = pos;
        this.blockPos = blockPos;
    }

    public ErosionContext(T source, Level level, Vec3 pos) {
        this(source, level, pos, BlockPos.containing(pos));
    }

    public T source() {
        return source;
    }

    public Level level() {
        return level;
    }

    public Vec3 pos() {
        return pos;
    }

    public BlockPos blockPos() {
        return blockPos;
    }
}

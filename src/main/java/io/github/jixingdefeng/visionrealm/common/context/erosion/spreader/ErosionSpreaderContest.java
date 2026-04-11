package io.github.jixingdefeng.visionrealm.common.context.erosion.spreader;

import io.github.jixingdefeng.visionrealm.common.context.erosion.ErosionContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ErosionSpreaderContest<T, R> extends ErosionContext<T> {
    private final R target;

    public ErosionSpreaderContest(T source, R target, Level level, Vec3 pos) {
        super(source, level, pos);
        this.target = target;
    }

    public R target() {
        return this.target;
    }
}

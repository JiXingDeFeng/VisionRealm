package io.github.jixingdefeng.visionrealm.client.renderer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class VisionRealmDimensionSpecialEffects extends DimensionSpecialEffects {

    public VisionRealmDimensionSpecialEffects() {
        super(OverworldEffects.CLOUD_LEVEL, true, SkyType.NORMAL, false, false);
    }

    @Override
    @NotNull
    public Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        return fogColor.scale(brightness * 0.95F + 0.05F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return true;
    }
}

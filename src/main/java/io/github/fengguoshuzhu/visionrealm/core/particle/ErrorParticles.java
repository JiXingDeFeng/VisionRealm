package io.github.fengguoshuzhu.visionrealm.core.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class ErrorParticles extends TextureSheetParticle {
    private static final float[][] color = {
            {1.0F, 0.0F, 0.0F},
            {0.0F, 1.0F, 0.0F},
            {0.0F, 0.0F, 1.0F},
            {1.0F, 1.0F, 0.0F},
            {1.0F, 0.0F, 1.0F},
            {0.0F, 1.0F, 1.0F},
    };

    protected ErrorParticles(ClientLevel level, double x, double y, double z, SpriteSet spriteSet, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.setSpriteFromAge(spriteSet);
        int colorIndex = this.random.nextInt(color.length);
        this.rCol = color[colorIndex][0];
        this.gCol = color[colorIndex][1];
        this.bCol = color[colorIndex][2];
        this.setParticleSpeed(0, 0, 0);
        this.quadSize = 0.35F;
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public record Provider(SpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ErrorParticles(level, x, y, z, this.spriteSet, xSpeed, ySpeed, zSpeed);
        }
    }
}

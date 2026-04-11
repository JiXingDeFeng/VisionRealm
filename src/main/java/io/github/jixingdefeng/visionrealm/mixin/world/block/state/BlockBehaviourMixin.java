package io.github.jixingdefeng.visionrealm.mixin.world.block.state;

import io.github.jixingdefeng.visionrealm.core.block.state.BlockBehaviourExpand;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin implements BlockBehaviourExpand {

    @Unique private boolean visionRealm$immuneErosion;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(BlockBehaviour.Properties properties, CallbackInfo ci) {
        if (properties instanceof BlockBehaviourExpand expand) {
            this.visionRealm$immuneErosion = expand.canBeEroded();
        } else {
            this.visionRealm$immuneErosion = true;
        }
    }

    @Override
    public boolean canBeEroded() {
        return this.visionRealm$immuneErosion;
    }

    @Mixin(BlockBehaviour.Properties.class)
    public static class PropertiesMixin implements PropertiesExpand {

        @Unique private BlockBehaviour.Properties visionRealm$properties = (BlockBehaviour.Properties)(Object)this;
        @Unique private boolean visionRealm$canBeEroded = true;

        @Override
        public BlockBehaviour.Properties immuneErosion() {
            this.visionRealm$canBeEroded = false;
            return this.visionRealm$properties;
        }

        @Override
        public boolean canBeEroded() {
            return this.visionRealm$canBeEroded;
        }
    }
}

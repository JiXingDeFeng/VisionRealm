package io.github.fengguoshuzhu.visionrealm.core.block.state;

import net.minecraft.world.level.block.state.BlockBehaviour;

public interface BlockBehaviourExpand {

    boolean canBeEroded();

    interface PropertiesExpand extends BlockBehaviourExpand {

        BlockBehaviour.Properties immuneErosion();
    }
}

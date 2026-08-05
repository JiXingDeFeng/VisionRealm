package io.github.jixingdefeng.visionrealm.content.world.level.block.state;

import net.minecraft.world.level.block.state.BlockBehaviour;

public interface BlockBehaviourExpand {

    boolean canBeEroded();

    interface PropertiesExpand extends BlockBehaviourExpand {

        BlockBehaviour.Properties immuneErosion();
    }
}

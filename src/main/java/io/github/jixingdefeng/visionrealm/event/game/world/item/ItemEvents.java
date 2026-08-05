package io.github.jixingdefeng.visionrealm.event.game.world.item;

import io.github.jixingdefeng.visionrealm.content.component.ModDataComponentTypes;
import io.github.jixingdefeng.visionrealm.content.world.item.ModItems;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;

public class ItemEvents {

    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (itemStack, context) -> new ComponentEnergyStorage(
                        itemStack, ModDataComponentTypes.ENERGY.get(), 100
                ),
                ModItems.CONVEYOR_ITEM.get()
        );
    }
}

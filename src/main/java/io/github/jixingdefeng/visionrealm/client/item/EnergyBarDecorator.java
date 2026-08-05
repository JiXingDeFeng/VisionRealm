package io.github.jixingdefeng.visionrealm.client.item;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

public class EnergyBarDecorator implements IItemDecorator {

    @Override
    public boolean render(
            @NotNull GuiGraphics guiGraphics,
            @NotNull Font font,
            @NotNull ItemStack stack,
            int xOffset,
            int yOffset
    ) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy != null && energy.getMaxEnergyStored() > 0) {
            float energyPercent = (float) energy.getEnergyStored() / energy.getMaxEnergyStored();
            float progress = Math.max(0, 1 - energyPercent);
            int height = (int) Math.ceil(16 * progress);
            guiGraphics.fill(xOffset, yOffset + 16, xOffset + 16, yOffset + height, 0x66FF0000);
            return true;
        }

        return false;
    }
}

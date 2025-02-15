/*
 * Copyright (c) 2021-2024 TeamMoeg
 *
 * This file is part of Frosted Heart.
 *
 * Frosted Heart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Frosted Heart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frosted Heart. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.teammoeg.frostedheart.content.generator.t2;

import com.teammoeg.frostedheart.content.generator.GeneratorRecipe;

import blusunrize.immersiveengineering.common.gui.IEBaseContainer;
import blusunrize.immersiveengineering.common.gui.IESlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class T2GeneratorContainer extends IEBaseContainer<T2GeneratorTileEntity> {
    public T2GeneratorTileEntity.GeneratorUIData data;

    public T2GeneratorContainer(int id, PlayerInventory inventoryPlayer, T2GeneratorTileEntity tile) {
        super(tile, id);

        this.addSlot(new IESlot(this, this.inv, 0, 80, 12) {
            @Override
            public boolean isItemValid(ItemStack itemStack) {
            	return tile.isStackValid(0, itemStack);
            }
        });

        this.addSlot(new IESlot.Output(this, this.inv, 1, 80, 51));

        slotCount = 2;

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        data = tile.guiData;
        trackIntArray(data);
    }
}


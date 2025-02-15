/*
 * Copyright (c) 2022-2024 TeamMoeg
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

package com.teammoeg.frostedheart.compat;

import com.simibubi.create.foundation.block.BlockStressValues.IStressValueProvider;
import com.teammoeg.frostedheart.FHBlocks;

import net.minecraft.block.Block;

public class FHStress implements IStressValueProvider {

    public FHStress() {
    }

    @Override
    public double getCapacity(Block arg0) {

        return 0;
    }

    @Override
    public double getImpact(Block arg0) {
        if (arg0 == FHBlocks.mech_calc.get()) return 64;
        return 0;
    }

    @Override
    public boolean hasCapacity(Block arg0) {
        return false;
    }

    @Override
    public boolean hasImpact(Block arg0) {
        if (arg0 == FHBlocks.mech_calc.get()) return true;
        return false;
    }

}

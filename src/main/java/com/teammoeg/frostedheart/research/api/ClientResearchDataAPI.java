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

package com.teammoeg.frostedheart.research.api;

import com.teammoeg.frostedheart.research.data.TeamResearchData;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientResearchDataAPI {

    @OnlyIn(Dist.CLIENT)
    public static TeamResearchData getData() {
        return TeamResearchData.getClientInstance();

    }

    @OnlyIn(Dist.CLIENT)
    public static CompoundNBT getVariants() {
        return TeamResearchData.getClientInstance().getVariants();

    }

    private ClientResearchDataAPI() {
    }
}

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

package com.teammoeg.frostedheart.research.research;

import java.util.HashMap;
import java.util.Map;

import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.client.util.GuiUtils;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public enum ResearchCategory {

    RESCUE("rescue"),
    LIVING("living"),
    PRODUCTION("production"),
    ARS("ars"),
    EXPLORATION("exploration");
    public static Map<ResourceLocation, ResearchCategory> ALL = new HashMap<>();
    static {
        for (ResearchCategory rc : ResearchCategory.values())
            ResearchCategory.ALL.put(rc.id, rc);
    }
    private ResourceLocation id;
    private TranslationTextComponent name;
    private TranslationTextComponent desc;

    private ResourceLocation icon;

    ResearchCategory(String id) {
        this.id = FHMain.rl(id);
        this.name = GuiUtils.translateResearchCategoryName(id);
        this.desc = GuiUtils.translateResearchCategoryDesc(id);
        this.icon = FHMain.rl("textures/gui/research/category/" + id + ".png");
        //FHMain.rl("textures/gui/research/category/background/" + id + ".png");

    }

    public TranslationTextComponent getDesc() {
        return desc;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public ResourceLocation getId() {
        return id;
    }

    public TranslationTextComponent getName() {
        return name;
    }

}

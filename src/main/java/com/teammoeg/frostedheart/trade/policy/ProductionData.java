/*
 * Copyright (c) 2024 TeamMoeg
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

package com.teammoeg.frostedheart.trade.policy;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teammoeg.frostedheart.trade.policy.snapshot.PolicySnapshot;
import com.teammoeg.frostedheart.trade.policy.snapshot.SellData;
import com.teammoeg.frostedheart.util.io.SerializeUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class ProductionData extends BaseData {
    public ItemStack item;

    public ProductionData(JsonObject jo) {
        super(jo);
        item = SerializeUtil.fromJson(jo.get("produce"));
    }

    public ProductionData(PacketBuffer pb) {
        super(pb);
        item = pb.readItemStack();
    }

    public ProductionData(String id, int maxstore, float recover, int price, ItemStack item) {
        super(id, maxstore, recover, price);
        this.item = item;

    }

    @Override
    public void fetch(PolicySnapshot ps, Map<String, Float> data) {

        int num = (int) (float) data.getOrDefault(getId(), 0f);
        if (!hideStockout || num > 0)
            ps.registerSell(new SellData(getId(), num, this));
    }

    @Override
    public String getType() {
        return "s";
    }

    @Override
    public JsonElement serialize() {
        JsonObject jo = super.serialize().getAsJsonObject();
        jo.add("produce", SerializeUtil.toJson(item));
        return jo;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarInt(1);
        super.write(buffer);
        buffer.writeItemStack(item);
    }
}

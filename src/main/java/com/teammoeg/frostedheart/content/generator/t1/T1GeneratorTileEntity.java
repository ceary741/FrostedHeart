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

package com.teammoeg.frostedheart.content.generator.t1;

import java.util.Random;
import java.util.function.Consumer;

import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.teammoeg.frostedheart.FHMultiblocks;
import com.teammoeg.frostedheart.FHTileTypes;
import com.teammoeg.frostedheart.client.util.ClientUtils;
import com.teammoeg.frostedheart.content.generator.MasterGeneratorTileEntity;

import com.teammoeg.frostedheart.content.generator.tool.GeneratorDriveHandler;
import com.teammoeg.frostedheart.content.generator.tool.NeighborTypeEnum;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;

public final class T1GeneratorTileEntity extends MasterGeneratorTileEntity<T1GeneratorTileEntity> {
    public T1GeneratorTileEntity.GeneratorUIData guiData = new T1GeneratorTileEntity.GeneratorUIData();
    public boolean hasFuel;
    GeneratorDriveHandler generatorDriveHandler;
    protected static BlockPos lastSupportPos;
    protected static NeighborTypeEnum neighborType;
    public T1GeneratorTileEntity() {
        super(FHMultiblocks.GENERATOR, FHTileTypes.GENERATOR_T1.get(), false);
        this.generatorDriveHandler = new GeneratorDriveHandler(world);
        this.lastSupportPos = new BlockPos(0,0,0);
    }
    public boolean isExistNeighborTileEntity() {
        Vector3i vec = this.multiblockInstance.getSize(world);
        int xLow = -1, xHigh = vec.getX(), yLow = 0, yHigh = vec.getY(), zLow = -1, zHigh = vec.getZ();
        int blastBlockCount = 0, alloySmelterCount = 0;
        for (int x = xLow; x <= xHigh; ++x)
            for (int y = yLow; y < yHigh; ++y)
                for (int z = zLow; z <= zHigh; ++z) {
                    BlockPos actualPos = getBlockPosForPos(new BlockPos(x, y, z));
                    /** Enum a seamless NoUpandDown hollow cube */
                    if ( ( (z>zLow && z<zHigh) && ((x==xLow) || (x==xHigh)) ) || ((z==zLow || z==zHigh) && (x>xLow && x<xHigh)) ) {
                        TileEntity te = Utils.getExistingTileEntity(world, actualPos);
                        if (te instanceof BlastFurnaceTileEntity) {
                            if (++blastBlockCount == 9) {
                            	BlastFurnaceTileEntity master=((BlastFurnaceTileEntity) te).master();
                                lastSupportPos = actualPos;
                                neighborType = NeighborTypeEnum.BlastFurnaceTileEntity;
                                System.out.println("The TileEntity is BlastFurnaceTileEntity");
                                return true;
                            }
                        }
                        if (te instanceof AlloySmelterTileEntity) {
                            if (++alloySmelterCount == 4) {
                                lastSupportPos = actualPos;
                                neighborType = NeighborTypeEnum.AlloySmelterTileEntity;
                                System.out.println("The TileEntity is AlloySmelterTileEntity");
                                return true;
                            }
                        }
                    }
                }
        return false;
    }
    @Override
    protected void callBlockConsumerWithTypeCheck(Consumer<T1GeneratorTileEntity> consumer, TileEntity te) {
        if (te instanceof T1GeneratorTileEntity)
            consumer.accept((T1GeneratorTileEntity) te);
    }

    @Override
    public int getLowerBound() {
        int distanceToGround = 2;
        int extra = MathHelper.ceil(getRangeLevel());
        return distanceToGround + extra;
    }

    @Override
    public int getUpperBound() {
        int distanceToTowerTop = 2;
        int extra = MathHelper.ceil(getRangeLevel() * 2);
        return distanceToTowerTop + extra;
    }

    @Override
    public void readCustomNBT(CompoundNBT nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        hasFuel = nbt.getBoolean("hasFuel");
    }

    @Override
    public void shutdownTick() {
        boolean invState = !this.getInventory().get(INPUT_SLOT).isEmpty();
        if (invState != hasFuel) {
            hasFuel = invState;
            this.markContainingBlockForUpdate(null);
        }

    }

    @Override
    protected void tickEffects(boolean isActive) {
        if (isActive) {
            BlockPos blockpos = this.getPos();
            Random random = world.rand;
            if (random.nextFloat() < 0.2F) {
                //for (int i = 0; i < random.nextInt(2) + 2; ++i) {
                ClientUtils.spawnSmokeParticles(world, blockpos.offset(Direction.UP, 1));
                ClientUtils.spawnSmokeParticles(world, blockpos);
                ClientUtils.spawnFireParticles(world, blockpos);
                //}
            }
        }
    }

    @Override
    protected boolean tickFuel() {
        this.hasFuel = !this.getInventory().get(INPUT_SLOT).isEmpty();
        return super.tickFuel();
    }

    @Override
    protected void tickDrives(boolean isActive) {
        if (isActive) {
            if (isExistNeighborTileEntity()) {
                this.generatorDriveHandler.checkExistOreAndUpdate(lastSupportPos, neighborType);
            }
        }
    }
    @Override
    public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.putBoolean("hasFuel", hasFuel);
    }


	@Override
	public float getMaxTemperatureLevel() {
		return isOverdrive()?2:1;
	}


	@Override
	public float getMaxRangeLevel() {
		return 1;
	}
}

package com.lothrazar.cyclicmagic.block.cablewireless.fluid;

import com.lothrazar.cyclicmagic.block.cablewireless.ILaserTarget;
import com.lothrazar.cyclicmagic.block.core.TileEntityBaseMachineFluid;
import com.lothrazar.cyclicmagic.data.BlockPosDim;
import com.lothrazar.cyclicmagic.gui.ITileRedstoneToggle;
import com.lothrazar.cyclicmagic.item.location.ItemLocation;
import com.lothrazar.cyclicmagic.liquid.FluidTankBase;
import com.lothrazar.cyclicmagic.util.RenderUtil.LaserConfig;
import com.lothrazar.cyclicmagic.util.UtilFluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;

public class TileCableFluidWireless extends TileEntityBaseMachineFluid implements ITickable, ILaserTarget, ITileRedstoneToggle {

  //ITilePreviewToggle
  public static final int TRANSFER_FLUID_PER_TICK = 500;
  public static final int TANK_FULL = 10000;
  public static final int SLOT_CARD_ITEM = 0;
  public static final int MAX_TRANSFER = 1000;
  private int transferRate = MAX_TRANSFER / 2;
  private int renderParticles = 0;

  public static enum Fields {
    REDSTONE, TRANSFER_RATE, RENDERPARTICLES;
  }

  private int needsRedstone = 0;

  public TileCableFluidWireless() {
    super(1);
    tank = new FluidTankBase(TANK_FULL);
  }

  @Override
  public int[] getFieldOrdinals() {
    return super.getFieldArray(Fields.values().length);
  }

  @Override
  public int getField(int id) {
    switch (Fields.values()[id]) {
      case REDSTONE:
        return this.needsRedstone;
      case TRANSFER_RATE:
        return this.transferRate;
      case RENDERPARTICLES:
        return this.renderParticles;
    }
    return 0;
  }

  @Override
  public void setField(int id, int value) {
    switch (Fields.values()[id]) {
      case REDSTONE:
        this.needsRedstone = value % 2;
      break;
      case TRANSFER_RATE:
        transferRate = value;
      break;
      case RENDERPARTICLES:
        this.renderParticles = value % 2;
      break;
    }
  }

  @Override
  public boolean isItemValidForSlot(int index, ItemStack stack) {
    if (index == SLOT_CARD_ITEM) {
      return true;
    }
    return stack.getItem() instanceof ItemLocation;
  }

  private BlockPosDim getTarget(int slot) {
    return ItemLocation.getPosition(this.getStackInSlot(slot));
  }

  @Override
  public void update() {
    if (isRunning() == false) {
      return;
    }
    outputFluid();
  }

  @Override
  public void toggleNeedsRedstone() {
    int val = (this.needsRedstone == 1) ? 0 : 1;
    this.setField(Fields.REDSTONE.ordinal(), val);
  }

  @Override
  public boolean onlyRunIfPowered() {
    return this.needsRedstone == 1;
  }

  private boolean isTargetValid(BlockPosDim target) {
    return target != null &&
        target.getDimension() == this.getDimension() &&
        world.isAreaLoaded(target.toBlockPos(), target.toBlockPos().up());
  }

  private void outputFluid() {
    BlockPosDim dim = this.getTarget(SLOT_CARD_ITEM);
    if (this.isTargetValid(dim)) {
      UtilFluid.tryFillPositionFromTank(world, dim.toBlockPos(), null, this.tank, TRANSFER_FLUID_PER_TICK);
    }
  }

  @Override
  public boolean isVisible() {
    return this.renderParticles == 1;
  }

  @Override
  public LaserConfig getTarget() {
    //find laser endpoints and go
    BlockPosDim first = new BlockPosDim(this.getPos(), this.getDimension());
    BlockPosDim second = this.getTarget(SLOT_CARD_ITEM);
    if (second != null && first != null && second.getDimension() == first.getDimension()) {
      float[] color = new float[] { 0.8F, 0.8F, 0.8F };
      double rotationTime = 0;
      double beamWidth = 0.09;
      float alpha = 0.5F;
      return new LaserConfig(first.toBlockPos(), second.toBlockPos(),
          rotationTime, alpha, beamWidth, color);
    }
    return null;
  }
}
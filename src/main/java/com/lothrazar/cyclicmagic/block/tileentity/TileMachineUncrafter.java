package com.lothrazar.cyclicmagic.block.tileentity;
import java.util.ArrayList;
import com.lothrazar.cyclicmagic.ModMain;
import com.lothrazar.cyclicmagic.util.UtilEntity;
import com.lothrazar.cyclicmagic.util.UtilParticle;
import com.lothrazar.cyclicmagic.util.UtilSound;
import com.lothrazar.cyclicmagic.util.UtilUncraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;// net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class TileMachineUncrafter extends TileEntityBaseMachine implements IInventory, ITickable, ISidedInventory {
  // http://www.minecraftforge.net/wiki/Containers_and_GUIs
  // http://greyminecraftcoder.blogspot.com.au/2015/01/tileentity.html
  // http://www.minecraftforge.net/forum/index.php?topic=28539.0
  // http://bedrockminer.jimdo.com/modding-tutorials/advanced-modding/tile-entities/
  // http://www.minecraftforge.net/wiki/Tile_Entity_Synchronization
  // http://www.minecraftforge.net/forum/index.php?topic=18871.0
  public static final int TIMER_FULL = 200;
  private ItemStack[] inv;
  private int timer;
  private int[] hopperInput = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };// all slots for all faces
  private static final String NBT_INV = "Inventory";
  private static final String NBT_SLOT = "Slot";
  private static final String NBT_TIMER = "Timer";
  public TileMachineUncrafter() {
    inv = new ItemStack[9];
    timer = TIMER_FULL;
  }
  @Override
  public boolean hasCustomName() {
    return false;
  }
  @Override
  public ITextComponent getDisplayName() {
    return null;
  }
  @Override
  public int getSizeInventory() {
    return inv.length;
  }
  @Override
  public ItemStack getStackInSlot(int index) {
    return inv[index];
  }
  @Override
  public ItemStack decrStackSize(int index, int count) {
    ItemStack stack = getStackInSlot(index);
    if (stack != null) {
      if (stack.stackSize <= count) {
        setInventorySlotContents(index, null);
      }
      else {
        stack = stack.splitStack(count);
        if (stack.stackSize == 0) {
          setInventorySlotContents(index, null);
        }
      }
    }
    return stack;
  }
  @Override
  public void setInventorySlotContents(int index, ItemStack stack) {
    inv[index] = stack;
    if (stack != null && stack.stackSize > getInventoryStackLimit()) {
      stack.stackSize = getInventoryStackLimit();
    }
  }
  @Override
  public int getInventoryStackLimit() {
    return 64;
  }
  @Override
  public boolean isUseableByPlayer(EntityPlayer player) {
    return true;
  }
  @Override
  public void openInventory(EntityPlayer player) {
  }
  @Override
  public void closeInventory(EntityPlayer player) {
  }
  @Override
  public boolean isItemValidForSlot(int index, ItemStack stack) {
    // for this container, same for all slots
    return true;// SlotUncraft.checkValid(stack);
  }
  @Override
  public int getField(int id) {
    return 0;
  }
  @Override
  public void setField(int id, int value) {
  }
  @Override
  public int getFieldCount() {
    return 0;
  }
  @Override
  public void clear() {
  }
  @Override
  public void readFromNBT(NBTTagCompound tagCompound) {
    super.readFromNBT(tagCompound);
    timer = tagCompound.getInteger(NBT_TIMER);
    NBTTagList tagList = tagCompound.getTagList(NBT_INV, 10);
    for (int i = 0; i < tagList.tagCount(); i++) {
      NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
      byte slot = tag.getByte(NBT_SLOT);
      if (slot >= 0 && slot < inv.length) {
        inv[slot] = ItemStack.loadItemStackFromNBT(tag);
      }
    }
  }
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
    tagCompound.setInteger(NBT_TIMER, timer);
    NBTTagList itemList = new NBTTagList();
    for (int i = 0; i < inv.length; i++) {
      ItemStack stack = inv[i];
      if (stack != null) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte(NBT_SLOT, (byte) i);
        stack.writeToNBT(tag);
        itemList.appendTag(tag);
      }
    }
    tagCompound.setTag(NBT_INV, itemList);
    return super.writeToNBT(tagCompound);
  }
  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {//getDescriptionPacket() {
    // Gathers data into a packet (S35PacketUpdateTileEntity) that is to be
    // sent to the client. Called on server only.
    NBTTagCompound syncData = new NBTTagCompound();
    this.writeToNBT(syncData);
    return new SPacketUpdateTileEntity(this.pos, 1, syncData);
  }
  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    // Extracts data from a packet (S35PacketUpdateTileEntity) that was sent
    // from the server. Called on client only.
    this.readFromNBT(pkt.getNbtCompound());
    super.onDataPacket(net, pkt);
  }
  public int getTimer() {
    return timer;
  }
  private void shiftAllUp() {
    for (int i = 0; i < this.getSizeInventory() - 1; i++) {
      shiftPairUp(i, i + 1);
    }
  }
  private void shiftPairUp(int low, int high) {
    ItemStack main = getStackInSlot(low);
    ItemStack second = getStackInSlot(high);
    if (main == null && second != null) { // if the one below this is not
      // empty, move it up
      this.setInventorySlotContents(high, null);
      this.setInventorySlotContents(low, second);
    }
  }
  public boolean isBurning() {
    return this.timer > 0 && this.timer < TIMER_FULL;
  }
  @Override
  public void update() {
    // this is from interface IUpdatePlayerListBox
    // change up the timer on both client and server (it gets synced
    // eventually but not constantly)
    this.shiftAllUp();
    boolean triggerUncraft = false;
    if (this.isPowered() == false) {
      //it works ONLY if its powered
      return;
    }
    //center of the block
    double x = this.getPos().getX() + 0.5;
    double y = this.getPos().getY() + 0.5;
    double z = this.getPos().getZ() + 0.5;
    ItemStack stack = getStackInSlot(0);
    if (stack == null) {
      timer = TIMER_FULL;// reset just like you would in a
      // furnace
      return;
    }
    timer--;
    if (timer <= 0) {
      timer = TIMER_FULL;
      triggerUncraft = true;
    }
    if (triggerUncraft) {
      // detect what direction my block faces)
      EnumFacing facing = this.getCurrentFacing();
      int dx = 0, dz = 0;
      if (facing == EnumFacing.SOUTH) {
        dz = -1;
      }
      else if (facing == EnumFacing.NORTH) {
        dz = +1;
      }
      else if (facing == EnumFacing.EAST) {
        dx = -1;
      }
      else if (facing == EnumFacing.WEST) {
        dx = +1;
      }
      x += dx;
      z += dz;
      BlockPos posOffsetFacing = new BlockPos(x, y, z);
      TileEntity attached = this.worldObj.getTileEntity(posOffsetFacing);
      IInventory attachedInv = null;
      if (attached != null && attached instanceof IInventory) {
        attachedInv = (IInventory) attached;
      }
      UtilUncraft uncrafter = new UtilUncraft(stack);
      boolean success = false;
      try {
        success = uncrafter.doUncraft();
      }
      catch (Exception e) {
        ModMain.logger.error("Unhandled exception in uncrafting " + e.getStackTrace().toString());
      }
      if (success) {
        if (this.worldObj.isRemote == false) { // drop the items
          ArrayList<ItemStack> uncrafterOutput = uncrafter.getDrops();
          ArrayList<ItemStack> toDrop = new ArrayList<ItemStack>();
          if (attached != null) {
            toDrop = dumpToIInventory(uncrafterOutput, attachedInv);
          }
          else {
            toDrop = uncrafterOutput;
          }
          for (ItemStack s : toDrop) {
            UtilEntity.dropItemStackInWorld(worldObj, posOffsetFacing, s);
          }
          this.decrStackSize(0, uncrafter.getOutsize());
        }
        UtilSound.playSound(worldObj, this.getPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS);
      }
      else {//try to dump to inventory first
        if (attached != null) {
          ArrayList<ItemStack> toDrop = new ArrayList<ItemStack>();
          toDrop.add(stack);
          toDrop = dumpToIInventory(toDrop, attachedInv);
          if (toDrop.size() == 1) {//it only had one in it. so if theres one left, it didnt work
            UtilEntity.dropItemStackInWorld(worldObj, posOffsetFacing, toDrop.get(0));
          }
        }
        else {
          UtilEntity.dropItemStackInWorld(worldObj, posOffsetFacing, stack);
        }
        if (this.worldObj.isRemote == false) {
          this.decrStackSize(0, stack.stackSize);
        }
        UtilSound.playSound(worldObj, this.getPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.BLOCKS);
      }
      this.worldObj.markBlockRangeForRenderUpdate(this.getPos(), this.getPos().up());
      this.markDirty();
    }
    else {
      //dont trigger an uncraft event, its still processing
      if (this.worldObj.isRemote && this.worldObj.rand.nextDouble() < 0.1) {
        UtilParticle.spawnParticle(worldObj, EnumParticleTypes.SMOKE_NORMAL, x, y, z);
      }
    }
  }
  public static ArrayList<ItemStack> dumpToIInventory(ArrayList<ItemStack> stacks, IInventory inventory) {
    //and return the remainder after dumping
    ArrayList<ItemStack> remaining = new ArrayList<ItemStack>();
    ItemStack chestStack;
    for (ItemStack current : stacks) {
      if (current == null) {
        continue;
      }
      for (int i = 0; i < inventory.getSizeInventory(); i++) {
        if (current == null) {
          continue;
        }
        chestStack = inventory.getStackInSlot(i);
        if (chestStack == null) {
          inventory.setInventorySlotContents(i, current);
          // and dont add current ot remainder at all ! sweet!
          current = null;
        }
        else if (chestStack.isItemEqual(current)) {
          int space = chestStack.getMaxStackSize() - chestStack.stackSize;
          int toDeposit = Math.min(space, current.stackSize);
          if (toDeposit > 0) {
            current.stackSize -= toDeposit;
            chestStack.stackSize += toDeposit;
            if (current.stackSize == 0) {
              current = null;
            }
          }
        }
      } // finished current pass over inventory
      if (current != null) {
        remaining.add(current);
      }
    }
    return remaining;
  }
  @Override
  public int[] getSlotsForFace(EnumFacing side) {
    return hopperInput;
  }
  @Override
  public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
    return this.isItemValidForSlot(index, itemStackIn);
  }
  @Override
  public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
    //do not let hoppers pull out of here for any reason
    return false;// direction == EnumFacing.DOWN;
  }
  @Override
  public ItemStack removeStackFromSlot(int index) {
    ItemStack stack = getStackInSlot(index);
    if (stack != null) {
      setInventorySlotContents(index, null);
    }
    return stack;
  }
  @Override
  public String getName() {
    return null;
  }
}
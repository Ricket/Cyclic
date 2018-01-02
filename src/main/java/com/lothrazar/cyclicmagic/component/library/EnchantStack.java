package com.lothrazar.cyclicmagic.component.library;
import java.util.HashMap;
import java.util.Map;
import com.lothrazar.cyclicmagic.data.Const;
import com.lothrazar.cyclicmagic.registry.EnchantRegistry;
import com.lothrazar.cyclicmagic.util.UtilChat;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;

/**
 * One enchantment instance is an enchant combined with its level and we have a number of those
 * 
 * @author Sam
 *
 */
public class EnchantStack {
  private static final String NBT_LEVEL = "level";
  private static final String NBT_COUNT = "eCount";
  private static final String NBT_ENCH = "ench";
  private int count = 0;
  private int level = 0;
  private Enchantment ench = null;
  public EnchantStack() {}
  public EnchantStack(Enchantment e, int lvl) {
    ench = e;
    level = lvl;
    count = 1;
  }
  public Enchantment getEnch() {
    return ench;
  }
  public Integer getLevel() {
    return level;
  }
  public int getCount() {
    return count;
  }
  public void readFromNBT(NBTTagCompound tags, String key) {
    NBTTagCompound t = (NBTTagCompound) tags.getTag(key);
    this.count = t.getInteger(NBT_COUNT);
    this.level = t.getInteger(NBT_LEVEL);
    String enchString = t.getString(NBT_ENCH);
    if (enchString.isEmpty() == false)
      this.ench = Enchantment.getEnchantmentByLocation(enchString);
  }
  public NBTTagCompound writeToNBT() {
    NBTTagCompound t = new NBTTagCompound();
    t.setInteger(NBT_COUNT, count);
    t.setInteger(NBT_LEVEL, level);
    if (ench == null) {
      t.setString(NBT_ENCH, "");
    }
    else {
      t.setString(NBT_ENCH, ench.getRegistryName().toString());
    }
    return t;
  }
  public boolean isEmpty() {
    return ench == null || getCount() == 0;
  }
  public boolean doesMatchNonEmpty(ItemStack stack) {
    if (this.isEmpty()) {
      return false;
    }
    if (stack.getItem().equals(Items.ENCHANTED_BOOK)) {
      Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
      for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
        if (this.doesMatch(entry.getKey(), entry.getValue())) {
          return true;
        }
      }
    }
    return false;
  }
  public boolean doesMatch(Enchantment e, int lvl) {
    return ench.equals(e) && level == lvl;
  }
  public void add() {
    count++;
  }
  public void remove() {
    count--;
    if (count <= 0) {
      ench = null;
      level = 0;
    }
  }
  public boolean equals(EnchantStack e) {
    return this.doesMatch(e.ench, e.getLevel()) && this.getCount() == e.getCount();
  }
  @Override
  public String toString() {
    if (this.isEmpty()) {
      return UtilChat.lang("enchantment_stack.empty");
    }
    return countName() + " " + UtilChat.lang(ench.getName()) + " " + levelName();
  }
  public String countName() {
    return "[" + count + "]";
  }
  public String levelName() {
    return EnchantRegistry.getStrForLevel(level);
  }
  public String shortName() {
    if (this.isEmpty()) {
      return "--";
    }
    return UtilChat.lang(ench.getName()).substring(0, 5);
  }
  public ItemStack getRenderIcon() {
    return makeEnchantedBook();
    //TODO: this was going to be some crazy config system 
    //TODO: not now. for the future 
  }
  public ItemStack makeEnchantedBook() {
    if (this.isEmpty()) {
      return ItemStack.EMPTY;
    }
    ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
    Map<Enchantment, Integer> enchMap = new HashMap<Enchantment, Integer>();
    enchMap.put(this.getEnch(), this.getLevel());
    EnchantmentHelper.setEnchantments(enchMap, stack);
    return stack;
  }
}
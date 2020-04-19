package com.lothrazar.cyclic.block.harvester;

import com.lothrazar.cyclic.base.ScreenBase;
import com.lothrazar.cyclic.gui.ButtonMachine;
import com.lothrazar.cyclic.gui.EnergyBar;
import com.lothrazar.cyclic.gui.TextureEnum;
import com.lothrazar.cyclic.net.PacketTileData;
import com.lothrazar.cyclic.registry.PacketRegistry;
import com.lothrazar.cyclic.registry.TextureRegistry;
import com.lothrazar.cyclic.util.UtilChat;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class ScreenHarvester extends ScreenBase<ContainerHarvester> {

  private EnergyBar energy;
  private ButtonMachine btnRedstone;

  public ScreenHarvester(ContainerHarvester screenContainer, PlayerInventory inv, ITextComponent titleIn) {
    super(screenContainer, inv, titleIn);
    this.energy = new EnergyBar(this);
    energy.max = TileHarvester.MAX;
  }

  @Override
  public void init() {
    super.init();
    int x, y;
    energy.guiLeft = guiLeft;
    energy.guiTop = guiTop;
    x = guiLeft + 8;
    y = guiTop + 8;
    btnRedstone = addButton(new ButtonMachine(x, y, 20, 20, "", (p) -> {
      container.tile.setNeedsRedstone((container.getNeedsRedstone() + 1) % 2);
      PacketRegistry.INSTANCE.sendToServer(new PacketTileData(TileHarvester.Fields.REDSTONE.ordinal(), container.tile.getNeedsRedstone(), container.tile.getPos()));
    }));
  }

  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    this.renderBackground();
    super.render(mouseX, mouseY, partialTicks);
    this.renderHoveredToolTip(mouseX, mouseY);
    energy.renderHoveredToolTip(mouseX, mouseY, container.getEnergy());
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    btnRedstone.setTooltip(UtilChat.lang("gui.cyclic.redstone" + container.getNeedsRedstone()));
    btnRedstone.setTextureId(container.getNeedsRedstone() == 1 ? TextureEnum.REDSTONE_NEEDED : TextureEnum.REDSTONE_ON);
    this.drawButtonTooltips(mouseX, mouseY);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    //    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.drawBackground(TextureRegistry.GUI);
    //energy
    energy.renderEnergy(container.getEnergy());
  }
}
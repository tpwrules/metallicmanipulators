package tpw_rules.metallicmanipulators

import net.minecraft.inventory.{Slot, IInventory, Container}
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.util.StatCollector
import org.lwjgl.opengl.GL11
import net.minecraft.client.resources.ResourceLocation

trait StandardContainer extends Container {
  protected var te: IInventory
  val playerInventoryStart: Int

  override def canInteractWith(player: EntityPlayer) = te.isUseableByPlayer(player)

  def addPlayerSlots(inv: InventoryPlayer, px: Int, py: Int) = {
    for (y <- 0 until 3; x <- 0 until 9) {
      this.addSlotToContainer(new Slot(inv, playerInventoryStart+(y*3)+x,
      px+(x*18), py+(y*18)))
    }
    for (x <- 0 until 9) {
      this.addSlotToContainer(new Slot(inv, playerInventoryStart+27+x,
      px+(x*18), py+58))
    }
  }

  override def transferStackInSlot(player: EntityPlayer, slot: Int): ItemStack = {
    val slotObject = inventorySlots.get(slot).asInstanceOf[Slot]
    if (slotObject == null || !slotObject.getHasStack) null

    val slotStack = slotObject.getStack
    val remainingStack = slotStack.copy

    // merge into either player or TE based on source inventory
    if (slot < playerInventoryStart) {
      if (!this.mergeItemStack(slotStack, playerInventoryStart, playerInventoryStart+36, true)) {
        return null
      }
    } else if (!this.mergeItemStack(slotStack, 0, playerInventoryStart, false)) {
      return null
    }

    if (slotStack.stackSize == 0)
      slotObject.putStack(null)
    else
      slotObject.onSlotChanged()

    if (slotStack.stackSize == remainingStack.stackSize) null

    slotObject.onPickupFromSlot(player, slotStack)

    remainingStack
  }
}

trait StandardGUI extends GuiContainer {
  val inventoryName: String
  val guiTexture: ResourceLocation

  override protected def drawGuiContainerForegroundLayer(a: Int, b: Int) = {
    super.drawGuiContainerForegroundLayer(a, b)
    fontRenderer.drawString(inventoryName, 8, 6, 0x404040)
    fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize-94, 0x404040)
  }

  override protected def drawGuiContainerBackgroundLayer(a: Float, b: Int, c: Int) = {
    GL11.glColor4f(1f, 1f, 1f, 1f)
    this.mc.renderEngine.func_110577_a(guiTexture)
    this.drawTexturedModalRect((width-xSize)/2, (height-ySize)/2, 0, 0, xSize, ySize)
  }
}
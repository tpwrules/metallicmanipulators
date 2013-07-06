package tpw_rules.metallicmanipulators

import net.minecraft.inventory.{Slot, IInventory, Container}
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.util.StatCollector
import org.lwjgl.opengl.GL11
import net.minecraft.client.resources.ResourceLocation
import cpw.mods.fml.common.network.IGuiHandler
import net.minecraft.world.World
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.Minecraft

trait StandardContainer extends Container {
  protected val tileEntity: IInventory
  val playerInventoryStart: Int


  // stupid crap because the trait can't access protected things in Container
  def doAddSlotToContainer(slot: Slot)
  def doMergeItemStack(stack: ItemStack, start: Int, end: Int, backwards: Boolean): Boolean

  override def canInteractWith(player: EntityPlayer) = tileEntity.isUseableByPlayer(player)

  def addPlayerSlots(inv: InventoryPlayer, px: Int, py: Int) = {
    for (y <- 0 until 3; x <- 0 until 9) {
      this.doAddSlotToContainer(new Slot(inv, playerInventoryStart+(y*3)+x,
      px+(x*18), py+(y*18)))
    }
    for (x <- 0 until 9) {
      this.doAddSlotToContainer(new Slot(inv, playerInventoryStart+27+x,
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
      if (!this.doMergeItemStack(slotStack, playerInventoryStart, playerInventoryStart+36, backwards=true)) {
        return null
      }
    } else if (!this.doMergeItemStack(slotStack, 0, playerInventoryStart, backwards=false)) {
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

  // stupid crap because the trait can't access protected things in Container
  def getFontRenderer: FontRenderer
  def getXSize: Int
  def getYSize: Int
  def getMC: Minecraft

  lazy val guiWidth = getXSize
  lazy val guiHeight = getYSize
  lazy val actualFontRenderer = getFontRenderer
  lazy val actualMC = getMC

  override protected def drawGuiContainerForegroundLayer(a: Int, b: Int) = {
    super.drawGuiContainerForegroundLayer(a, b)
    actualFontRenderer.drawString(inventoryName, 8, 6, 0x404040)
    actualFontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, guiHeight-94, 0x404040)
  }

  override protected def drawGuiContainerBackgroundLayer(a: Float, b: Int, c: Int) = {
    GL11.glColor4f(1f, 1f, 1f, 1f)
    this.actualMC.renderEngine.func_110577_a(guiTexture)
    this.drawTexturedModalRect((width-guiWidth)/2, (height-guiHeight)/2, 0, 0, guiWidth, guiHeight)
  }
}

object GUIHandler extends IGuiHandler {
  override def getServerGuiElement(id: Int, player: EntityPlayer, world: World,
                                    x: Int, y: Int, z: Int): Object =
     world.getBlockTileEntity(x, y, z) match {
       case te: Inventory => te.getContainer(player.inventory)
       case _ => null
     }
  override def getClientGuiElement(id: Int, player: EntityPlayer, world: World,
                                    x: Int, y: Int, z: Int): Object =
    world.getBlockTileEntity(x, y, z) match {
      case te: Inventory => te.getGUI(player.inventory)
      case _ => null
    }
}
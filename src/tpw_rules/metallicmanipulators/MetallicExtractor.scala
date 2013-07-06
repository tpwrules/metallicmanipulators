package tpw_rules.metallicmanipulators

import net.minecraft.block.{BlockContainer, material}
import material.Material
import net.minecraft.client.renderer.texture.IconRegister
import net.minecraft.util.Icon
import net.minecraft.world.World
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.tileentity.TileEntity
import net.minecraft.item.ItemStack
import net.minecraft.inventory.ISidedInventory

class BlockMetallicExtractor(id: Int) extends BlockContainer(id, Material.iron) with BlockMachine {
  var frontTexture: Icon = null
  var sideTexture: Icon = null

  setHardness(5f)
  setResistance(10f)
  setUnlocalizedName("metallicExtractor")

  GameRegistry.registerBlock(this, "MetallicExtractor")
  GameRegistry.registerTileEntity(classOf[TileMetallicExtractor], "TEMetallicExtractor")

  override def registerIcons(ir: IconRegister) = {
    frontTexture = ir.registerIcon("metallicmanipulators:metallicExtractorFront")
    sideTexture = ir.registerIcon("metallicmanipulators:metallicExtractorSide")
  }

  override def createNewTileEntity(world: World): TileEntity = new TileMetallicExtractor
}

class TileMetallicExtractor extends TileEntity with SidedInventory {
  val inventorySize = 18
  var inv = new Array[ItemStack](inventorySize)

  override def updateEntity() = {
    var changed = false
    for (slot <- 0 until 9; stack = getStackInSlot(slot); if stack != null) {
      setInventorySlotContents(slot+9, stack)
      setInventorySlotContents(slot, null)
      changed = true
    }
    if (changed) onInventoryChanged()
  }

  def canInsertItem(slot: Int, stack: ItemStack, side: Int) =
    side match {
      case 1 => slot < 9 // top
      case _ => false
    }

  def canExtractItem(slot: Int, stack: ItemStack, side: Int) =
    side match {
      case 0 => slot >= 9 && slot < 18 // bottom
      case _ => false
    }

  def getAccessibleSlotsFromSide(side: Int): Array[Int] =
    side match {
      case 1 => (0 until 9).toArray
      case 0 => (9 until 18).toArray
      case _ => Array()
    }

  def getInvName = "Metallic Extractor"
  def isInvNameLocalized = true
}
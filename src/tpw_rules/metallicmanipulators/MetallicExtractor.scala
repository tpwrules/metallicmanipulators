package tpw_rules.metallicmanipulators

import net.minecraft.block.{BlockContainer, material, ITileEntityProvider }
import material.Material
import net.minecraft.client.renderer.texture.IconRegister
import net.minecraft.util.Icon
import net.minecraft.world.World
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.tileentity.TileEntity
import net.minecraft.item.ItemStack

class BlockMetallicExtractor(id: Int) extends BlockContainer(id, Material.iron) with BlockMachine {
  var frontTexture: Icon = null
  var sideTexture: Icon = null

  setHardness(5f)
  setResistance(10f)
  setUnlocalizedName("metallicExtractor")

  GameRegistry.registerBlock(this, "MetallicExtractor")

  override def registerIcons(ir: IconRegister) = {
    frontTexture = ir.registerIcon("metallicmanipulators:metallicExtractorFront")
    sideTexture = ir.registerIcon("metallicmanipulators:metallicExtractorSide")
  }

  override def createNewTileEntity(world: World): TileEntity = new TileMetallicExtractor
}

class TileMetallicExtractor extends TileEntity with Inventory {
  val inventorySize = 18
  var inv = new Array[ItemStack](inventorySize)

  def getInvName = "Metallic Extractor"
  def isInvNameLocalized = true
}
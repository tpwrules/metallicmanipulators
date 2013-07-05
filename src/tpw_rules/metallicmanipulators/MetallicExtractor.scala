package tpw_rules.metallicmanipulators

import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.block.{Block, material}
import material.Material
import net.minecraft.client.renderer.texture.IconRegister
import net.minecraft.util.Icon
import net.minecraft.world.World
import net.minecraft.item.ItemStack
import net.minecraft.entity.EntityLivingBase
import cpw.mods.fml.common.registry.GameRegistry

class BlockMetallicExtractor(id: Int) extends Block(id, Material.iron) with Front {
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
}
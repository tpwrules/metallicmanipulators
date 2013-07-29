package tpw_rules.metallicmanipulators

import net.minecraft.{util, world, entity, item, block}
import util.{Icon, MathHelper}
import world.World
import block.Block
import entity.EntityLivingBase
import item.ItemStack
import net.minecraftforge.common.ForgeDirection

trait Front extends Block {
  var frontTexture: Icon
  var sideTexture: Icon

  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, item: ItemStack): Unit = {
    val facing = MathHelper.floor_double((entity.rotationYaw * 4F) / 360F + 0.5D) & 3
    world.setBlockMetadataWithNotify(x, y, z, facing, 2)
    super.onBlockPlacedBy(world, x, y, z, entity, item)
  }

  override def getIcon(side: Int, meta: Int): Icon = 
    if (side == meta) frontTexture else sideTexture
}
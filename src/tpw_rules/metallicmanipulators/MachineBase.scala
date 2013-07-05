package tpw_rules.metallicmanipulators

import net.minecraft.block.BlockContainer
import net.minecraft.world.World
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity

trait BlockMachine extends BlockContainer with Front {
  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, item: ItemStack): Unit = {
    super.onBlockPlacedBy(world, x, y, z, entity, item)
    if (world.isRemote) return
    world.getBlockTileEntity(x, y, z).asInstanceOf[TileMachine].placed
  }

  override def breakBlock(world: World, x: Int, y: Int, z: Int, side: Int, meta: Int) = {
    if (!world.isRemote) world.getBlockTileEntity(x, y, z).asInstanceOf[TileMachine].broken
    super.breakBlock(world, x, y, z, side, meta)
  }
}

trait TileMachine extends TileEntity {
  def placed
  def broken
}
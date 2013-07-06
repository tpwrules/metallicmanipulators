package tpw_rules.metallicmanipulators

import net.minecraft.block.BlockContainer
import net.minecraft.world.World
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.network.packet.{Packet132TileEntityData, Packet}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.INetworkManager
import net.minecraft.entity.player.EntityPlayer

trait BlockMachine extends BlockContainer with Front {
  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, item: ItemStack): Unit = {
    super.onBlockPlacedBy(world, x, y, z, entity, item)
    if (world.isRemote) return
    world.getBlockTileEntity(x, y, z).asInstanceOf[TileMachine].placed()
  }

  override def breakBlock(world: World, x: Int, y: Int, z: Int, side: Int, meta: Int) = {
    if (!world.isRemote) world.getBlockTileEntity(x, y, z).asInstanceOf[TileMachine].broken()
    super.breakBlock(world, x, y, z, side, meta)
  }
}

trait BlockGUI extends BlockContainer {
  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer,
                                 meta: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (player.isSneaking) false
    player.openGui(MetallicManipulators, 0, world, x, y, z)
    true
  }
}

trait TileMachine extends TileEntity {
  def placed() = {}
  def broken() = {}
}

trait DescriptionPacket extends TileEntity {
  def writeDescriptionPacket(tag: NBTTagCompound) = {}
  def readDescriptionPacket(tag: NBTTagCompound) = {}

  override def getDescriptionPacket: Packet = {
    val tag = new NBTTagCompound
    this.writeToNBT(tag)
    this.writeDescriptionPacket(tag)
    new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, tag)
  }

  override def onDataPacket(net: INetworkManager, packet: Packet132TileEntityData) = {
    val tag = packet.customParam1
    this.readFromNBT(tag)
    this.readDescriptionPacket(tag)
  }
}
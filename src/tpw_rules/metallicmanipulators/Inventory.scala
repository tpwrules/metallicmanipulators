package tpw_rules.metallicmanipulators

import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.entity.player.EntityPlayer
import scala.util.Random
import net.minecraft.entity.item.EntityItem
import net.minecraft.nbt.{NBTBase, NBTTagByte, NBTTagCompound}

trait Inventory extends TileMachine with IInventory {
  val inventorySize: Int
  var inv: Array[ItemStack]

  def closeChest() = {}
  def openChest() = {}

  def getSizeInventory = inventorySize
  def getInventoryStackLimit = 64

  def decrStackSize(slot: Int, amount: Int): ItemStack = {
    var stack = getStackInSlot(slot)
    if (stack == null) return null
    if (stack.stackSize <= amount) {
      setInventorySlotContents(slot, null)
    } else {
      stack = stack.splitStack(amount)
      if (stack.stackSize == 0) setInventorySlotContents(slot, null)
    }
    stack
  }

  def getStackInSlotOnClosing(slot: Int): ItemStack = {
    val stack = getStackInSlot(slot)
    if (stack != null) setInventorySlotContents(slot, null)
    stack
  }
  def getStackInSlot(slot: Int) = inv(slot)
  def setInventorySlotContents(slot: Int, stack: ItemStack) = {
    inv(slot) = stack
    if (stack != null && stack.stackSize > getInventoryStackLimit)
      stack.stackSize = getInventoryStackLimit
  }

  def isUseableByPlayer(player: EntityPlayer) = true
  def isStackValidForSlot(slot: Int, stack: ItemStack) = true

  override def broken() = {
    vomitItems()
    super.broken()
  }

  def vomitItems() = {
    vomitItemList((inv filter {x => x != null}).toList)
  }

  def vomitItemList(items: List[ItemStack]) = {
    val rand = new Random
    items foreach { item =>
      val entity = new EntityItem(worldObj,
        xCoord+(rand.nextFloat()*.8+.1), yCoord+(rand.nextFloat()*.8+.1),
        zCoord+(rand.nextFloat()*.8+.1), new ItemStack(item.itemID,
          item.stackSize, item.getItemDamage))
      if (item.hasTagCompound)
        entity.getEntityItem.setTagCompound(item.getTagCompound.copy().asInstanceOf[NBTTagCompound])
      entity.motionX = rand.nextGaussian*.05
      entity.motionY = rand.nextGaussian*.05 + .2
      entity.motionZ = rand.nextGaussian*.05
      worldObj.spawnEntityInWorld(entity)
    }
  }
}
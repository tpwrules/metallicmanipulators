package tpw_rules.metallicmanipulators

import net.minecraft.inventory.{ISidedInventory, IInventory}
import net.minecraft.item.ItemStack
import net.minecraft.entity.player.EntityPlayer
import scala.util.Random
import net.minecraft.entity.item.EntityItem
import net.minecraft.nbt.{NBTTagList, NBTTagCompound}

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

  override def writeToNBT(tag: NBTTagCompound) = {
    super.writeToNBT(tag)
    println("Writing!")
    val invList = new NBTTagList()
    for (slot <- 0 until inventorySize) {
      val stack = inv(slot)
      if (stack != null) {
        val slotTag = new NBTTagCompound()
        stack.writeToNBT(slotTag)
        tag.setByte("slot", slot.toByte)
        invList.appendTag(slotTag)
      }
    }
    tag.setTag("inventory", invList)
  }

  override def readFromNBT(tag: NBTTagCompound) = {
    super.readFromNBT(tag)
    val invList = tag.getTagList("inventory")
    println("Reading!", invList.tagCount)
    for (i <- 0 until invList.tagCount; slotTag = invList.tagAt(i).asInstanceOf[NBTTagCompound]) {
      val slot = slotTag.getByte("slot").toInt
      if (slot >= 0 && slot < inventorySize)
        inv(slot) = ItemStack.loadItemStackFromNBT(slotTag)
    }
  }
}

trait SidedInventory extends TileMachine with Inventory with ISidedInventory
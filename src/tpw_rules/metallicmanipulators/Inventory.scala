package tpw_rules.metallicmanipulators

import net.minecraft.inventory.{ISidedInventory, IInventory}
import net.minecraft.item.ItemStack
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import scala.util.Random
import net.minecraft.entity.item.EntityItem
import net.minecraft.nbt.{NBTTagList, NBTTagCompound}

trait Inventory extends TileMachine with IInventory {
  val inventorySize: Int
  var inv: Array[ItemStack]

  var inventoryChanged = false

  def closeChest() = {}
  def openChest() = {}

  def getSizeInventory = inventorySize
  def getInventoryStackLimit = 64

  def getGUI(invPlayer: InventoryPlayer): StandardGUI
  def getContainer(invPlayer: InventoryPlayer): StandardContainer

  override def updateEntity() {
    super.updateEntity()
    if (inventoryChanged) {
      inventoryChanged = false
      onInventoryChanged()
    }
  }

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

  def isUseableByPlayer(player: EntityPlayer) = player.getDistanceSq(xCoord+.5, yCoord+.5, zCoord+.5) < 64
  def isItemValidForSlot(slot: Int, stack: ItemStack) = true

  override def broken() = {
    super.broken()
    vomitItemList(inv.toList)
  }

  def vomitItemList(items: List[ItemStack]) = {
    val rand = MetallicManipulators.rand
    for (item <- items; if item != null) {
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
    val invList = new NBTTagList()
    for (slot <- 0 until inventorySize; stack = inv(slot); if stack != null) {
      val slotTag = new NBTTagCompound()
      stack.writeToNBT(slotTag)
      slotTag.setByte("slot", slot.toByte)
      invList.appendTag(slotTag)
    }
    tag.setTag("inventory", invList)
  }

  override def readFromNBT(tag: NBTTagCompound) = {
    super.readFromNBT(tag)
    val invList = tag.getTagList("inventory")
    for (i <- 0 until invList.tagCount; slotTag = invList.tagAt(i).asInstanceOf[NBTTagCompound]) {
      val slot = slotTag.getByte("slot").toInt
      if (slot >= 0 && slot < inventorySize)
        inv(slot) = ItemStack.loadItemStackFromNBT(slotTag)
    }
  }

  def mergeStackToSlots(stack: ItemStack, start: Int, end: Int): Boolean = mergeStackToSlots(stack, this, start, end)

  def mergeStackToSlots(stack: ItemStack, te: IInventory, start: Int, end: Int) = {
    var done = false
    for (slot <- start until end; currentStack = te.getStackInSlot(slot); if !done; if // split because of stupid unneeded semicolon warning
      currentStack != null; if stack.itemID == currentStack.itemID && (!stack.getHasSubtypes || stack.getItemDamage == currentStack.getItemDamage) && ItemStack.areItemStackTagsEqual(stack, currentStack)) {
      if (currentStack.stackSize+stack.stackSize > currentStack.getMaxStackSize) {
        stack.stackSize -= currentStack.getMaxStackSize-currentStack.stackSize
        currentStack.stackSize = currentStack.getMaxStackSize
      } else {
        currentStack.stackSize += stack.stackSize
        stack.stackSize = 0
        done = true
      }
    }
    if (!done) {
      for (slot <- start until end; currentStack = te.getStackInSlot(slot); if currentStack == null; if !done) {
        te.setInventorySlotContents(slot, stack)
        done = true
      }
    }
    te match {
      case x: Inventory => x.inventoryChanged = true
      case x => x.onInventoryChanged()
    }
    done
  }
}

trait SidedInventory extends TileMachine with Inventory with ISidedInventory
package tpw_rules.metallicmanipulators

import net.minecraft.inventory.{Slot, IInventory, Container}
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import net.minecraft.item.ItemStack

trait StandardContainer extends Container with Inventory {
  protected var te: IInventory
  val playerInventoryStart: Int

  override def canInteractWith(player: EntityPlayer) = te.isUseableByPlayer(player)

  def addPlayerSlots(inv: InventoryPlayer, px: Int, py: Int) = {
    for (y <- 0 until 3; x <- 0 until 9) {
      this.addSlotToContainer(new Slot(inv, playerInventoryStart+(y*3)+x,
      px+(x*18), py+(y*18)))
    }
    for (x <- 0 until 9) {
      this.addSlotToContainer(new Slot(inv, playerInventoryStart+27+x,
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
      if (!this.mergeItemStack(slotStack, playerInventoryStart, playerInventoryStart+36, true)) {
        return null
      }
    } else if (!this.mergeItemStack(slotStack, 0, playerInventoryStart, false)) {
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
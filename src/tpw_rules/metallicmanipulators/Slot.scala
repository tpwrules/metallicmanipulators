package tpw_rules.metallicmanipulators

import net.minecraft.inventory.{IInventory, Slot}
import net.minecraft.item.ItemStack

class SlotOutput(inv: IInventory, idx: Int, x: Int, y: Int) extends Slot(inv, idx, x, y) {
  override def isItemValid(stack: ItemStack) = false
}

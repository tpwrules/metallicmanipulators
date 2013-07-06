package tpw_rules.metallicmanipulators

import net.minecraft.inventory.{IInventory, Slot}
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityFurnace

class SlotOutput(inv: IInventory, idx: Int, x: Int, y: Int) extends Slot(inv, idx, x, y) {
  override def isItemValid(stack: ItemStack) = false
}

class SlotFuel(inv: IInventory, idx: Int, x: Int, y: Int) extends Slot(inv, idx, x, y) {
  override def isItemValid(stack: ItemStack) = TileEntityFurnace.getItemBurnTime(stack) > 0
}
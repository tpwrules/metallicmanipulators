package tpw_rules.metallicmanipulators

import net.minecraft.item.{ItemStack, Item}
import net.minecraft.item.crafting.CraftingManager

class ItemIronNugget(id: Int) extends Item(id) {
  setCreativeTab(MetallicManipulators.tabItems)
  setUnlocalizedName("ironNugget")
  func_111206_d("metallicmanipulators:ironNugget")

  val t = CraftingManager.getInstance
  // add recipe to convert nuggets to iron
  t.addRecipe(new ItemStack(Item.ingotIron),
    "XXX", "XXX", "XXX",
    'X'.asInstanceOf[Object], new ItemStack(this))
  // add recipe to convert iron to nuggets
  t.addShapelessRecipe(new ItemStack(this, 9),
    new ItemStack(Item.ingotIron))
}

package tpw_rules.metallicmanipulators

import net.minecraft.block.{BlockContainer, material}
import material.Material
import net.minecraft.client.renderer.texture.IconRegister
import net.minecraft.util.Icon
import net.minecraft.world.World
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.tileentity.TileEntity
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.item.crafting.{ShapelessRecipes, ShapedRecipes, IRecipe, CraftingManager}
import net.minecraftforge.oredict.{ShapedOreRecipe, OreDictionary}

import scala.collection.JavaConversions._

class BlockMetallicExtractor(id: Int) extends BlockContainer(id, Material.iron) with BlockMachine {
  var frontTexture: Icon = null
  var sideTexture: Icon = null

  setHardness(5f)
  setResistance(10f)
  setUnlocalizedName("metallicExtractor")

  GameRegistry.registerBlock(this, "MetallicExtractor")
  GameRegistry.registerTileEntity(classOf[TileMetallicExtractor], "TEMetallicExtractor")

  override def registerIcons(ir: IconRegister) = {
    frontTexture = ir.registerIcon("metallicmanipulators:metallicExtractorFront")
    sideTexture = ir.registerIcon("metallicmanipulators:metallicExtractorSide")
  }

  override def createNewTileEntity(world: World): TileEntity = new TileMetallicExtractor
}

object TileMetallicExtractor {
  lazy val recipeList = CraftingManager.getInstance().getRecipeList.asInstanceOf[java.util.ArrayList[IRecipe]].toList
}

class TileMetallicExtractor extends TileEntity with SidedInventory {
  val inventorySize = 18
  var inv = new Array[ItemStack](inventorySize)

  override def updateEntity() = {
    var changed = false
    for (slot <- 0 until 9; stack = getStackInSlot(slot); if stack != null) {
      performOperation(decrStackSize(slot, 1))
      changed = true
    }
    if (changed) onInventoryChanged()
  }

  def performOperation(inputStack: ItemStack): Unit = {
    val stackID = inputStack.itemID
    // get the recipes that can make this item
    val recipes = TileMetallicExtractor.recipeList filter { x =>
      val output = x.getRecipeOutput
      output != null && output.stackSize == 1 && output.itemID == stackID}
    if (recipes.length != 1) return // return if we found either no or too many recipes
    // get the list of items this recipe requires
    val inputList = recipes(0) match {
        case x: ShapedRecipes => x.recipeItems.toList
        case x: ShapedOreRecipe => x.getInput.toList map { x => x match {
          case ores: Array[ItemStack] => ores(0)
          case x: ItemStack => x
          case _ => null
        }}
        case x: ShapelessRecipes => x.recipeItems.asInstanceOf[java.util.ArrayList[ItemStack]].toList
        case _ => List()
      }
    // get the items we can turn into outputs
    val elgibleOutputs = inputList filter { x => x != null } filter { x =>
        x.itemID == Item.ingotGold.itemID ||
        x.itemID == Item.ingotIron.itemID ||
        (OreDictionary.getOreName(OreDictionary.getOreID(x)) startsWith "ingot")
      }
    // combine similar items into output stacks
    var outputStacks: List[ItemStack] = List()
    for (elgibleStack <- elgibleOutputs) {
      var merged = false
      for (outputStack <- outputStacks) {
        if (elgibleStack.itemID == outputStack.itemID && (!elgibleStack.getHasSubtypes || elgibleStack.getItemDamage == outputStack.getItemDamage)) {
          outputStack.stackSize += elgibleStack.stackSize
          merged = true
        }
      }
      if (!merged) outputStacks = elgibleStack.copy :: outputStacks
    }
  }

  def canInsertItem(slot: Int, stack: ItemStack, side: Int) =
    side match {
      case 1 => slot < 9 // top
      case _ => false
    }

  def canExtractItem(slot: Int, stack: ItemStack, side: Int) =
    side match {
      case 0 => slot >= 9 && slot < 18 // bottom
      case _ => false
    }

  def getAccessibleSlotsFromSide(side: Int): Array[Int] =
    side match {
      case 1 => (0 until 9).toArray
      case 0 => (9 until 18).toArray
      case _ => Array()
    }

  def getInvName = "Metallic Extractor"
  def isInvNameLocalized = true
}
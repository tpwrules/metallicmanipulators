package tpw_rules.metallicmanipulators

import net.minecraft.block.{BlockContainer, material}
import material.Material
import net.minecraft.client.renderer.texture.IconRegister
import net.minecraft.util.{MathHelper, Icon}
import net.minecraft.world.World
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.tileentity.TileEntity
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.item.crafting.{ShapelessRecipes, ShapedRecipes, IRecipe, CraftingManager}
import net.minecraftforge.oredict.{ShapedOreRecipe, OreDictionary}

import scala.collection.JavaConversions._
import net.minecraft.nbt.{NBTTagList, NBTTagCompound}
import net.minecraft.inventory.{Slot, IInventory, Container}
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.resources.ResourceLocation

class BlockMetallicExtractor(id: Int) extends BlockContainer(id, Material.iron) with BlockMachine with BlockGUI {
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

  var outbuf: List[ItemStack] = List()

  override def updateEntity(): Unit = {
    if (worldObj.isRemote) return
    var changed = false
    if (outbuf.length > 0) {
      dumpOutput(List())
    } else {
      for (slot <- 0 until 9; stack = getStackInSlot(slot); if stack != null; if !changed) {
        performOperation(decrStackSize(slot, 1))
        changed = true
      }
    }
    if (changed) onInventoryChanged()
  }

  def dumpOutput(output: List[ItemStack]) = {
    outbuf = output ++ outbuf filter {x => !mergeStackToSlots(x, 9, 18)}
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
    // reduce output according to item's damage
    if (inputStack.isItemStackDamageable) {
      val damageMultiplier = 1-(inputStack.getItemDamage.toDouble/inputStack.getMaxDamage)
      //outputStacks foreach {stack => stack.stackSize = MathHelper.floor_double(stack.stackSize * damageMultiplier)}
      outputStacks foreach { stack =>
        val part = stack.stackSize*damageMultiplier
        val nuggets = MathHelper.floor_double((part-part.toInt)*9)
        if (stack.itemID == Item.ingotGold.itemID && nuggets > 0)
          outputStacks = new ItemStack(Item.goldNugget, nuggets) :: outputStacks
        stack.stackSize = MathHelper.floor_double(part)
      }
    }
    // and finally, stick the output into our inventory
    dumpOutput(outputStacks)
  }

  override def writeToNBT(tag: NBTTagCompound): Unit = {
    super.writeToNBT(tag)
    if (outbuf.length == 0) return
    val invList = new NBTTagList()
    outbuf foreach { stack =>
      val stackTag = new NBTTagCompound()
      stack.writeToNBT(stackTag)
      invList.appendTag(stackTag)
    }
    tag.setTag("outBuffer", invList)
  }

  override def readFromNBT(tag: NBTTagCompound): Unit = {
    super.readFromNBT(tag)
    outbuf = List()
    if (!tag.hasKey("outBuffer")) return
    val invList = tag.getTagList("outBuffer")
    for (i <- 0 until invList.tagCount) {
      val stackTag = invList.tagAt(i).asInstanceOf[NBTTagCompound]
      outbuf = ItemStack.loadItemStackFromNBT(stackTag) :: outbuf
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

  override def getContainer(invPlayer: InventoryPlayer) =
     new ContainerMetallicExtractor(invPlayer, this).asInstanceOf[StandardContainer]
  override def getGUI(invPlayer: InventoryPlayer) =
    new GuiMetallicExtractor(invPlayer, this).asInstanceOf[StandardGUI]
}

class ContainerMetallicExtractor(playerInv: InventoryPlayer, te: TileMetallicExtractor) extends
    Container with StandardContainer {
  val playerInventoryStart = 18
  val tileEntity = te.asInstanceOf[IInventory]

  for (y <- 0 until 3; x <- 0 until 3) {
    addSlotToContainer(new Slot(tileEntity, (y*3)+x,
      18+(x*18), 17+(y*18)))
  }
  for (y <- 0 until 3; x <- 0 until 3) {
    addSlotToContainer(new SlotOutput(tileEntity, 9+(y*3)+x,
      106+(x*18), 17+(y*18)))
  }

  addPlayerSlots(playerInv, 8, 107)

  // stupid crap because the trait can't access protected things in Container
  override def doAddSlotToContainer(slot: Slot) = addSlotToContainer(slot)
  override def doMergeItemStack(stack: ItemStack, start: Int, end: Int, backwards: Boolean) =
    mergeItemStack(stack, start, end, backwards)
}

class GuiMetallicExtractor(playerInv: InventoryPlayer, te: TileMetallicExtractor) extends
    GuiContainer(new ContainerMetallicExtractor(playerInv, te)) with StandardGUI {
  xSize = 176
  ySize = 189
  val inventoryName = "Metallic Extractor"
  val guiTexture = new ResourceLocation("metallicmanipulators", "textures/gui/metallicExtractor.png")

  // stupid crap because the trait can't access protected things in Container
  def getFontRenderer = this.fontRenderer
  def getXSize = this.xSize
  def getYSize = this.ySize
  def getMC = this.mc
}
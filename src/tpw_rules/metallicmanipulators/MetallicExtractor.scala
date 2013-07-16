package tpw_rules.metallicmanipulators

import scala.collection.JavaConversions._

import net.minecraft.block.{BlockContainer, material}
import material.Material
import net.minecraft.client.renderer.texture.IconRegister
import net.minecraft.util.{ResourceLocation, MathHelper, Icon}
import net.minecraft.world.World
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.tileentity.{TileEntityFurnace, TileEntity}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.item.crafting.{ShapelessRecipes, ShapedRecipes, IRecipe, CraftingManager}
import net.minecraftforge.oredict.{ShapedOreRecipe, OreDictionary}
import net.minecraft.nbt.{NBTTagList, NBTTagCompound}
import net.minecraft.inventory.{ICrafting, Slot, IInventory, Container}
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.client.gui.inventory.GuiContainer

class BlockMetallicExtractor(id: Int) extends BlockContainer(id, Material.iron) with BlockMachine with BlockGUI {
  var frontTexture: Icon = null
  var sideTexture: Icon = null

  setHardness(5f)
  setResistance(10f)
  setUnlocalizedName("metallicExtractor")
  setCreativeTab(MetallicManipulators.tabBlocks)

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

class TileMetallicExtractor extends TileEntity with SidedInventory with Output {
  val inventorySize = 19
  val outputStart = 9
  val outputEnd = 18
  var inv = new Array[ItemStack](inventorySize)

  var workItem: ItemStack = null

  var progress = 0

  var power = 0

  override def updateEntity(): Unit = {
    if (worldObj.isRemote) return // don't do work if on the client
    // consume an item from the power slot if necessary
    if (power <= 800 && inv(18) != null) {
      power += TileEntityFurnace.getItemBurnTime(decrStackSize(18, 1))*2
      inventoryChanged = true
    }
    if (outputBuf.length > 0) return // don't do work if there is pending output
    if (workItem == null && power >= 800) { // grab an item to work on if we aren't currently working on one
      workItem = getWorkItem
    } else if (power >= 8) {
      progress += 1
      power -= 8
      if (progress >= 100) {
        performOperation(workItem)
        progress = 0
        workItem = if (outputBuf.length > 0 && power >= 800) getWorkItem else null
      }
    }
  }

  def getWorkItem: ItemStack = {
    for (slot <- 0 until 9; stack = getStackInSlot(slot); if stack != null) {
      val out = decrStackSize(slot, 1)
      inventoryChanged = true
      return out
    }
    null
  }

  def performOperation(inputStack: ItemStack): Unit = {
    val stackID = inputStack.itemID
    // get the recipes that can make this item
    val recipes = TileMetallicExtractor.recipeList filter { x =>
      val output = x.getRecipeOutput
      output != null && output.stackSize == 1 && output.itemID == stackID }
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
      for (outputStack <- outputStacks; if !merged) {
        if (elgibleStack.itemID == outputStack.itemID && (!elgibleStack.getHasSubtypes || elgibleStack.getItemDamage == outputStack.getItemDamage)) {
          outputStack.stackSize += 1
          merged = true
        }
      }
      if (!merged) {
        val t = elgibleStack.copy
        t.stackSize = 1
        outputStacks = t :: outputStacks
      }
    }
    // reduce output according to item's damage
    if (inputStack.isItemStackDamageable) {
      val damageMultiplier = 1-(inputStack.getItemDamage.toDouble/inputStack.getMaxDamage)
      outputStacks foreach { stack =>
        val part = stack.stackSize*damageMultiplier
        val nuggets = MathHelper.floor_double((part-part.toInt)*9)
        if (stack.itemID == Item.ingotGold.itemID && nuggets > 0)
          outputStacks = new ItemStack(Item.goldNugget, nuggets) :: outputStacks
        else if (stack.itemID == Item.ingotIron.itemID && nuggets > 0)
          outputStacks = new ItemStack(MetallicManipulators.ironNugget, nuggets) :: outputStacks
        stack.stackSize = MathHelper.floor_double(part)
      }
    }
    // and finally, stick the output into our inventory
    addToOutput(outputStacks)
  }

  override def broken() = {
    if (workItem != null) vomitItemList(List(workItem))
  }

  override def writeToNBT(tag: NBTTagCompound) = {
    super.writeToNBT(tag)
    if (workItem != null)
      tag.setCompoundTag("workItem", workItem.writeToNBT(new NBTTagCompound))
    tag.setInteger("progress", progress)
    tag.setInteger("power", power)
  }

  override def readFromNBT(tag: NBTTagCompound) = {
    super.readFromNBT(tag)
    workItem = null
    if (tag.hasKey("workItem"))
      workItem = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("workItem"))
    progress = tag.getInteger("progress")
    if (progress < 0 || progress > 100) progress = 0
    power = tag.getInteger("power")
    if (power < 0) power = 0
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

  override def isItemValidForSlot(slot: Int, stack: ItemStack) =
    slot match {
      case 18 => TileEntityFurnace.getItemBurnTime(stack) > 0
      case _ => true
    }

  def getAccessibleSlotsFromSide(side: Int): Array[Int] =
    side match {
      case 1 => (0 until 9).toArray
      case 0 => (9 until 18).toArray
      case _ => Array()
    }

  def getInvName = "Metallic Extractor"
  def isInvNameLocalized = true

  def getScaledProgress = MathHelper.clamp_int(MathHelper.floor_double(progress*24/100), 0, 24)
  def getScaledPower = MathHelper.clamp_int(MathHelper.floor_double(power*127/5000), 0, 127)

  override def getContainer(invPlayer: InventoryPlayer) =
     new ContainerMetallicExtractor(invPlayer, this).asInstanceOf[StandardContainer]
  override def getGUI(invPlayer: InventoryPlayer) =
    new GuiMetallicExtractor(invPlayer, this).asInstanceOf[StandardGUI]
}

class ContainerMetallicExtractor(playerInv: InventoryPlayer, te: TileMetallicExtractor) extends
    Container with StandardContainer {
  val playerInventoryStart = 19
  val tileEntity = te.asInstanceOf[IInventory]

  var currentProgress = -1
  var currentPower = -1

  for (y <- 0 until 3; x <- 0 until 3) {
    addSlotToContainer(new Slot(tileEntity, (y*3)+x,
      18+(x*18), 17+(y*18)))
  }
  for (y <- 0 until 3; x <- 0 until 3) {
    addSlotToContainer(new SlotOutput(tileEntity, 9+(y*3)+x,
      106+(x*18), 17+(y*18)))
  }
  addSlotToContainer(new SlotFuel(tileEntity, 18, 8, 76))

  addPlayerSlots(playerInv, 8, 107)

  // stupid crap because the trait can't access protected things in Container
  override def doAddSlotToContainer(slot: Slot) = addSlotToContainer(slot)
  override def doMergeItemStack(stack: ItemStack, start: Int, end: Int, backwards: Boolean) =
    mergeItemStack(stack, start, end, backwards)

  override def merge(stack: ItemStack, slot: Int): Boolean = {
    if (slot < playerInventoryStart) {
      if (!this.mergeItemStack(stack, playerInventoryStart, playerInventoryStart+36, true)) {
        return false
      }
    } else if (TileEntityFurnace.getItemBurnTime(stack) == 0) {
      if (!this.mergeItemStack(stack, 0, 9, false)) return false
    } else if (!this.mergeItemStack(stack, 18, 19, false)) {
      return false
    }
    true
  }

  override def addCraftingToCrafters(crafter: ICrafting) = {
    super.addCraftingToCrafters(crafter)
    crafter.sendProgressBarUpdate(this, 0, te.progress)
    crafter.sendProgressBarUpdate(this, 1, te.power)
  }

  override def detectAndSendChanges() = {
    super.detectAndSendChanges()
    if (currentProgress != te.progress || currentPower != te.power) {
      this.crafters foreach { crafter =>
        val c = crafter.asInstanceOf[ICrafting]
        c.sendProgressBarUpdate(this, 0, te.progress)
        c.sendProgressBarUpdate(this, 1, te.power)
      }
    }
    currentProgress = te.progress
    currentPower = te.power
  }

  override def updateProgressBar(which: Int, value: Int): Unit = {
    if (!te.worldObj.isRemote) return
    which match {
      case 0 => te.progress = value
      case 1 => te.power = value
    }
  }
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

  override def drawDynamicElements(x: Int, y: Int) = {
    this.drawTexturedModalRect(x+76, y+35,
    176, 0, te.getScaledProgress, 17)
    this.drawTexturedModalRect(x+36, y+80,
    0, 189, te.getScaledPower, 8)
  }
}
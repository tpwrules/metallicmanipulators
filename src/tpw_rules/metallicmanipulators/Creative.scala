package tpw_rules.metallicmanipulators

import net.minecraft.creativetab.CreativeTabs

class CreativeTabBlocks(name: String) extends CreativeTabs(name: String) {
  def this() = this("metallicManipulatorsBlocks")

  override def getTabIconItemIndex = MetallicManipulators.metallicExtractor.blockID
}

class CreativeTabItems(name: String) extends CreativeTabs(name: String) {
  def this() = this("metallicManipulatorsItems")

  override def getTabIconItemIndex = MetallicManipulators.metallicExtractor.blockID
}
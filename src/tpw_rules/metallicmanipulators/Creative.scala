package tpw_rules.metallicmanipulators

import net.minecraft.creativetab.CreativeTabs

class CreativeTabBlocks extends CreativeTabs("metallicManipulatorsBlocks") {
  override def getTabIconItemIndex = MetallicManipulators.metallicExtractor.blockID
}

class CreativeTabItems extends CreativeTabs("metallicManipulatorsItems") {
  override def getTabIconItemIndex = MetallicManipulators.metallicExtractor.blockID
}
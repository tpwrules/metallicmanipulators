package tpw_rules.metallicmanipulators

import java.io.File
import net.minecraftforge.common.Configuration
import cpw.mods.fml.common.Loader

object Config {
  val config = new Configuration(new File(Loader.instance.getConfigDir, "metallicmanipulators.cfg"))

  config.load()

  val blockExtractorID = config.getBlock("MetallicExtractor", "ID", 2400).getInt

  val ingotNames = config.get("General", "ingotNames", Array("ingotIron", "ingotGold")).getStringList.toList

  config.save()
}
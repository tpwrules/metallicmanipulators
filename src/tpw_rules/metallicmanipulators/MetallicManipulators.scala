package tpw_rules.metallicmanipulators

import cpw.mods.fml.common.{ Mod, SidedProxy, event }
import cpw.mods.fml.common.network.{NetworkRegistry, NetworkMod}

@Mod(modLanguage="scala", modid="metallicmanipulators", name="Metallic Manipulators", version="1")
@NetworkMod(clientSideRequired=true, serverSideRequired=false)
object MetallicManipulators {
  @SidedProxy(
    clientSide="tpw_rules.metallicmanipulators.ClientProxy",
    serverSide="tpw_rules.metallicmanipulators.CommonProxy")
  var proxy: CommonProxy = null

  lazy val metallicExtractor = new BlockMetallicExtractor(Config.blockExtractorID)

  NetworkRegistry.instance.registerGuiHandler(this, GUIHandler)

  @Mod.EventHandler
  def preinit(e: event.FMLPreInitializationEvent) = {
    val lolol = metallicExtractor
  }

  @Mod.EventHandler
  def init(e: event.FMLInitializationEvent) = {
  }
}
package tpw_rules.metallicmanipulators

import cpw.mods.fml.common.{ Mod, SidedProxy, event }
import cpw.mods.fml.common.network.{NetworkRegistry, NetworkMod}
import scala.util.Random

@Mod(modLanguage="scala", modid="metallicmanipulators", name="Metallic Manipulators", version="1")
@NetworkMod(clientSideRequired=true, serverSideRequired=false)
object MetallicManipulators {
  @SidedProxy(
    clientSide="tpw_rules.metallicmanipulators.ClientProxy",
    serverSide="tpw_rules.metallicmanipulators.CommonProxy")
  var proxy: CommonProxy = null

  val rand = new Random

  // we need to make all of these lazy in order to initialize them
  // at the proper time and keep them vals

  lazy val tabBlocks = new CreativeTabBlocks()
  lazy val tabItems = new CreativeTabItems()

  lazy val metallicExtractor = new BlockMetallicExtractor(Config.blockExtractorID)

  lazy val ironNugget = new ItemIronNugget(Config.itemIronNuggetID)

  @Mod.EventHandler
  def init(e: event.FMLInitializationEvent) = {
    var x: Any = null
    // ensure all the lazy vals are referenced so they are constructed
    x = tabBlocks
    x = tabItems
    x = metallicExtractor
    x = ironNugget

    NetworkRegistry.instance.registerGuiHandler(this, GUIHandler)
  }
}
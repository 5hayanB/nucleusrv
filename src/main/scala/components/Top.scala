package nucleusrv.components
import chisel3._
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig, BusDevice, BusHost}
// import caravan.bus.wishbone.{WBRequest, WBResponse, WishboneConfig}
import caravan.bus.tilelink.{TLRequest, TLResponse, TilelinkConfig}
import components.RVFIPORT
import jigsaw.rams.fpga.BlockRam

class Top(programFile:Option[String]) extends MultiIOModule{
    val pin = IO(Output(UInt(32.W)))
    val rvfi = IO(new RVFIPORT)

  implicit val config = WishboneConfig(32, 32) //WishboneConfig(32,32)

  val core: Core = Module(new Core(/*req, rsp*/ new WBRequest /*WBRequest*/,new WBResponse /*WBResponse*/)(M = true))
  val imemAdapter = Module(new WishboneAdapter() /*TilelinkAdapter()*/) //instrAdapter
  val dmemAdapter = Module(new WishboneAdapter() /*WishboneAdapter()*/) //dmemAdapter

  // TODO: Make RAMs generic
  val imemCtrl = Module(BlockRam.createNonMaskableRAM(programFile, config, 8192))
  val dmemCtrl = Module(BlockRam.createMaskableRAM(config, 1024))

  /*  Imem Interceonnections  */
  imemAdapter.reqIn <> core.imemReq
  core.imemRsp <> imemAdapter.rspOut
  imemCtrl.req <> imemAdapter.reqOut
  imemAdapter.rspIn <> imemCtrl.rsp

  /*  Dmem Interconnections  */
  dmemAdapter.reqIn <> core.dmemReq
  core.dmemRsp <> dmemAdapter.rspOut
  dmemCtrl.req <> dmemAdapter.reqOut
  dmemAdapter.rspIn <> dmemCtrl.rsp

  rvfi <> core.rvfi
  pin := core.pin

}
//class Top(programFile:Option[String]) extends Module{
//  val io = IO(new Bundle() {
//    val pin = Output(UInt(32.W))
//    val rvfi = new RVFIPORT
//  })
//
//  implicit val config = TilelinkConfig(32) //WishboneConfig(32,32)
//
//  val core: Core = Module(new Core(/*req, rsp*/ new TLRequest /*WBRequest*/,new TLResponse /*WBResponse*/))
//  val imemAdapter = Module(new TilelinkAdapter() /*TilelinkAdapter()*/) //instrAdapter
//  val dmemAdapter = Module(new TilelinkAdapter() /*WishboneAdapter()*/) //dmemAdapter
//
//  // TODO: Make RAMs generic
//  val imemCtrl = Module(BlockRam.createNonMaskableRAM(programFile, config, 8192))
//  val dmemCtrl = Module(BlockRam.createMaskableRAM(config, 1024))
//
//  /*  Imem Interceonnections  */
//  imemAdapter.reqIn <> core.imemReq
//  core.imemRsp <> imemAdapter.rspOut
//  imemCtrl.req <> imemAdapter.reqOut
//  imemAdapter.rspIn <> imemCtrl.rsp
//
//  /*  Dmem Interconnections  */
//  dmemAdapter.reqIn <> core.dmemReq
//  core.dmemRsp <> dmemAdapter.rspOut
//  dmemCtrl.req <> dmemAdapter.reqOut
//  dmemAdapter.rspIn <> dmemCtrl.rsp
//
//  rvfi <> core.rvfi
//  pin := core.pin
//
//}
package nucleusrv.components
import chisel3._
import rvfi_trace._

class Top(programFile:Option[String], dataFile:Option[String], RVFI:Boolean=false) extends Module{

  val io = IO(new Bundle() {
    val pin = Output(UInt(32.W))
  })


  val core: Core = Module(new Core(M = true, true))
  core.io.stall := false.B

  val dmem = Module(new SRamTop(dataFile))
  val imem = Module(new SRamTop(programFile))

  /*  Imem Interceonnections  */
  core.io.imemRsp <> imem.io.rsp
  imem.io.req <> core.io.imemReq


  /*  Dmem Interconnections  */
  core.io.dmemRsp <> dmem.io.rsp
  dmem.io.req <> core.io.dmemReq

  io.pin := core.io.pin

  //val rvfi = if (RVFI) Some(Module(new RVFIUnit(RVFI)))
  //           else None

  //if (RVFI) Seq(
  //        rvfi.get.io.mem_reg_ins.get,

  //        rvfi.get.io.id_reg_rd1.get,
  //        rvfi.get.io.id_reg_rd2.get,
  //        rvfi.get.io.wb_rd.get,
  //        rvfi.get.io.rs1_addr.get,
  //        rvfi.get.io.rs2_addr.get,
  //        rvfi.get.io.wb_data.get,
  //        rvfi.get.io.writeEnable.get,

  //        rvfi.get.io.mem_reg_pc.get,
  //        rvfi.get.io.nextPC.get,

  //        rvfi.get.io.ex_reg_result.get,
  //        rvfi.get.io.readEnable.get,
  //        rvfi.get.io.memWriteEnable.get,
  //        rvfi.get.io.ex_reg_wd.get,
  //        rvfi.get.io.readData.get
  //) zip Seq(
  //        core.io.mem_reg_ins,
  //        
  //        core.io.id_reg_rd1,
  //        core.io.id_reg_rd2,
  //        core.io.wb_rd,
  //        core.io.rs1_addr,
  //        core.io.rs2_addr,
  //        core.io.wb_data,
  //        core.io.writeEnable,

  //        core.io.mem_reg_pc,
  //        core.io.nextPC,
  //        
  //        core.io.ex_reg_result,
  //        core.io.readEnable,
  //        core.io.memWriteEnable,
  //        core.io.ex_reg_wd,
  //        core.io.readData
  //  ) foreach {
  //  x => x._1 := x._2.get
  //} else None
}

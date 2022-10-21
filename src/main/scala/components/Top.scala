package nucleusrv.components
import chisel3._
import rvfi_trace._

class Top(programFile:Option[String], dataFile:Option[String], RVFI:Boolean=false) extends Module{

  val io = IO(new Bundle() {
    val pin = Output(UInt(32.W))
  })


  val core: Core = Module(new Core(M = true, RVFI))
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

  val tracer = if (RVFI) Some(Module(new RVFIUnit(RVFI))) else None


  if (RVFI) Seq(
    (tracer.get.io.mem_reg_ins, core.io.mem_reg_ins),
    (tracer.get.io.ex_reg_ins, core.io.ex_reg_ins),

    (tracer.get.io.id_reg_rd1, core.io.rs1_rdata),
    (tracer.get.io.id_reg_rd2, core.io.rs2_rdata),
    (tracer.get.io.wb_rd, core.io.wb_rd),
    (tracer.get.io.rs1_addr, core.io.rs1_addr),
    (tracer.get.io.rs2_addr, core.io.rs2_addr),
    (tracer.get.io.wb_data, core.io.wb_data),
    (tracer.get.io.writeEnable, core.io.writeEnable),

    (tracer.get.io.mem_reg_pc, core.io.mem_reg_pc),
    (tracer.get.io.nextPC, core.io.nextPC),
    
    (tracer.get.io.ex_reg_result, core.io.ex_reg_result),
    (tracer.get.io.readEnable, core.io.readEnable),
    (tracer.get.io.memWriteEnable, core.io.memWriteEnable),
    (tracer.get.io.ex_reg_wd, core.io.ex_reg_wd),
    (tracer.get.io.readData, core.io.readData),
    (tracer.get.io.hdu_if_reg_write, core.io.hdu_if_reg_write)
  ) map (n => n._1.get := n._2.get) else None
}

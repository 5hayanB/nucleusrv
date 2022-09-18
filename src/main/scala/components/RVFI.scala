package components

import chisel3._

class RVFIPORT extends MultiIOModule{
    val valid     = Output(Bool())
    val order     = Output(UInt(64.W))
    val insn      = Output(UInt(32.W))
    val trap      = Output(Bool())
    val halt      = Output(Bool())
    val intr      = Output(Bool())
    val ixl       = Output(UInt(2.W))
    val mode      = Output(UInt(2.W))
    val rs1_addr  = Output(UInt(5.W))
    val rs1_rdata = Output(SInt(32.W))
    val rs2_addr  = Output(UInt(5.W))
    val rs2_rdata = Output(SInt(32.W))
    val rd_addr   = Output(UInt(5.W))
    val rd_wdata  = Output(SInt(32.W))
    val pc_rdata  = Output(UInt(32.W))
    val pc_wdata  = Output(UInt(32.W))
//    val mem_addr  = Output(UInt(32.W))
//    val mem_rmask = Output(UInt(4.W))
//    val mem_wmask = Output(UInt(4.W))
//    val mem_rdata = Output(UInt(32.W))
//    val mem_wdata = Output(UInt(32.W))
}
class ModuleIO extends MultiIOModule {
  val stall = Input(Bool())
  val insn = Input(UInt(32.W))
  val rs1_rdata = Input(UInt(32.W))
  val rs2_rdata = Input(UInt(32.W))
  val rd_addr = Input(UInt(5.W))
  val rd_wdata = Input(UInt(32.W))
  val pc = Input(SInt(32.W))
  val pc_offset = Input(SInt(32.W))
  val pc_four = Input(SInt(32.W))
  val pc_src = Input(Bool())
  val rvfi = new RVFIPORT
}
class RVFI extends ModuleIO {
  // val io = IO(new ModuleIO)

  val rvfi_valid = RegInit(false.B)
  val rvfi_order = RegInit(0.U(64.W))
  val rvfi_halt = RegInit(false.B)
  val rvfi_intr = RegInit(false.B)
  val rvfi_mode = RegInit(3.U(2.W))

  rvfi_valid := !reset.asBool() && !stall

  when(rvfi_valid){
    rvfi_order := rvfi_order + 1.U
  }

  rvfi.pc_rdata := pc.asUInt()
  rvfi.pc_wdata := Mux(pc_src, pc_offset.asUInt(), pc_four.asUInt())

  rvfi.rs1_addr := insn(19, 15)
  rvfi.rs1_rdata := rs1_rdata.asSInt()
  rvfi.rs2_addr := insn(24, 20)
  rvfi.rs2_rdata := rs2_rdata.asSInt()

  rvfi.rd_addr := rd_addr
  rvfi.rd_wdata := rd_wdata.asSInt()

  rvfi.mode := rvfi_mode
  rvfi.valid := rvfi_valid
  rvfi.order := rvfi_order
  rvfi.insn := insn
  rvfi.halt := rvfi_halt
  rvfi.intr := rvfi_intr
  rvfi.ixl  := 1.U
  rvfi.trap := false.B
}
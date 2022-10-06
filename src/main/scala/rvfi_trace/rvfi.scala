package rvfi_trace

import chisel3._

class RVFI_IO(RVFI:Boolean, XLEN:Int, NRET:Int, ILEN:Int) extends Bundle {
  // Input ports

  // - Instruction metadata
  val mem_reg_ins = if (RVFI) Some(Input(UInt(XLEN.W))) else None

  // - Register read/write
  val id_reg_rd1  = if (RVFI) Some(Input(SInt(XLEN.W))) else None
  val id_reg_rd2  = if (RVFI) Some(Input(SInt(XLEN.W))) else None
  val wb_rd       = if (RVFI) Some(Input(UInt(5.W))) else None
  val rs1_addr    = if (RVFI) Some(Input(UInt(5.W))) else None
  val rs2_addr    = if (RVFI) Some(Input(UInt(5.W))) else None
  val wb_data     = if (RVFI) Some(Input(SInt(XLEN.W))) else None
  val writeEnable = if (RVFI) Some(Input(Bool())) else None

  // - Program Counter
  val mem_reg_pc = if (RVFI) Some(Input(UInt(XLEN.W))) else None
  val nextPC     = if (RVFI) Some(Input(UInt(XLEN.W))) else None

  // - Memory Access
  val ex_reg_result = if (RVFI) Some(Input(UInt(XLEN.W))) else None
  val readEnable    = if (RVFI) Some(Input(Bool())) else None
  val memWriteEnable = if (RVFI) Some(Input(Bool())) else None
  val ex_reg_wd     = if (RVFI) Some(Input(SInt(XLEN.W))) else None
  val readData      = if (RVFI) Some(Input(SInt(XLEN.W))) else None

  // Output ports

  // - Instruction metadata
  //val rvfi_valid = if (RVFI) Some(Output(UInt(NRET.W))) else None
  //val rvfi_order = if (RVFI) Some(Output(UInt((NRET * 64).W))) else None
  val rvfi_insn  = if (RVFI) Some(Output(UInt((NRET * ILEN).W))) else None
  //val rvfi_trap  = if (RVFI) Some(Output(UInt(NRET.W))) else None
  //val rvfi_halt  = if (RVFI) Some(Output(UInt(NRET.W))) else None
  //val rvfi_intr  = if (RVFI) Some(Output(UInt(NRET.W))) else None
  val rvfi_mode  = if (RVFI) Some(Output(UInt((NRET * 2).W))) else None
  //val rvfi_ixl   = if (RVFI) Some(Output(UInt((NRET * 2).W))) else None

  // - Register read/write
  val rvfi_rs1_addr  = if (RVFI) Some(Output(UInt((NRET * 5).W))) else None
  val rvfi_rs2_addr  = if (RVFI) Some(Output(UInt((NRET * 5).W))) else None
  val rvfi_rs1_rdata = if (RVFI) Some(Output(SInt((NRET * XLEN).W))) else None
  val rvfi_rs2_rdata = if (RVFI) Some(Output(SInt((NRET * XLEN).W))) else None
  val rvfi_rd_addr   = if (RVFI) Some(Output(UInt((NRET * 5).W))) else None
  val rvfi_rd_wdata  = if (RVFI) Some(Output(SInt((NRET * XLEN).W))) else None

  // - Program Counter
  val rvfi_pc_rdata = if (RVFI) Some(Output(UInt((NRET * XLEN).W))) else None
  val rvfi_pc_wdata = if (RVFI) Some(Output(UInt((NRET * XLEN).W))) else None

  // - Memory Access
  val rvfi_mem_addr  = if (RVFI) Some(Output(UInt((NRET * XLEN).W))) else None
  //val rvfi_mem_rmask = if (RVFI) Some(Output(UInt((NRET * XLEN / 8).W)) else None
  //val rvfi_mem_wmask = if (RVFI) Some(Output(UInt((NRET * XLEN / 8).W)) else None
  val rvfi_mem_rdata = if (RVFI) Some(Output(SInt((NRET * XLEN).W))) else None
  val rvfi_mem_wdata = if (RVFI) Some(Output(SInt((NRET * XLEN).W))) else None
}

class RVFIUnit(RVFI:Boolean=false, XLEN:Int=32, NRET:Int=1, ILEN:Int=32) extends Module {
  // Initializing IO ports
  val io: RVFI_IO    = IO(new RVFI_IO(RVFI, XLEN, NRET, ILEN))
  val mem_reg_ins    = if (RVFI) Some(dontTouch(WireInit(io.mem_reg_ins.get))) else None
  val id_reg_rd1     = if (RVFI) Some(dontTouch(WireInit(io.id_reg_rd1.get))) else None
  val id_reg_rd2     = if (RVFI) Some(dontTouch(WireInit(io.id_reg_rd2.get))) else None
  val wb_rd          = if (RVFI) Some(dontTouch(WireInit(io.wb_rd.get))) else None
  val rs1_addr       = if (RVFI) Some(dontTouch(WireInit(io.rs1_addr.get))) else None
  val rs2_addr       = if (RVFI) Some(dontTouch(WireInit(io.rs2_addr.get))) else None
  val wb_data        = if (RVFI) Some(dontTouch(WireInit(io.wb_data.get))) else None
  val writeEnable    = if (RVFI) Some(dontTouch(WireInit(io.writeEnable.get))) else None
  val mem_reg_pc     = if (RVFI) Some(dontTouch(WireInit(io.mem_reg_pc.get))) else None
  val nextPC         = if (RVFI) Some(dontTouch(WireInit(io.nextPC.get))) else None
  val ex_reg_result  = if (RVFI) Some(dontTouch(WireInit(io.ex_reg_result.get))) else None
  val readEnable     = if (RVFI) Some(dontTouch(WireInit(io.readEnable.get))) else None
  val memWriteEnable = if (RVFI) Some(dontTouch(WireInit(io.memWriteEnable.get))) else None
  val ex_reg_wd      = if (RVFI) Some(dontTouch(WireInit(io.ex_reg_wd.get))) else None
  val readData       = if (RVFI) Some(dontTouch(WireInit(io.readData.get))) else None

  // Delay Registers
  //
  // - Instruction metadata
  //
  // - Register read/write
  val ex_reg_rd1      = if (RVFI) Some(dontTouch(RegInit(0.S(XLEN.W)))) else None
  val ex_reg_rd2      = if (RVFI) Some(dontTouch(RegInit(0.S(XLEN.W)))) else None
  val mem_reg_rd1     = if (RVFI) Some(dontTouch(RegInit(0.S(XLEN.W)))) else None
  val mem_reg_rd2     = if (RVFI) Some(dontTouch(RegInit(0.S(XLEN.W)))) else None
  val id_reg_rs1Addr  = if (RVFI) Some(dontTouch(RegInit(0.U(5.W)))) else None
  val id_reg_rs2Addr  = if (RVFI) Some(dontTouch(RegInit(0.U(5.W)))) else None
  val ex_reg_rs1Addr  = if (RVFI) Some(dontTouch(RegInit(0.U(5.W)))) else None
  val ex_reg_rs2Addr  = if (RVFI) Some(dontTouch(RegInit(0.U(5.W)))) else None
  val mem_reg_rs2Addr = if (RVFI) Some(dontTouch(RegInit(0.U(5.W)))) else None
  val mem_reg_rs1Addr = if (RVFI) Some(dontTouch(RegInit(0.U(5.W)))) else None

  // - Program Counter
  val if_reg_nPC  = if (RVFI) Some(dontTouch(RegInit(0.U(XLEN.W)))) else None
  val id_reg_nPC  = if (RVFI) Some(dontTouch(RegInit(0.U(XLEN.W)))) else None
  val ex_reg_nPC  = if (RVFI) Some(dontTouch(RegInit(0.U(XLEN.W)))) else None
  val mem_reg_nPC = if (RVFI) Some(dontTouch(RegInit(0.U(XLEN.W)))) else None

  // - Memory Access
  val mem_reg_result      = if (RVFI) Some(dontTouch(RegInit(0.U(XLEN.W)))) else None
  //val mem_reg_readData    = if (RVFI) Some(RegInit(0.U(XLEN.W))) else None
  val mem_reg_wd          = if (RVFI) Some(dontTouch(RegInit(0.S(XLEN.W)))) else None
  val mem_reg_readEnable  = if (RVFI) Some(dontTouch(RegInit(0.B))) else None
  val mem_reg_writeEnable = if (RVFI) Some(dontTouch(RegInit(0.B))) else None

  // Intermediate wires
  // - Instruction metadata
  //val rvfi_valid = if (RVFI) Some(Output(UInt(NRET.W))) else None
  //val rvfi_order = if (RVFI) Some(Output(UInt((NRET * 64).W))) else None
  val rvfi_insn = if (RVFI) Some(dontTouch(WireInit(mem_reg_ins.get))) else None
  //val rvfi_trap  = if (RVFI) Some(Output(UInt(NRET.W))) else None
  //val rvfi_halt  = if (RVFI) Some(Output(UInt(NRET.W))) else None
  //val rvfi_intr  = if (RVFI) Some(Output(UInt(NRET.W))) else None
  //val rvfi_mode  = if (RVFI) Some(Output(UInt((NRET * 2).W))) else None
  //val rvfi_ixl   = if (RVFI) Some(Output(UInt((NRET * 2).W))) else None

  // - Register read/write
  val rvfi_rs1_addr  = if (RVFI) Some(dontTouch(WireInit(mem_reg_rs1Addr.get))) else None
  val rvfi_rs2_addr  = if (RVFI) Some(dontTouch(WireInit(mem_reg_rs2Addr.get))) else None
  val rvfi_rs1_rdata = if (RVFI) Some(dontTouch(WireInit(mem_reg_rd1.get))) else None
  val rvfi_rs2_rdata = if (RVFI) Some(dontTouch(WireInit(mem_reg_rd2.get))) else None
  val rvfi_rd_addr   = if (RVFI) Some(dontTouch(WireInit(wb_rd.get))) else None
  val rvfi_rd_wdata  = if (RVFI) Some(dontTouch(WireInit(wb_data.get))) else None

  // - Program Counter
  val rvfi_pc_rdata = if (RVFI) Some(dontTouch(WireInit(mem_reg_pc.get))) else None
  val rvfi_pc_wdata = if (RVFI) Some(dontTouch(WireInit(mem_reg_nPC.get))) else None

  // - Memory Access
  val rvfi_mem_addr  = if (RVFI) Some(dontTouch(WireInit(mem_reg_result.get))) else None
  //val rvfi_mem_rmask = if (RVFI) Some(Output(UInt((NRET * XLEN / 8).W)) else None
  //val rvfi_mem_wmask = if (RVFI) Some(Output(UInt((NRET * XLEN / 8).W)) else None
  val rvfi_mem_rdata = if (RVFI) Some(dontTouch(WireInit(readData.get))) else None
  val rvfi_mem_wdata = if (RVFI) Some(dontTouch(WireInit(mem_reg_wd.get))) else None


  // Wiring to output ports
  if (RVFI) io.rvfi_mode.get := 3.U else None
  if (RVFI) Seq(
    // RVFI output ports
    //
    // - Instruction metadata
    io.rvfi_insn,

    // - Register read
    io.rvfi_rs1_addr, io.rvfi_rs2_addr, io.rvfi_rs1_rdata, io.rvfi_rs2_rdata,

    // - Program Counter
    io.rvfi_pc_rdata, io.rvfi_pc_wdata,

    // Delay Registers
    //
    // - Instruction metadata
    //
    // - Register read
    ex_reg_rd1,     ex_reg_rd2,     mem_reg_rd1,    mem_reg_rd2,     id_reg_rs1Addr,
    id_reg_rs2Addr, ex_reg_rs1Addr, ex_reg_rs2Addr, mem_reg_rs1Addr, mem_reg_rs2Addr,

    // - Program Counter
    if_reg_nPC, id_reg_nPC, ex_reg_nPC, mem_reg_nPC,

    // - Memory Access
    mem_reg_result, mem_reg_wd, mem_reg_readEnable, mem_reg_writeEnable

  ) zip Seq(
    // RVFI output ports
    //
    // - Instruction metadata
    rvfi_insn, 

    // - Register read
    rvfi_rs1_addr, rvfi_rs2_addr, rvfi_rs1_rdata, rvfi_rs2_rdata,

    // - Program Counter
    rvfi_pc_rdata, rvfi_pc_wdata,

    // Delay Registers
    //
    // - Instruction metadata
    //
    // - Register read
    id_reg_rd1, id_reg_rd2,     ex_reg_rd1,     ex_reg_rd2,     rs1_addr,
    rs2_addr,   id_reg_rs1Addr, id_reg_rs2Addr, ex_reg_rs1Addr, ex_reg_rs2Addr,

    // - Program Counter
    nextPC, if_reg_nPC, id_reg_nPC, ex_reg_nPC,

    // - Memory Access
    ex_reg_result, ex_reg_wd, readEnable, memWriteEnable

  ) foreach {
    x => x._1.get := x._2.get
  } else None

  if (RVFI) Seq(
    // - Register write
    io.rvfi_rd_addr,
    io.rvfi_rd_wdata,

    // - Memory Access
    io.rvfi_mem_addr,
    io.rvfi_mem_rdata,
    io.rvfi_mem_wdata

  ) zip Seq(
    // - Register write
    (writeEnable.get, rvfi_rd_addr,  0.U),
    (writeEnable.get, rvfi_rd_wdata, 0.S),

    // - Memory Access
    (mem_reg_readEnable.get || mem_reg_writeEnable.get, rvfi_mem_addr,  0.U),
    (mem_reg_readEnable.get,                            rvfi_mem_rdata, 0.S),
    (mem_reg_writeEnable.get,                           rvfi_mem_wdata, 0.S)

  ) foreach {
    x => x._1.get := Mux(x._2._1, x._2._2.get, x._2._3)
  } else None

  val clkCycle = if (RVFI) Some(RegInit(0.U(32.W))) else None
  if (RVFI) clkCycle.get := clkCycle.get + 1.U else None
  if (RVFI) printf(
        "ClkCycle: %d, pc_rdata: %x, pc_wdata: %x, insn: %x, mode: %d, rs1_addr: %d, rs1_rdata: %x, rs2_addr: %d, rs2_rdata: %x, rd_addr: %d, rd_wdata: %x, mem_addr: %x, mem_rdata: %x, mem_wdata: %x\n",
        clkCycle.get,                    io.rvfi_pc_rdata.get,  io.rvfi_pc_wdata.get,  io.rvfi_insn.get,      io.rvfi_mode.get,
        io.rvfi_rs1_addr.get, io.rvfi_rs1_rdata.get, io.rvfi_rs2_addr.get,  io.rvfi_rs2_rdata.get, io.rvfi_rd_addr.get,
        io.rvfi_rd_wdata.get, io.rvfi_mem_addr.get,  io.rvfi_mem_rdata.get, io.rvfi_mem_wdata.get
  ) else None
}

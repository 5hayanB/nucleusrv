
package nucleusrv.components
import chisel3._
import chisel3.util._
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}
import components.{RVFI, RVFIPORT}

class Core(val req:AbstrRequest, val rsp:AbstrResponse)(M:Boolean = false)(implicit val config:BusConfig) extends MultiIOModule {
    val pin: UInt = Output(UInt(32.W))

    val dmemReq = IO(Decoupled(req))
    val dmemRsp = IO(Flipped(Decoupled(rsp)))

    val imemReq = IO(Decoupled(req))
    val imemRsp = IO(Flipped(Decoupled(rsp)))

    val rvfi = IO(new RVFIPORT)


  // IF-ID Registers
  val if_reg_pc = RegInit(0.U(32.W))
  val if_reg_ins = RegInit(0.U(32.W))

  // ID-EX Registers
  val id_reg_pc = RegInit(0.U(32.W))
  val id_reg_rd1 = RegInit(0.U(32.W))
  val id_reg_rd2 = RegInit(0.U(32.W))
  val id_reg_imm = RegInit(0.U(32.W))
  val id_reg_wra = RegInit(0.U(5.W))
  val id_reg_f7 = RegInit(0.U(7.W))
  val id_reg_f3 = RegInit(0.U(3.W))
  val id_reg_ins = RegInit(0.U(32.W))
  val id_reg_ctl_aluSrc = RegInit(false.B)
  val id_reg_ctl_aluSrc1 = RegInit(0.U(2.W))
  val id_reg_ctl_memToReg = RegInit(0.U(2.W))
  val id_reg_ctl_regWrite = RegInit(false.B)
  val id_reg_ctl_memRead = RegInit(false.B)
  val id_reg_ctl_memWrite = RegInit(false.B)
  val id_reg_ctl_branch = RegInit(false.B)
  val id_reg_ctl_aluOp = RegInit(0.U(2.W))
  val id_reg_ctl_jump = RegInit(0.U(2.W))

  // EX-MEM Registers
  val ex_reg_branch = RegInit(0.U(32.W))
  val ex_reg_zero = RegInit(0.U(32.W))
  val ex_reg_result = RegInit(0.U(32.W))
  val ex_reg_wd = RegInit(0.U(32.W))
  val ex_reg_wra = RegInit(0.U(5.W))
  val ex_reg_ins = RegInit(0.U(32.W))
  val ex_reg_ctl_memToReg = RegInit(0.U(2.W))
  val ex_reg_ctl_regWrite = RegInit(false.B)
  val ex_reg_ctl_memRead = RegInit(false.B)
  val ex_reg_ctl_memWrite = RegInit(false.B)
  val ex_reg_ctl_branch_taken = RegInit(false.B)
  val ex_reg_pc = RegInit(0.U(32.W))

  // MEM-WB Registers
  val mem_reg_rd = RegInit(0.U(32.W))
  val mem_reg_ins = RegInit(0.U(32.W))
  val mem_reg_result = RegInit(0.U(32.W))
  val mem_reg_branch = RegInit(0.U(32.W))
  val mem_reg_wra = RegInit(0.U(5.W))
  val mem_reg_ctl_memToReg = RegInit(0.U(2.W))
  val mem_reg_ctl_regWrite = RegInit(false.B)
  val mem_reg_pc = RegInit(0.U(32.W))

  //Pipeline Units
  val IF = Module(new InstructionFetch(req, rsp))
  val ID = Module(new InstructionDecode)
  val EX = Module(new Execute(M))
  val MEM = Module(new MemoryFetch(req,rsp))

  /*****************
   * Fetch Stage *
   ******************/

  val pc = Module(new PC)
  
  imemReq <> IF.coreInstrReq
  IF.coreInstrResp <> imemRsp

  IF.address := pc.in.asUInt()
  val instruction = Mux(imemRsp.valid, IF.instruction, "h00000013".U(32.W))

  pc.halt := Mux(imemReq.valid, 0.B, 1.B)
  pc.in := Mux(ID.hdu_pcWrite && !MEM.stall, Mux(ID.pcSrc, ID.pcPlusOffset.asSInt(), pc.pc4), pc.out)


  when(ID.hdu_if_reg_write && !MEM.stall) {
    if_reg_pc := pc.out.asUInt()
    if_reg_ins := instruction
  }
  when(ID.ifid_flush) {
    if_reg_ins := 0.U
  }


  /****************
   * Decode Stage *
   ****************/

  id_reg_rd1 := ID.readData1
  id_reg_rd2 := ID.readData2
  id_reg_imm := ID.immediate
  id_reg_wra := ID.writeRegAddress
  id_reg_f3 := ID.func3
  id_reg_f7 := ID.func7
  id_reg_ins := if_reg_ins
  id_reg_pc := if_reg_pc
  id_reg_ctl_aluSrc := ID.ctl_aluSrc
  id_reg_ctl_memToReg := ID.ctl_memToReg
  id_reg_ctl_regWrite := ID.ctl_regWrite
  id_reg_ctl_memRead := ID.ctl_memRead
  id_reg_ctl_memWrite := ID.ctl_memWrite
  id_reg_ctl_branch := ID.ctl_branch
  id_reg_ctl_aluOp := ID.ctl_aluOp
  id_reg_ctl_jump := ID.ctl_jump
  id_reg_ctl_aluSrc1 := ID.ctl_aluSrc1
//  IF.PcWrite := ID.hdu_pcWrite
  ID.id_instruction := if_reg_ins
  ID.pcAddress := if_reg_pc
  ID.dmem_resp_valid := dmemRsp.valid
//  IF.PcSrc := ID.pcSrc
//  IF.PCPlusOffset := ID.pcPlusOffset
  ID.ex_ins := id_reg_ins
  ID.ex_mem_ins := ex_reg_ins
  ID.mem_wb_ins := mem_reg_ins
  ID.ex_mem_result := ex_reg_result


  /*****************
   * Execute Stage *
  ******************/

  //ex_reg_branch := EX.branchAddress
  ex_reg_wd := EX.writeData
  ex_reg_result := EX.ALUresult
  //ex_reg_ctl_branch_taken := EX.branch_taken
  EX.immediate := id_reg_imm
  EX.readData1 := id_reg_rd1
  EX.readData2 := id_reg_rd2
  EX.pcAddress := id_reg_pc
  EX.func3 := id_reg_f3
  EX.func7 := id_reg_f7
  EX.ctl_aluSrc := id_reg_ctl_aluSrc
  EX.ctl_aluOp := id_reg_ctl_aluOp
  EX.ctl_aluSrc1 := id_reg_ctl_aluSrc1
  //EX.ctl_branch := id_reg_ctl_branch
  //EX.ctl_jump := id_reg_ctl_jump
  ex_reg_pc := id_reg_pc
  ex_reg_wra := id_reg_wra
  ex_reg_ins := id_reg_ins
  ex_reg_ctl_memToReg := id_reg_ctl_memToReg
  ex_reg_ctl_regWrite := id_reg_ctl_regWrite
//  ex_reg_ctl_memRead := id_reg_ctl_memRead
//  ex_reg_ctl_memWrite := id_reg_ctl_memWrite
  ID.id_ex_mem_read := id_reg_ctl_memRead
  ID.ex_mem_mem_read := ex_reg_ctl_memRead
//  ID.ex_mem_mem_write := ex_reg_ctl_memWrite
  //EX.ex_mem_regWrite := ex_reg_ctl_regWrite
  //EX.mem_wb_regWrite := mem_reg_ctl_regWrite
  EX.id_ex_ins := id_reg_ins
  EX.ex_mem_ins := ex_reg_ins
  EX.mem_wb_ins := mem_reg_ins
  ID.id_ex_rd := id_reg_ins(11, 7)
  ID.id_ex_branch := Mux(id_reg_ins(6,0) === "b1100011".asUInt(), true.B, false.B )
  ID.ex_mem_rd := ex_reg_ins(11, 7)
  ID.ex_result := EX.ALUresult


  /****************
   * Memory Stage *
   ****************/

  dmemReq <> MEM.dccmReq
  MEM.dccmRsp <> dmemRsp
//  val stall = Wire(Bool())
//  stall := (ex_reg_ctl_memWrite || ex_reg_ctl_memRead) && !dmemRsp.valid
  when(MEM.stall){
    mem_reg_rd := mem_reg_rd
    mem_reg_result := mem_reg_result
//    mem_reg_wra := mem_reg_wra
    ex_reg_wra := ex_reg_wra
    ex_reg_ctl_memToReg := ex_reg_ctl_memToReg
//    mem_reg_ctl_memToReg := mem_reg_ctl_memToReg
    ex_reg_ctl_regWrite := ex_reg_ctl_regWrite
    mem_reg_ctl_regWrite := ex_reg_ctl_regWrite
    mem_reg_ins := mem_reg_ins
    mem_reg_pc := mem_reg_pc

    ex_reg_ctl_memRead := ex_reg_ctl_memRead
    ex_reg_ctl_memWrite := ex_reg_ctl_memWrite

  } otherwise{
    mem_reg_rd := MEM.readData
    mem_reg_result := ex_reg_result
//    mem_reg_ctl_memToReg := ex_reg_ctl_memToReg
    mem_reg_ctl_regWrite := ex_reg_ctl_regWrite
    mem_reg_ins := ex_reg_ins
    mem_reg_pc := ex_reg_pc
    mem_reg_wra := ex_reg_wra
    ex_reg_ctl_memRead := id_reg_ctl_memRead
    ex_reg_ctl_memWrite := id_reg_ctl_memWrite
  }
  mem_reg_wra := ex_reg_wra
  mem_reg_ctl_memToReg := ex_reg_ctl_memToReg
  EX.ex_mem_regWrite := ex_reg_ctl_regWrite
  MEM.aluResultIn := ex_reg_result
  MEM.writeData := ex_reg_wd
  MEM.readEnable := ex_reg_ctl_memRead
  MEM.writeEnable := ex_reg_ctl_memWrite
  EX.mem_result := ex_reg_result

  /********************
   * Write Back Stage *
   ********************/

  val wb_data = Wire(UInt(32.W))
  val wb_addr = Wire(UInt(5.W))

  when(mem_reg_ctl_memToReg === 1.U) {
    wb_data := MEM.readData
    wb_addr := Mux(dmemRsp.valid, mem_reg_wra, 0.U)
  }.elsewhen(mem_reg_ctl_memToReg === 2.U) {
      wb_data := mem_reg_pc
      wb_addr := mem_reg_wra
    }
    .otherwise {
      wb_data := mem_reg_result
      wb_addr := mem_reg_wra
    }

  ID.mem_wb_result := wb_data
  ID.writeData := wb_data
  EX.wb_result := wb_data
  EX.mem_wb_regWrite := mem_reg_ctl_regWrite
  ID.writeReg := wb_addr
  ID.ctl_writeEnable := mem_reg_ctl_regWrite
  pin := wb_data



  val rvfi = Module(new RVFI)
  rvfi.stall := MEM.stall
  rvfi.pc := pc.out
  rvfi.pc_src := ID.pcSrc
  rvfi.pc_four := pc.pc4
  rvfi.pc_offset := pc.in
  rvfi.rd_wdata := wb_data
  rvfi.rd_addr := wb_addr
  rvfi.rs1_rdata := ID.readData1
  rvfi.rs2_rdata := ID.readData2
  rvfi.insn := if_reg_ins

  rvfi <> rvfi.rvfi


}


package nucleusrv.components
import chisel3._

class InstructionDecode extends Module {

    val id_instruction = IO(Input(UInt(32.W)))
    val writeData = IO(Input(UInt(32.W)))
    val writeReg = IO(Input(UInt(5.W)))
    val pcAddress = IO(Input(UInt(32.W)))
    val ctl_writeEnable = IO(Input(Bool()))
    val id_ex_mem_read = IO(Input(Bool()))
//    val ex_mem_mem_write = Input(Bool())
    val ex_mem_mem_read =IO( Input(Bool()))
    val dmem_resp_valid = IO(Input(Bool()))
    val id_ex_rd = IO(Input(UInt(5.W)))
    val ex_mem_rd = IO(Input(UInt(5.W)))
    val id_ex_branch = IO(Input(Bool()))
    //for forwarding
    val ex_mem_ins = IO(Input(UInt(32.W)))
    val mem_wb_ins = IO(Input(UInt(32.W)))
    val ex_ins = IO(Input(UInt(32.W)))
    val ex_result = IO(Input(UInt(32.W)))
    val ex_mem_result = IO(Input(UInt(32.W)))
    val mem_wb_result = IO(Input(UInt(32.W)))
    
    //Outputs
    val immediate = IO(Output(UInt(32.W)))
    val writeRegAddress = IO(Output(UInt(5.W)))
    val readData1 = IO(Output(UInt(32.W)))
    val readData2 = IO(Output(UInt(32.W)))
    val func7 = IO(Output(UInt(7.W)))
    val func3 = IO(Output(UInt(3.W)))
    val ctl_aluSrc = IO(Output(Bool()))
    val ctl_memToReg = IO(Output(UInt(2.W)))
    val ctl_regWrite =IO( Output(Bool()))
    val ctl_memRead = IO(Output(Bool()))
    val ctl_memWrite = IO(Output(Bool()))
    val ctl_branch = IO(Output(Bool()))
    val ctl_aluOp = IO(Output(UInt(2.W)))
    val ctl_jump = IO(Output(UInt(2.W)))
    val ctl_aluSrc1 =IO( Output(UInt(2.W)))
    val hdu_pcWrite = IO(Output(Bool()))
    val hdu_if_reg_write =IO( Output(Bool()))
    val pcSrc = IO(Output(Bool()))
    val pcPlusOffset = IO(Output(UInt(32.W)))
    val ifid_flush = IO(Output(Bool()))

  //Hazard Detection Unit
  val hdu = Module(new HazardUnit)
  hdu.dmem_resp_valid := dmem_resp_valid
  hdu.id_ex_memRead := id_ex_mem_read
//  hdu.ex_mem_memWrite := ex_mem_mem_write
  hdu.ex_mem_memRead := ex_mem_mem_read
  hdu.id_ex_rd := id_ex_rd
  hdu.id_ex_branch := id_ex_branch
  hdu.ex_mem_rd := ex_mem_rd
  hdu.id_rs1 := id_instruction(19, 15)
  hdu.id_rs2 := id_instruction(24, 20)
  hdu.jump := ctl_jump
  hdu.branch := ctl_branch
  hdu_pcWrite := hdu.pc_write
  hdu_if_reg_write := hdu.if_reg_write

  //Control Unit
  val control = Module(new Control)
  control.in := id_instruction
  ctl_aluOp := control.aluOp
  ctl_aluSrc := control.aluSrc
  ctl_aluSrc1 := control.aluSrc1
  ctl_branch := control.branch
  ctl_memRead := control.memRead
  ctl_memToReg := control.memToReg
  ctl_jump := control.jump
  when(hdu.ctl_mux && id_instruction =/= "h13".U) {
    ctl_memWrite := control.memWrite
    ctl_regWrite := control.regWrite

  }.otherwise {
    ctl_memWrite := false.B
    ctl_regWrite := false.B
  }

  //Register File
  val registers = Module(new Registers)
  val registerRd = writeReg
  val registerRs1 = id_instruction(19, 15)
  val registerRs2 = id_instruction(24, 20)
  registers.readAddress(0) := registerRs1
  registers.readAddress(1) := registerRs2
  registers.writeEnable := ctl_writeEnable
  registers.writeAddress := registerRd
  registers.writeData := writeData

  //Forwarding to fix structural hazard
  when(ctl_writeEnable && (writeReg === registerRs1)){
    when(registerRs1 === 0.U){
      readData1 := 0.U
    }.otherwise{
      readData1 := writeData
    }
  }.otherwise{
    readData1 := registers.readData(0)
  }
  when(ctl_writeEnable && (writeReg === registerRs2)){
    when(registerRs2 === 0.U){
      readData2 := 0.U
    }.otherwise{
      readData2 := writeData
    }
  }.otherwise{
    readData2 := registers.readData(1)
  }
  

  val immediate = Module(new ImmediateGen)
  immediate.instruction := id_instruction
  immediate := immediate.out

  // Branch Forwarding
  val input1 = Wire(UInt(32.W))
  val input2 = Wire(UInt(32.W))

  when(registerRs1 === ex_mem_ins(11, 7)) {
    input1 := ex_mem_result
  }.elsewhen(registerRs1 === mem_wb_ins(11, 7)) {
      input1 := mem_wb_result
    }
    .otherwise {
      input1 := readData1
    }
  when(registerRs2 === ex_mem_ins(11, 7)) {
    input2 := ex_mem_result
  }.elsewhen(registerRs2 === mem_wb_ins(11, 7)) {
      input2 := mem_wb_result
    }
    .otherwise {
      input2 := readData2
    }

  //Branch Unit
  val bu = Module(new BranchUnit)
  bu.branch := ctl_branch
  bu.funct3 := id_instruction(14, 12)
  bu.rd1 := input1
  bu.rd2 := input2
  bu.take_branch := hdu.take_branch
  hdu.taken := bu.taken  

  //Forwarding for Jump
  val j_offset = Wire(UInt(32.W))
    when(registerRs1 === ex_ins(11, 7)){
      j_offset := ex_result
    }.elsewhen(registerRs1 === ex_mem_ins(11, 7)) {
    j_offset := ex_mem_result
  }.elsewhen(registerRs1 === mem_wb_ins(11, 7)) {
    j_offset := mem_wb_result
  }.elsewhen(registerRs1 === ex_ins(11, 7)){
    j_offset := ex_result
  }.otherwise {
      j_offset := readData1
    }

  //Offset Calculation (Jump/Branch)
  when(ctl_jump === 1.U) {
    pcPlusOffset := pcAddress + immediate
  }.elsewhen(ctl_jump === 2.U) {
      pcPlusOffset := j_offset + immediate
    }
    .otherwise {
      pcPlusOffset := pcAddress + immediate.out
    }

  when(bu.taken || ctl_jump =/= 0.U) {
    pcSrc := true.B
  }.otherwise {
    pcSrc := false.B
  }

  //Instruction Flush
  ifid_flush := hdu.ifid_flush

  writeRegAddress := id_instruction(11, 7)
  func3 := id_instruction(14, 12)
  func7 := id_instruction(31,25)
}

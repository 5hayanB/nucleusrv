
package nucleusrv.components
import chisel3._
import chisel3.util.MuxCase

class Execute(M:Boolean = true) extends MultiIOModule {
    val immediate = IO(Input(UInt(32.W)))
    val readData1 = IO(Input(UInt(32.W)))
    val readData2 = IO(Input(UInt(32.W)))
    val pcAddress = IO(Input(UInt(32.W)))
    val func7 = IO(Input(UInt(7.W)))
    val func3 = IO(Input(UInt(3.W)))
    val mem_result = IO(Input(UInt(32.W)))
    val wb_result = IO(Input(UInt(32.W)))

    val ex_mem_regWrite = IO(Input(Bool()))
    val mem_wb_regWrite = IO(Input(Bool()))
    val id_ex_ins = IO(Input(UInt(32.W)))
    val ex_mem_ins = IO(Input(UInt(32.W)))
    val mem_wb_ins = IO(Input(UInt(32.W)))

    val ctl_aluSrc = IO(Input(Bool()))
    val ctl_aluOp = IO(Input(UInt(2.W)))
    val ctl_aluSrc1 = IO(Input(UInt(2.W)))

    val writeData = IO(Output(UInt(32.W)))
    val ALUresult = IO(Output(UInt(32.W))
)
  val alu = Module(new ALU)
  val aluCtl = Module(new AluControl)
  val fu = Module(new ForwardingUnit)

  // Forwarding Unt

  fu.ex_regWrite := ex_mem_regWrite
  fu.mem_regWrite := mem_wb_regWrite
  fu.ex_reg_rd := ex_mem_ins(11, 7)
  fu.mem_reg_rd := mem_wb_ins(11, 7)
  fu.reg_rs1 := id_ex_ins(19, 15)
  fu.reg_rs2 := id_ex_ins(24, 20)

  val inputMux1 = MuxCase(
    0.U,
    Array(
      (fu.forwardA === 0.U) -> (readData1),
      (fu.forwardA === 1.U) -> (mem_result),
      (fu.forwardA === 2.U) -> (wb_result)
    )
  )
  val inputMux2 = MuxCase(
    0.U,
    Array(
      (fu.forwardB === 0.U) -> (readData2),
      (fu.forwardB === 1.U) -> (mem_result),
      (fu.forwardB === 2.U) -> (wb_result)
    )
  )

  val aluIn1 = MuxCase(
    inputMux1,
    Array(
      (ctl_aluSrc1 === 1.U) -> pcAddress,
      (ctl_aluSrc1 === 2.U) -> 0.U
    )
  )
  val aluIn2 = Mux(ctl_aluSrc, inputMux2, immediate)

  aluCtl.f3 := func3
  aluCtl.f7 := func7(5)
  aluCtl.aluOp := ctl_aluOp
  aluCtl.aluSrc := ctl_aluSrc

  alu.input1 := aluIn1
  alu.input2 := aluIn2
  alu.aluCtl := aluCtl.out

  if(M){
    val mdu = Module (new MDU)
    val mduCtl = Module(new MduControl)

    mduCtl.f3 := func3
    mduCtl.f7 := func7
    mduCtl.aluOp := ctl_aluOp
    mduCtl.aluSrc := ctl_aluSrc

    mdu.src_a := aluIn1.asSInt
    mdu.src_b := aluIn2.asSInt
    mdu.op := mduCtl.op
    mdu.valid := true.B

    when (func7 === 1.U && mdu.ready){ALUresult := (Mux(mdu.output.valid, mdu.output.bits, 0.S)).asUInt}
    .otherwise{ALUresult := alu.result}
  }else{ALUresult := alu.result}

  // ALUresult := alu.result

  writeData := inputMux2
}

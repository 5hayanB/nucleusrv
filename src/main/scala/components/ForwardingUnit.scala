
package nucleusrv.components
import chisel3._

class ForwardingUnit extends MultiIOModule {
    val ex_reg_rd = IO(Input(UInt(5.W)))
    val mem_reg_rd = IO(Input(UInt(5.W)))
    val reg_rs1 = IO(Input(UInt(5.W)))
    val reg_rs2 = IO(Input(UInt(5.W)))
    val ex_regWrite = IO(Input(Bool()))
    val mem_regWrite = IO(Input(Bool()))

    val forwardA = IO(Output(UInt(2.W)))
    val forwardB = IO(Output(UInt(2.W))
)
  forwardA := DontCare
  forwardB := DontCare
  
  when(reg_rs1 === ex_reg_rd && ex_reg_rd =/= 0.U && ex_regWrite) {
    forwardA := 1.U
  }.elsewhen(
      reg_rs1 === mem_reg_rd && mem_reg_rd =/= 0.U && mem_regWrite
    ) {
      forwardA := 2.U
    }
    .otherwise {
      forwardA := 0.U
    }

  when(reg_rs2 === ex_reg_rd && ex_reg_rd =/= 0.U && ex_regWrite) {
    forwardB := 1.U
  }.elsewhen(
      reg_rs2 === mem_reg_rd && mem_reg_rd =/= 0.U && mem_regWrite
    ) {
      forwardB := 2.U
    }
    .otherwise {
      forwardB := 0.U
    }
}

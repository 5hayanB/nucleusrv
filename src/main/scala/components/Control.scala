
package nucleusrv.components
import chisel3._
import chisel3.util._

class Control extends MultiIOModule {
    val in = IO(Input(UInt(32.W)))
    val aluSrc = IO(Output(Bool()))
    val memToReg = IO(Output(UInt(2.W)))
    val regWrite = IO(Output(Bool()))
    val memRead = IO(Output(Bool()))
    val memWrite = IO(Output(Bool()))
    val branch =IO( Output(Bool()))
    val aluOp = IO(Output(UInt(2.W)))
    val jump = IO(Output(UInt(2.W)))
    val aluSrc1 = IO(Output(UInt(2.W))
)
  val signals = ListLookup(
    in,
    /*   aluSrc  ToReg regWrite memRead  memWrite branch  jump  aluOp aluSrc1*/
    List(false.B, 0.U, false.B, false.B, false.B, false.B, 0.U, 0.U, 0.U),
    Array(
      // R-Type
      BitPat("b?????????????????????????0110011") -> List(
        true.B, // aluSrc
        0.U, // memToReg
        true.B, // regWrite
        false.B, // memRead
        false.B, // memWrite
        false.B, // branch
        0.U, // jump
        2.U, // aluOp
        0.U // aluSrc1
      ),
      // I-Type
      BitPat("b?????????????????????????0010011") -> List(
        false.B, // aluSrc
        0.U, // memToReg
        true.B, // regWrite
        false.B, // memRead
        false.B, // memWrite
        false.B, // branch
        0.U, // jump
        2.U, // aluOp
        0.U
      ),
      // Load
      BitPat("b?????????????????????????0000011") -> List(
        false.B, // aluSrc
        1.U, // memToReg
        true.B, // regWrite
        true.B, // memRead
        false.B, // memWrite
        false.B, // branch
        0.U, // jump
        0.U, // aluOp
        0.U
      ),
      // Store
      BitPat("b?????????????????????????0100011") -> List(
        false.B, // aluSrc
        0.U, // memToReg
        false.B, // regWrite
        false.B, // memRead
        true.B, // memWrite
        false.B, // branch
        0.U, // jump
        0.U, // aluOp
        0.U
      ),
      // SB-Type
      BitPat("b?????????????????????????1100011") -> List(
        true.B, // aluSrc
        0.U, // memToReg
        false.B, // regWrite
        false.B, // memRead
        false.B, // memWrite
        true.B, // branch
        0.U, // jump
        0.U, // aluOp
        0.U
      ),
      // lui
      BitPat("b?????????????????????????0110111") -> List(
        false.B, // aluSrc
        0.U, // memToReg
        true.B, // regWrite
        false.B, // memRead
        false.B, // memWrite
        false.B, // branch
        0.U, // jump
        0.U, // aluOp
        2.U  // aluSrc1
      ),
      // auipc
      BitPat("b?????????????????????????0010111") -> List(
        false.B, // aluSrc
        0.U, // memToReg
        true.B, // regWrite
        false.B, // memRead
        false.B, // memWrite
        false.B, // branch
        0.U, // jump
        0.U, // aluOp
        1.U  // aluSrc1
      ),
      // jal
      BitPat("b?????????????????????????1101111") -> List(
        false.B, // aluSrc
        2.U, // memToReg
        true.B, // regWrite
        false.B, // memRead
        false.B, // memWrite
        false.B, // branch
        1.U, // jump
        0.U, // aluOp
        0.U
      ),
      // jalr
      BitPat("b?????????????????????????1100111") -> List(
        false.B, // aluSrc
        2.U, // memToReg
        true.B, // regWrite
        false.B, // memRead
        false.B, // memWrite
        false.B, // branch
        2.U, // jump
        0.U, // aluOp
        0.U
      )
    )
  )
  aluSrc := signals(0)
  memToReg := signals(1)
  regWrite := signals(2)
  memRead := signals(3)
  memWrite := signals(4)
  branch := signals(5)
  jump := signals(6)
  aluOp := signals(7)
  aluSrc1 := signals(8)
}

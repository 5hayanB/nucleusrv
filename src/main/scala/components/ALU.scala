package nucleusrv.components
import chisel3._
import chisel3.util._

class ALU extends MultiIOModule {
    val input1 = IO(Input(UInt(32.W)))
    val input2 = IO(Input(UInt(32.W)))
    val aluCtl = IO(Input(UInt(4.W)))

    val zero = IO(Output(Bool()))
    val result = IO(Output(UInt(32.W)))
  result := MuxCase(
    0.U,
    Array(
      (aluCtl === 0.U) -> (input1 & input2),
      (aluCtl === 1.U) -> (input1 | input2),
      (aluCtl === 2.U) -> (input1 + input2),
      (aluCtl === 3.U) -> (input1 - input2),
      (aluCtl === 4.U) -> (input1.asSInt < input2.asSInt).asUInt,
      (aluCtl === 5.U) -> (input1 < input2),
      (aluCtl === 6.U) -> (input1 << input2(4, 0)),
      (aluCtl === 7.U) -> (input1 >> input2(4, 0)),
      (aluCtl === 8.U) -> (input1.asSInt >> input2(4, 0)).asUInt,
      (aluCtl === 9.U) -> (input1 ^ input2)
    )
  )
  zero := DontCare
}

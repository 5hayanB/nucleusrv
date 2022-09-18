package nucleusrv.components
import chisel3._
import chisel3.util._
import chisel3.experimental._

class MduControl extends MultiIOModule {
    val aluOp = IO(Input(UInt(2.W)))
    val f7 = IO(Input(UInt(7.W)))
    val f3 = IO(Input(UInt(3.W)))
    val aluSrc = IO(Input(Bool()))
    val op = IO(Output(UInt(4.W)))

  when(f7 === 1.U && (f3 === 0.U || f3 === 1.U || f3 === 2.U || f3 === 3.U || f3 === 4.U || f3 === 5.U || f3 === 6.U || f3 === 7.U)){
      op := f3
  }
  .otherwise{
      op := aluOp
  }
}

package nucleusrv.components
import chisel3._

class PC extends MultiIOModule{
    val in = IO(Input(SInt(32.W)))
    val halt = IO(Input(Bool()))
    val out = IO(Output(SInt(32.W)))
    val pc4 = IO(Output(SInt(32.W)))

  val pc_reg = RegInit((0x0-0x4).asSInt(32.W))
  pc_reg := in
  out := pc_reg
  pc4 := Mux(halt, pc_reg, pc_reg + 4.S)
}

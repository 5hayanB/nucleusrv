
package nucleusrv.components
import chisel3._
import chisel3.util.BitPat

class JumpUnit extends MultiIOModule {
    val func7 = IO(Input(UInt(7.W)))
    val jump = IO(Output(UInt(2.W)))

  when(func7 === BitPat("b1101111")) {
    jump := 2.U
  }.elsewhen(func7 === BitPat("b1100111")) {
      jump := 3.U
    }
    .otherwise {
      jump := 0.U
    }
}

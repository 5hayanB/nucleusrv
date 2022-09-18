
package nucleusrv.components
import chisel3._
import chisel3.util._

class BranchUnit extends MultiIOModule {
    val branch = IO(Input(Bool()))
    val funct3 = IO(Input(UInt(3.W)))
    val rd1 = IO(Input(UInt(32.W)))
    val rd2 = IO(Input(UInt(32.W)))
    val take_branch = IO(Input(Bool()))

    val taken: Bool = Output(Bool())

  taken := DontCare

  val check: Bool = Wire(Bool())
  check := DontCare

  switch(funct3) {
    is(0.U) { check := (rd1 === rd2) } // beq
    is(1.U) { check := (rd1 =/= rd2) } // bne
    is(4.U) { check := (rd1.asSInt < rd2.asSInt) } // blt
    is(5.U) { check := (rd1.asSInt >= rd2.asSInt) } // bge
    is(6.U) { check := (rd1 < rd2) } // bltu
    is(7.U) { check := (rd1 >= rd2) } // bgeu
  }

  taken := check & branch & take_branch

}

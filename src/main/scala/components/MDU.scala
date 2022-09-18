package nucleusrv.components
import chisel3._
import chisel3.util._
import chisel3.experimental._

class MDU(n:Int = 32) extends MultiIOModule{
        val src_a         = IO(Input(SInt(32.W)))
        val src_b         = IO(Input(SInt(32.W)))
        val op            = IO(Input(UInt(5.W)))
        val valid         = IO(Input(Bool()))
        val ready         = IO(Output(Bool()))
        
        val output        = IO(Valid(Output(SInt(32.W))))

    // Multiplier
    val cases = Array((op === 0.U || op === 1.U )     ->      src_a * src_b,
                    (op === 2.U)                         ->      src_a * (src_b.asUInt).asSInt,
                    (op === 3.U)                         ->      (src_a.asUInt * src_b.asUInt).asSInt)

    val out_wire = Wire(SInt(64.W))
    out_wire := MuxCase(0.S, cases)

    // Divider
    val r_ready    = RegInit(1.U(1.W))
    val r_counter  = RegInit(n.U(6.W))
    val r_dividend = RegInit(0.U(n.W))
    val r_quotient = RegInit(0.U(n.W))

    output.valid := 0.U

    // shift + substract
    when(op === 5.U || op === 7.U){
        val dividend  = WireInit(src_a.asUInt)
        val divisor   = WireInit(src_b.asUInt)
        when(valid === 1.U) {
            r_ready    := 0.U
            r_counter  := n.U
            r_dividend := dividend
            r_quotient := 0.U
        }.elsewhen(r_counter =/= 0.U){
            when(r_dividend >= (divisor<<(r_counter-1.U))){
            r_dividend    := r_dividend - (divisor<<(r_counter-1.U))
            r_quotient    := r_quotient + (1.U<<(r_counter-1.U))
            }.otherwise {r_ready := 1.U}
            r_counter  := r_counter - 1.U
            r_ready    := (r_counter === 1.U)
        }.otherwise{output.valid := 1.U}
    }

    ready     := r_ready
    when(op === 0.U){
        output.bits := out_wire(31,0).asSInt
        output.valid := 1.U
    }.elsewhen(op === 1.U && op === 1.U && op === 2.U && op === 3.U){
        output.bits := out_wire(63,32).asSInt
        output.valid := 1.U
    }.elsewhen(op === 5.U){
        output.bits := r_quotient.asSInt
    }.elsewhen(op === 7.U){
        output.bits := r_dividend.asSInt
    }.otherwise{
        output.bits := 0.S
    }
}
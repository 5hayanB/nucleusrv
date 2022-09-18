
package nucleusrv.components
import chisel3._
import chisel3.util._ 

import caravan.bus.common.{BusConfig, AbstrRequest, AbstrResponse}

class InstructionFetch(val req:AbstrRequest, val rsp:AbstrResponse)(implicit val config:BusConfig) extends MultiIOModule {
    val address = IO(Input(UInt(32.W)))
    val instruction = IO(Output(UInt(32.W)))

    val coreInstrReq = IO(Decoupled(req))
    val coreInstrResp = IO(Flipped(Decoupled(rsp)))

  coreInstrResp.ready := true.B

  coreInstrReq.bits.activeByteLane := "b1111".U
  coreInstrReq.bits.isWrite := false.B
  coreInstrReq.bits.dataRequest := DontCare

  coreInstrReq.bits.addrRequest := address
  coreInstrReq.valid := Mux(coreInstrReq.ready, true.B, false.B)

  instruction := Mux(coreInstrResp.valid, coreInstrResp.bits.dataResponse, DontCare)
}


package nucleusrv.components
import chisel3._
import chisel3.util._ 

import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig, DecoupledMulti}

class MemoryFetch(val req:AbstrRequest, val rsp:AbstrResponse)(implicit val config:BusConfig) extends MultiIOModule {
    val aluResultIn = (Input(UInt(32.W)))
    val writeData = (Input(UInt(32.W)))
    val writeEnable = (Input(Bool()))
    val readEnable = (Input(Bool()))
    val readData = (Output(UInt(32.W)))
    val stall = (Output(Bool()))

    val dccmReq = (DecoupledMulti(req))
    val dccmRsp = (Flipped(DecoupledMulti(rsp)))

  // val dataMem: DataMemory = Module(new DataMemory(req, rsp))
  // dataMem.address := aluResultIn
  // dataMem.writeData := writeData
  // dataMem.writeEnable := writeEnable
  // dataMem.readEnable := readEnable

  dccmRsp.ready := true.B

  dccmReq.bits.activeByteLane := "b1111".U
  dccmReq.bits.dataRequest := writeData
  dccmReq.bits.addrRequest := aluResultIn
  dccmReq.bits.isWrite := writeEnable
  dccmReq.valid := Mux(writeEnable | readEnable, true.B, false.B)

  stall := (writeEnable || readEnable) && !dccmRsp.valid

  // dccmReq <> dataMem.coreDccmReq
  // dataMem.coreDccmRsp <> dccmRsp

  readData := Mux(dccmRsp.valid, dccmRsp.bits.dataResponse, DontCare) //dataMem.readData

  when(writeEnable && aluResultIn(31, 28) === "h8".asUInt()){
    printf("%x\n", writeData)
  }

}

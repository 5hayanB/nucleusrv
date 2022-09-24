package nucleusrv.components

import chisel3._
import chisel3.util._
import caravan.bus.common.{BusConfig, AbstrRequest, AbstrResponse, DecoupledMulti}

class MemoryWrapper(val req:AbstrRequest, val rsp:AbstrResponse)(implicit val config:BusConfig) extends MultiIOModule {
        val request = IO(Flipped(DecoupledMulti(req)))   
        val response = IO(DecoupledMulti(rsp))          

    request.ready := true.B

    val data = Wire(Vec(4,UInt()))
    val mem = Module(new SRam)

    //clock and csb
    // val clk0 = Input(Bool())
    // val csb0 = Input(Bool())

    // for sending request
    when(request.fire() & request.bits.isWrite){                                      
        val maskedData = request.bits.dataRequest.asTypeOf(Vec(4, UInt(8.W)))        
        data := request.bits.activeByteLane.asBools zip maskedData map {                  
            case (b:Bool, i:UInt) => Mux(b, i, 0.U)
        }

        // feed these pins into the BLACK BOX of SRAM/Peripheral
        mem.din0 := data.asUInt                     // ye data hy 
        mem.addr0 := request.bits.addrRequest     // ye address hay
        mem.web0 := ~request.bits.isWrite         // ye write enable hy
        mem.csb0 := 1.B

    }.elsewhen(request.fire() & ~request.bits.isWrite){                                // if req is of read
        
        mem.din0 := request.bits.addrRequest     // ye address hy
        mem.addr0 := request.bits.dataRequest     // ye data hy, but kisi kaam ka nahi
        mem.web0 := ~request.bits.isWrite         // ye write enable h, low hga read k lye
        mem.csb0 := 1.B

    }.otherwise{
        mem.din0 := DontCare
        mem.addr0 := DontCare  
        mem.web0 := DontCare
        mem.csb0 := DontCare

        

    }

    // For recieveing response
    val responseData =  mem.dout0                // yahan data phek do, response se any wala

    // CAUTION => If data is coming after 1 or more cycles, you have preserve the request.bits.activeByteLane pin
    // until the data comes back as response
    // If your Module(SRAM/Peripheral) can done masking inside of it then it willbe good. Otherwise PRESERVE IT!
    
    val maskedData = responseData.asTypeOf(Vec(4, UInt(8.W)))                       // breaking into Vecs to apply masking
    data := request.bits.activeByteLane.asBools zip maskedData map {                  // applying maskiing a/c to mask bits (activeByteLane)
        case (b:Bool, i:UInt) => Mux(b, i, 0.U)
    }

    response.bits.dataResponse := data.asUInt        // sending data as response
    response.bits.error := false.B                    // implement a logic for error here, if the response has error
    response.valid := true.B                          // implement a logic for indicating that the requested READ/WRITE operation is done and the response signal coming is valud
                                                        // valid pin shall be high for one cycle ONLY
}
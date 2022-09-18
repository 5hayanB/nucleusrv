
package nucleusrv.components
import chisel3._

class Registers() extends MultiIOModule {

    val readAddress = IO(Input(Vec(2, UInt(5.W))))
    val writeEnable = IO(Input(Bool()))
    val writeAddress = IO(Input(UInt(5.W)))
    val writeData = IO(Input(UInt(32.W)))

    val readData = IO(Output(Vec(2, UInt(32.W))))

  val reg = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  when(writeEnable) {
    reg(writeAddress) := writeData
  }
  for (i <- 0 until 2) {
    when(readAddress(i) === 0.U) {
      readData(i) := 0.U
    }.otherwise {
      readData(i) := reg(readAddress(i))
    }
  }

  // readData1 := registerFile(readAddress1)
  // readData2 := registerFile(readAddress2)

  // when(writeEnable) {
  //   registerFile(writeAddress) := writeData
  // }
}

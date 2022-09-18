
package nucleusrv.components
import chisel3._

class HazardUnit extends MultiIOModule {
    val id_ex_memRead = IO(Input(Bool()))
    val ex_mem_memRead = IO(Input(Bool()))
    val id_ex_branch = IO(Input(Bool()))
    val id_ex_rd = IO(Input(UInt(5.W)))
    val ex_mem_rd = IO(Input(UInt(5.W)))
    val id_rs1 = IO(Input(UInt(5.W)))
    val id_rs2 = IO(Input(UInt(5.W)))
    val dmem_resp_valid = IO(Input(Bool()))
    val taken = IO(Input(Bool()))
    val jump = IO(Input(UInt(2.W)))
    val branch = IO(Input(Bool()))

    val if_reg_write = IO(Output(Bool()))
    val pc_write = IO(Output(Bool()))
    val ctl_mux = IO(Output(Bool()))
    val ifid_flush = IO(Output(Bool()))
    val take_branch = IO(Output(Bool()))

  ctl_mux := true.B
  pc_write := true.B
  if_reg_write := true.B
  take_branch := true.B
  ifid_flush := false.B

//  load-use hazard
  when(
    (id_ex_memRead || branch) &&
      (id_ex_rd === id_rs1 || id_ex_rd === id_rs2 ) &&
      ((id_ex_rd =/= 0.U && id_rs1 =/= 0.U) ||
      (id_ex_rd =/= 0.U && id_rs2 =/= 0.U)) &&
      !id_ex_branch
  )
  {
    ctl_mux := false.B
    pc_write := false.B
    if_reg_write := false.B
    take_branch := false.B
  }

  when(ex_mem_memRead && branch && (ex_mem_rd === id_rs1 || ex_mem_rd === id_rs2)){
    ctl_mux := false.B
    pc_write := false.B
    if_reg_write := false.B
    take_branch := false.B
  }

  //branch hazard
  when(taken || jump =/= 0.U) {
    ifid_flush := true.B
  }.otherwise {
    ifid_flush := false.B
  }

}

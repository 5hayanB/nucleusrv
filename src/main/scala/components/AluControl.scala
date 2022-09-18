
// Copyright (C) 2020-2021 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package nucleusrv.components
import chisel3._
import chisel3.util._

class AluControl extends MultiIOModule {
    val aluOp = IO(Input(UInt(2.W)))
    val f7 = IO(Input(UInt(1.W)))
    val f3 = IO(Input(UInt(3.W)))
    val aluSrc = IO(Input(Bool()))
    val out = IO(Output(UInt(4.W)))

  out := 15.U

  when(aluOp === 0.U) {
    out := 2.U
  }.otherwise { //(aluOp === 2.U)
    switch(f3) {
      is(0.U) {
        when(!aluSrc || f7 === 0.U) {
          out := 2.U
        } //add
          .otherwise {
            out := 3.U
          } // sub
      }
      is(1.U) {
        out := 6.U
      } // sll
      is(2.U) {
        out := 4.U
      } // slt
      is(3.U) {
        out := 5.U
      } // sltu
      is(5.U) {
        when(f7 === 0.U) {
          out := 7.U // srl
        }.otherwise {
          out := 8.U // sra
        }
      }
      is(7.U) {
        out := 0.U
      } // and
      is(6.U) {
        out := 1.U
      } // or
      is(4.U) {
        out := 9.U
      } //xor
    }
  }
}

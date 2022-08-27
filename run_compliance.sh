#!/bin/bash

export NUCLEUSRV=$PWD
FILE=$NUCLEUSRV/test_run_dir/Top_Test/VTop

TEST=$1
cd $NUCLEUSRV/riscv-arch-test
make clean TARGETDIR=$NUCLEUSRV/riscv-target RISCV_TARGET=nucleusrv RISCV_DEVICE=rv32i RISCV_ISA=rv32im RISCV_TEST=$TEST TARGET_SIM=$NUCLEUSRV/test_run_dir/Top_Test/VTop
make TARGETDIR=$NUCLEUSRV/riscv-target RISCV_TARGET=nucleusrv RISCV_DEVICE=rv32i RISCV_ISA=rv32imc RISCV_TEST=$TEST TARGET_SIM=$NUCLEUSRV/test_run_dir/Top_Test/VTop | tee Test_result.txt
cd ../

addi sp sp -32
sw s0 28(sp)
addi s0 sp 32
sw zero -20(s0)
li a5 1
sw a5 -24(s0)
sw zero -28(s0)
j 8000004c
lw a4 -20(s0)
lw a5 -24(s0)
add a5 a4 a5
sw a5 -32(s0)
lw a5 -24(s0)
sw a5 -20(s0)
lw a5 -32(s0)
sw a5 -24(s0)
lw a5 -28(s0)
addi a5 a5 1
sw a5 -28(s0)
lw a4 -28(s0)
li a5 9
bge a5 a4 80000020
nop
nop
lw s0 28(sp)
addi sp sp 32
ret
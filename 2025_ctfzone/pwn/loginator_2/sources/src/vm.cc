#include "vm.h"

Vmxxx::Vmxxx() {
}

void Vmxxx::setup_instruction(byte opcode,Instruction* ins) {
  handles[opcode] = ins;
}

void Vmxxx::execute() {
  ip = 0;
  while (ip < code.size()) {
    byte opcode = code[ip];
    auto ins = handles[opcode];
    if (ins != nullptr) {
      ins->execute();
      ip+=ins->get_size();
    }
    else{
      std::cout<<"No such instruction for opcode: "<<opcode<<"\n";
      return;
    }
  } 
}

void Vmxxx::load_code(std::string &code_str) {
  code.clear();
  for (char c : code_str) {
    code.push_back(static_cast<byte>(c));
  }
}

void Vmxxx::reserve_code(size_t code_size) {
  code.resize(code_size);
}

void Vmxxx::reserve_memory(size_t memory_size) {
  memory.resize(memory_size);
}

void Vmxxx::reserve_regs(size_t regs_cnt) {
  registers.resize(regs_cnt);
  std::fill(registers.begin(), registers.end(), 0ULL);
}

void MOVRV ::execute() {
  uint8_t src = Vmxxx::code[Vmxxx::ip + 1];
  uint64_t dest = *(uint64_t*)&Vmxxx::code[Vmxxx::ip + 2];
  if(src < Vmxxx::registers.size()) {
    Vmxxx::registers[src] = dest;
  }
  else {
    std::cout<<"registers OOB detected at MOVRV"<<"\n";
    exit(-1);
  }
}

void XORRR ::execute() {
  uint8_t src1 = Vmxxx::code[Vmxxx::ip + 1];
  uint8_t src2 = Vmxxx::code[Vmxxx::ip + 2];
  if(src1 < Vmxxx::registers.size() && src2 < Vmxxx::registers.size()) {
    Vmxxx::registers[src1] = Vmxxx::registers[src1] ^ Vmxxx::registers[src2];
  }
  else {
    std::cout<<"registers OOB detected at XORRR"<<"\n";
    exit(-1);
  }
}

void ADDRR ::execute() {
  uint8_t src1 = Vmxxx::code[Vmxxx::ip + 1];
  uint8_t src2 = Vmxxx::code[Vmxxx::ip + 2];
  if(src1 < Vmxxx::registers.size() && src2 < Vmxxx::registers.size()) {
    Vmxxx::registers[src1] = Vmxxx::registers[src1] + Vmxxx::registers[src2];
  }
  else {
    std::cout<<"registers OOB detected at ADDRR"<<"\n";
    exit(-1);
  }
}

void SUBRR ::execute() {
  uint8_t src1 = Vmxxx::code[Vmxxx::ip + 1];
  uint8_t src2 = Vmxxx::code[Vmxxx::ip + 2];
  if(src1 < Vmxxx::registers.size() && src2 < Vmxxx::registers.size()) {
    Vmxxx::registers[src1] = Vmxxx::registers[src1] - Vmxxx::registers[src2];
  }
  else {
    std::cout<<"registers OOB detected at SUBRR"<<"\n";
    exit(-1);
  }
}

void CMPRR ::execute() {
  Vmxxx::flags.zero = false;
  Vmxxx::flags.greater = false;
  Vmxxx::flags.less = false;
  uint8_t src1 = Vmxxx::code[Vmxxx::ip + 1];
  uint8_t src2 = Vmxxx::code[Vmxxx::ip + 2];
  if(src1 < Vmxxx::registers.size() && src2 < Vmxxx::registers.size()) {
    int64_t result = Vmxxx::registers[src1] - Vmxxx::registers[src2];

    if (result == 0) {
      Vmxxx::flags.zero = true;
    } else if (result > 0) {
      Vmxxx::flags.greater = true;
    } else {
      Vmxxx::flags.less = true;
    }
  }
  else {
    std::cout<<"registers OOB detected at CMPRR"<<"\n";
    exit(-1);
  }
}

void JMP ::execute() {
  uint32_t target = *(uint32_t *)&(Vmxxx::code[Vmxxx::ip + 1]);

  Vmxxx::ip = target;
}

void JE ::execute() {
  if (Vmxxx::flags.zero) { // If zero flag is set, do not jump
    uint32_t target = *(uint32_t *)&(Vmxxx::code[Vmxxx::ip + 1]);
    Vmxxx::ip = target;
  } else {
    Vmxxx::ip += 5; // Move past this instruction if condition not met
  }
}

void JNE ::execute() {
  if (Vmxxx::flags.zero) { // If zero flag is set, do not jump
    uint32_t target = *(uint32_t *)&(Vmxxx::code[Vmxxx::ip + 1]);
    Vmxxx::ip = target;
  } else {
    Vmxxx::ip += 5; // Move past this instruction if condition not met
  }
}

void MOVRR::execute(){
  uint8_t src1 = Vmxxx::code[Vmxxx::ip + 1];
  uint8_t src2 = Vmxxx::code[Vmxxx::ip + 2];
  if(src1 < Vmxxx::registers.size() && src2 < Vmxxx::registers.size()) {
    Vmxxx::registers[src1] = Vmxxx::registers[src2];
  }
}

void MOVRM::execute(){
  uint8_t src1 = Vmxxx::code[Vmxxx::ip + 1];
  uint8_t src2 = Vmxxx::code[Vmxxx::ip + 2];
  if(src1 < Vmxxx::registers.size() && src2 < Vmxxx::memory.size()) {
    Vmxxx::registers[src1] = *(uint64_t*)&Vmxxx::memory[src2];
  }
}

void MOVMR::execute(){
  uint8_t src1 = Vmxxx::code[Vmxxx::ip + 1];
  uint8_t src2 = Vmxxx::code[Vmxxx::ip + 2];
  if(src2 < Vmxxx::registers.size() && src1 < Vmxxx::memory.size()) {
    uint64_t value = 0;
    for(int i=0;i<8;i++)
      Vmxxx::memory[src1+i] = (Vmxxx::registers[src2] >> i*8) & 0xff;
  }
}

void OUT::execute() {
  uint8_t off = Vmxxx::code[Vmxxx::ip + 1];
  uint8_t len = Vmxxx::code[Vmxxx::ip + 2];
  for(int i=0;i<len;i++)
    std::cout<<Vmxxx::memory[off+i];
}

void IN::execute() {
  uint8_t off = Vmxxx::code[Vmxxx::ip + 1];
  uint8_t len = Vmxxx::code[Vmxxx::ip + 2];
  for(int i=0;i<len;i++)
    std::cin>>Vmxxx::memory[off+i];
}

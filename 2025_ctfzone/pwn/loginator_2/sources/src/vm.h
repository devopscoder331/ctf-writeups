#include <cstdint>
#include <iostream>
#include <unordered_map>
#include <string>
#include <vector>

#define byte unsigned char

#ifndef VMXXX
#define VMXXX

class Instruction {
public:
  Instruction() { size = 0; }
  virtual void execute() = 0;
  int get_size() { return size; };
  int size = 0;

private:
};

typedef struct Flags {
  char zero : 1;
  char greater : 2;
  char less : 3;
} Flags;

class Vmxxx {
public:
  Vmxxx();
  void reserve_memory(size_t memory_size);
  void reserve_code(size_t code_size);
  void reserve_regs(size_t regs_cnt);
  void execute();
  void load_code(std::string &code);
  void setup_instruction(byte opcode,Instruction* ins);
  inline static std::unordered_map<byte, Instruction *> handles;
  inline static std::vector<uint8_t> code;
  inline static std::vector<uint8_t> memory;
  inline static std::vector<uint64_t> registers;
  inline static uint32_t ip;
  inline static Flags flags;
private:
};

class MOVRR : public Instruction {
public:
  MOVRR() { size = 3; }
  void execute() override;

private:
};

class MOVMR : public Instruction {
public:
  MOVMR() { size = 3; }
  void execute() override;

private:
};

class MOVRM : public Instruction {
public:
  MOVRM() { size = 3; }
  void execute() override;

private:
};

class MOVRV : public Instruction {
public:
  MOVRV() { size = 10; }
  void execute() override;

private:
};

class XORRR : public Instruction {
public:
  XORRR() { size = 3; }
  void execute() override;

private:
};

class ADDRR : public Instruction {
public:
  ADDRR() { size = 3; }
  void execute() override;

private:
};

class SUBRR : public Instruction {
public:
  SUBRR() { size = 3; }
  void execute() override;

private:
};

class CMPRR : public Instruction {
public:
  CMPRR() { size = 3; }
  void execute() override;

private:
};

class JMP : public Instruction {
public:
  JMP() { size = 2; }
  void execute() override;

private:
};

class JE : public Instruction {
public:
  JE() { size = 2; }
  void execute() override;

private:
};

class JNE : public Instruction {
public:
  JNE() { size = 2; }
  void execute() override;

private:
};

class IN : public Instruction {
public:
  IN() { size = 3; }
  void execute() override;

private:
};

class OUT : public Instruction {
public:
  OUT() { size = 3; }
  void execute() override;

private:
};

#endif

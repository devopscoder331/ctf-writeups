#include <iostream>
#include <limits>
#include "./vm.h"

void win() {
  system("/bin/sh");
}

void print_str(std::string str) {
  std::cout<<str;
}

void menu(){
  print_str("Welcome to vmxxx program.\n");
  print_str("Enter 1 to write and execute code.\n");
  print_str("Enter 2 to exit.\n");
  print_str(">> \n");
}

int read_integer() {
    int number;
    while (true) {
        std::cin >> number;
        if (std::cin) { // Check if input is valid and not zero
            break;
        } else {
            std::cin.clear(); // Clear the error state
            std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n'); // Flush buffer up to newline
            std::cout << "Invalid input. Please enter an integer: ";
        }
    }
    return number;
}

Vmxxx vmx;
MOVRR movrr;
MOVMR movmr;
MOVRM movrm;
MOVRV movrv;
XORRR xorrr;
ADDRR addrr;
SUBRR subrr;
CMPRR cmprr;
JMP jmp;
JE je;
JNE jne;
IN in;
OUT out;

void create_and_run_vm(std::string& code){
  vmx.setup_instruction(0x40,&movrr);
  vmx.setup_instruction(0x41,&movrm);
  vmx.setup_instruction(0x42,&movmr);
  vmx.setup_instruction(0x43,&movrv);
  vmx.setup_instruction(0x44,&xorrr);
  vmx.setup_instruction(0x45,&addrr);
  vmx.setup_instruction(0x46,&subrr);
  vmx.setup_instruction(0x47,&cmprr);
  vmx.setup_instruction(0x48,&jmp);
  vmx.setup_instruction(0x49,&je);
  vmx.setup_instruction(0x4a,&jne);
  vmx.setup_instruction(0x4b,&in);
  vmx.setup_instruction(0x4c,&out);
  vmx.load_code(code);
  vmx.reserve_memory(0x100);
  vmx.reserve_regs(0x10);
  vmx.execute();
  return;
}

int main() {
  while(true) {
    menu();
    int option = read_integer();
    switch(option){
      case 1:{
        std::string code;
        std::cout<<"Enter your code"<<"\n";
        std::cout<<">> \n";
        std::cin >> code;
        create_and_run_vm(code);
      }
      break;
      case 2:
      return 0;
      break;
    }
  }
  return 0;
}

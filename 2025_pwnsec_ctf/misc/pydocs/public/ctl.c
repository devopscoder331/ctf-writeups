// build:  gcc -O2 -Wall -Wextra -o ctl ctl.c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/stat.h>


int main(int argc, char **argv){
    if(strcmp(argv[1],"path")==0){
        // To-do: Implement PATH modification
        return 0;
    }

    if(strcmp(argv[1],"permission")==0){
        const char *file = argv[2];
        if(chmod(file, 0555)!=0){
            return 1;
        }
        return 0;
    }

    return 1;
}

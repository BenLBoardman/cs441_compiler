## CS441 Compiler Milestone 1 - "Connor F. Gershwin"

This is my submission for the first milestone of the CS441 compiler. This document explains usage instructions and the peephole optimization I chose to use.

### Usage
This compiler utilizes bash scripts to build and run and it requires Java 21 (it therefore will not work on Tux). Before running it the first time, run `./build.sh` to run the gradle build process. `./comp.sh <args>` will then run the compiler with any args specified.
#### Args
There is one required command-line arg and two optional arguments. With no arguments, the CFG produced is printed to the console.
- The required arg is the name of the file to compile. It must be located within `cs441-compiler/test-code`. It can be located in a subdirectory, but that needs to be specified in the argument.
- `-noSSA` outputs the CFG before conmverting it to SSA and terminates the program there.
- `-o <outfile>` specifies a file to write the program output to instead of the console. This file is created if it doesn't exist and placed in the directory `cs441-compiler/test-out`. The program will recognize subdirectories if they exist, but cannot create subdirectories of its own.

### Optimization
I chose to do pieces of multiple peephole optimizations.
- *Rejecting Invalid Code*: There are several locations where the compiler will reject invalid code during compilation. For example, attempts to write to or perform math on `this` will be rejcted at compile time. 
- *Constant Expression Simplification*: Constant expressions will be simplified at compile time. An example of this is shown in `constmath.comp`.
- *This tag checking*: Pointer and integer tag checks will not be generated on accesses and calls to `this`. As well, many cases where `this` could be treated as an integer are automatically rejected at compile time.

### Errors
There are no errors in the code that I have been using for tests, but that does not mean there aren't latent errors that I did not catch. I was able to catch many errors by slapping a loop into the simple stack code, which likely reduced the set of potential latent errors by exposing and forcing the fixing of several different pointer, jump, phi, and loop errors.

### Time Spent
While I did not track the amount of time I spent on this, my estimation is that it is above twenty hours, and possibly above thirty.

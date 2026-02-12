## CS441 Compiler Milestone 2
This is my submission for the second milestone of the CS441 compiler. This document explains usage instructions and the peephole optimization I chose to use.

### Usage
This compiler utilizes bash scripts to build and run and it requires Java 21 (it therefore will not work on Tux). Before running it the first time, run `./build.sh` to run the gradle build process. `./comp.sh <args>` will then run the compiler with any args specified.
#### Args
There is one required command-line arg and two optional arguments. With no arguments, the CFG produced is printed to the console.
- The required arg is the name of the file to compile. It must be located within `cs441-compiler/test-code`. It can be located in a subdirectory, but that needs to be specified in the argument.
- `-noSSA` outputs the CFG before converting it to SSA and terminates the program there.
- `-o <outfile>` specifies a file to write the program output to instead of the console. This file is created if it doesn't exist and placed in the directory `cs441-compiler/test-out`. The program will recognize subdirectories if they exist, but cannot create subdirectories of its own.
- *new* `-simpleSSA` causes the compiler to use the more maximal phi-placement code developed in Milestone I to produce SSA code. This flag does not skip dominator calculation, so it should be compatible with all later optimizations.
- *new* `-noVN` causes the compiler to skip the value numbering step of the process. If `-noSSA` is set, this will be set automatically,
- *new* `-debug` allows debug printouts to work. This is not used anywhere in the current compiler version, but is placed for future use.

### Optimization - Milestone 1
I chose to do pieces of multiple peephole optimizations.
- *Rejecting Invalid Code*: There are several locations where the compiler will reject invalid code during compilation. For example, attempts to write to or perform math on `this` will be rejcted at compile time. 
- *Constant Expression Simplification*: Constant expressions will be simplified at compile time. An example of this is shown in `constmath.comp`.
- *This tag checking*: Pointer and integer tag checks will not be generated on accesses and calls to `this`. As well, many cases where `this` could be treated as an integer are automatically rejected at compile time.

### Optimization - Milestone 2
Local value numbering is fully implemented. Any time a value is computed that has already been computed in the same basic block, the second computation will be removed and all references to the variable that the redundant computation is stored in will be replaced with references to the variable that stored the result of the original computation. A couple of other minor tweaks have also been made:
- SSA code (phi placement excluded) has been cleaned up and sped up
- Cases where a tag check or class initialization will generate a temp value that then gets assigned to be a named value are replaced to just fully assign to the named value, which improves IR readability.


### Test Code
Several test-code files have been produced to highlight various tweaks, but the two most useful are `xtraphi.comp` and `vn_ex.comp`.
- `xtraphi.comp` is a contrived example designed to force the creation of a useless phi. It assigns a variable `x` twice before entering a loop, where `x` is unmodified. The simple SSA code creates a phi there, as it is maximal, while the optimizeed SSA realizes that `x` is neityher assigned nor read in the while loop and therefore ignores it. Value numbering also still works on this example in simple/maximal SSA.
- `vn_ex.comp` is a similarly contrived example intended to showcase local value numbering. In this case, we make a bunch of identical assignments, add a print (to break the basic block up), and then make some more assignments for fun. Looking at VN-less and VN-ed output, it's pretty clear that there are less lines in the VN output. 

### Errors
There are no errors in the code that I have been using for tests, but that does not mean there aren't latent errors that I did not catch.
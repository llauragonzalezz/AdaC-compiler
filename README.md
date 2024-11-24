 
# AdaC Compiler  

Complete ADAC compiler developed in Java for the **Language Processing and Compiler Design** course along ![@Sondeluz](https://github.com/Sondeluz) during the 2021/2022 academic year at Universidad de Zaragoza. 

ADAC is a fictional programming language combining features of **Ada** and **C**, designed for educational purposes. The compiler implements:  
- Lexical analysis  
- Syntactic analysis  
- Semantic analysis  
- Intermediate code generation targeting the **P virtual machine**  

## Features  
- Generates valid `.pcode` files for the P virtual machine   
- Modular design with distinct phases: lexing, parsing, semantic analysis, and code generation  
- Supports nested procedures and functions and handles scalars and vectors as parameters  

## Requirements  
- **Apache Ant**: for building and cleaning the project  
- **JavaCC**: for processing the compiler definition files. Update javacchome in the build.xml file to point to your JavaCC installation.
- **Ruby**: Required to execute test script.

## Building and Running  
1. **Building the compiler:**  
   Run the following command from the project root: 
   ```bash 
   ant -f build.xml
  The output will be a .jar file located in the dist/ folder.

2. Cleaning the project:
   Remove all generated files with
   ```bash 
   ant clean
3. Using the compiler:
   To compile an ADAC program, use the generated JAR file:
   ```bash 
   java -jar dist/adac_4.jar <program>.adac
4. Run All Tests
To test the compiler with the included test files, execute:
   ```bash
   ruby test.rb

## Additional Information
All documentation is written in Spanish

All test files required to validate the compiler are included in the doc/ directory

The compiler generates .pcode files as intermediate code for the P Virtual Machine, but executables to translate P code to assembly or to directly execute P code are not included in this project. To run .pcode files, an external tool for the P Virtual Machine is required.

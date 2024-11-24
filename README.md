 
# adac_compiler  

Complete ADAC compiler developed for the **Language Processing, Compiler Design** course along ![@Sondeluz](https://github.com/Sondeluz) during the 2021/2022 academic year at Universidad de Zaragoza. 

ADAC is a fictional programming language combining features of **Ada** and **C**, designed for educational purposes. The compiler implements:  
- Lexical analysis  
- Syntactic analysis  
- Semantic analysis  
- Intermediate code generation targeting the **P virtual machine**  

The project includes integration with the tools needed to assemble and run the generated P-code.

## Features  
- Generates valid `.pcode` files for the P virtual machine  
- Structured and maintainable implementation leveraging JavaCC for lexical and syntactic analysis  
- Modular design with distinct phases: lexing, parsing, semantic analysis, and code generation  
- Supports nested procedures and functions  
- Handles scalars and vectors as parameters  

## Requirements  
- **Apache Ant**: for building and cleaning the project  
- **JavaCC**: for processing the compiler definition files   

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

## Additional Information
All documentation is written in Spanish
All test files required to validate the compiler are included in the doc/ directory.

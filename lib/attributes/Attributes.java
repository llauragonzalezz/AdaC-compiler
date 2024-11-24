//*****************************************************************
// File:   Attributes.java
// Author: Procesadores de Lenguajes-University of Zaragoza
// Date:   enero 2022
//         Clase única para almacenar los atributos que pueden aparecer en el traductor de adac
//         Se hace como clase plana, por simplicidad. Los atributos que se pueden almacenar
//         se autoexplican con el nombre.
//*****************************************************************

package lib.attributes;
import lib.symbolTable.*;
import lib.symbolTable.Symbol.Types;

import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import lib.tools.codeGeneration.*;
import traductor.Token;


// Nota: A la hora de realizar la generación de código, su complejidad ha aumentado considerablemente.
// Con más tiempo, se podría prescindir de muchos de sus atributos
public class Attributes implements Cloneable {
    public Symbol.Types type;
    public Symbol.Types baseType = Symbol.Types.UNDEFINED; // UNDEFINED si no es vector / componente de vector
    // 0 si no es vector / componente de vector
    public int minInd = 0;
    public int maxInd = 0;
    public Symbol.ParameterClass parClass = Symbol.ParameterClass.NONE;

    // Nombre de la variable / procedimiento / función
    public String nombre;

    // Referencia al símbolo en la tabla al que representa
    public Symbol simboloTabla = null;

    // Referencia al token al que representa
    public Token token = null;

    // Valores para constantes, para posibles optimizaciones
    // No se han utilizado realmente, por falta de tiempo
    public int valInt;
    public boolean valBool;
    public char valChar;
    public String valString;

    // Flags para la generación de código
    public boolean esAsignable = false; 
    public boolean esConstante = false;                
    public boolean tipoConvertido = false;

    // Bloque de código sintetizado que representa de cualquier producción
    public CodeBlock b = new CodeBlock();

    // Constructores

    public Attributes() {
        this.type = Symbol.Types.UNDEFINED;
    }

    public Attributes(Symbol simbolo) {
        type = simbolo.type;
        parClass = simbolo.parClass;
    }

    public void setBaseType(Symbol.Types simbolo) {
        baseType = simbolo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Attributes(int _valInt) {
        type = Symbol.Types.INT;
        valInt = _valInt;
    }

    public Attributes(int _valInt, Symbol.ParameterClass _parClass) {
        type = Symbol.Types.INT;
        valInt = _valInt;
        parClass = _parClass;
    }

    public Attributes(boolean _valBool) {
        type = Symbol.Types.BOOL;
        valBool = _valBool;
    }

    public Attributes(boolean _valBool, Symbol.ParameterClass _parClass) {
        type = Symbol.Types.BOOL;
        valBool = _valBool;
        parClass = _parClass;
    }

    public Attributes(char _valChar) {
        type = Symbol.Types.CHAR;
        valChar = _valChar;
    }

    public Attributes(char _valChar, Symbol.ParameterClass _parClass) {
        type = Symbol.Types.CHAR;
        valChar = _valChar;
        parClass = _parClass;
    }

    public Attributes(String _valString) {
        type = Symbol.Types.STRING;
        valString = _valString;
    }

    // Indica si son del mismo tipo, infiriendo si alguno de los atributos son
    // arrays o funciones
    public boolean sonDelMismoTipo(Attributes attr) {   
        Types t1 = attr.type;
        Types t2 = this.type;
        
        if (attr.type == Symbol.Types.FUNCTION ) {
            SymbolFunction sf = (SymbolFunction) attr.simboloTabla;
           t1 = sf.returnType;
        } else if (attr.type == Symbol.Types.ARRAY) {
            t1 = attr.baseType;
        }

        if (this.type == Symbol.Types.FUNCTION ) {
            SymbolFunction sf = (SymbolFunction) this.simboloTabla;
            t2 = sf.returnType;
        } else if (this.type == Symbol.Types.ARRAY) {
            t2 = this.baseType;
        }
     
        return (t1 == t2);
    }

    // Funciones auxiliares

    public boolean esInt() {
        return this.type == Symbol.Types.INT;
    }

    public boolean esBool() {
        return this.type == Symbol.Types.BOOL;
    }

    public boolean esChar() {
        return this.type == Symbol.Types.CHAR;
    }

    public boolean esString() {
        return this.type == Symbol.Types.STRING;
    }

    public boolean noTieneTipo() {
        return this.type == Symbol.Types.UNDEFINED;
    }

    public boolean esProcedimiento() {
        return this.type == Symbol.Types.PROCEDURE;
    }

    public boolean esFuncion() {
        return this.type == Symbol.Types.FUNCTION;
    }

    public void setAsignable() {
        this.esAsignable = true;
    }

    public void unsetAsignable() {
        this.esAsignable = false;
    }

    public String toString() {
        String mensaje = "(tipo = " + type + ", " + "asignable = " + esAsignable;
        
        mensaje += (baseType != Symbol.Types.UNDEFINED) ? ", tipo base = " + baseType + ", minInd = " + minInd + ", maxInd = " + maxInd : "";

        return mensaje + ")";
    }

    public Attributes clone() {
    	try {
    		return (Attributes) super.clone();
    	}
    	catch (CloneNotSupportedException e) {
    		return null; 
    	}
    }
}

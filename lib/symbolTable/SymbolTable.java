//*****************************************************************
// File:   SymbolTable.java
// Author: Procesadores de Lenguajes-University of Zaragoza
// Date:   julio 2021
// Coms:   Atributos públicos para ahorrarnos el uso de getters y setters
//*****************************************************************

//la tabla de símbolos será un ArrayList de diccionarios (HashMap<String, Symbol>), manejada como
//una pila: se inserta y accede por la derecha
//Cada nuevo bloque se apilará, guardando los símbolos en el diccionario correspondiente
//El constructor ya genera el primer bloque, vacío inicialmente.

//https://quick-adviser.com/can-a-hashmap-have-multiple-values-for-same-key/
//HashMap doesn’t allow duplicate keys but allows duplicate values. That means A 
//single key can’t contain more than 1 value but more than 1 key can contain a single value.
//HashMap allows null key also but only once and multiple null values.
//https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html

package lib.symbolTable;

import java.util.*;
import lib.symbolTable.exceptions.SymbolNotFoundException;
import lib.tools.codeGeneration.CGUtils;
import lib.symbolTable.exceptions.AlreadyDefinedSymbolException;

public class SymbolTable {
	private final int ST_SIZE = 16; //hasta 16 niveles
    private final int HASH_SIZE = 1024; //buckets
    private ArrayList<HashMap<String, Symbol>> st;
    private Set<String> reservedWords;

    public int level; //nivel actual
    public boolean haHabidoReturn; // Indicador de si ha habido returns en la función del bloque actual

    public SymbolTable() {
        st = new ArrayList<HashMap<String, Symbol>>(ST_SIZE);
        level = -1; // Aún no hay ningún bloque intoducido
        reservedWords = new HashSet<String> ();
    }

    public SymbolTable(String[] pals) {
    	st = new ArrayList<HashMap<String, Symbol>>(ST_SIZE);
        level = -1; // Aún no hay ningún bloque introducido
        reservedWords = new HashSet<String>(Arrays.asList(pals));
    }
    
    // Apila un nuevo bloque
    public void insertBlock() {
        st.add(new HashMap<String, Symbol>(HASH_SIZE));
        level++;
        haHabidoReturn = false;
        CGUtils.memorySpaces[level] = 3; // Siempre se reservan tres posiciones por defecto en los bloques
    }

    // Elimina un bloque
    public void removeBlock() {
        st.remove(st.size()-1);
        level--;
        haHabidoReturn = false;
    }

    // Inserta las palabras reservadas, sustituyendo el anterior contenido
    public void insertReservedWords(String[] pals) {
        reservedWords = new HashSet<String>(Arrays.asList(pals));
    }
    
    public boolean isReservedWord (String word)  
    {
    	return reservedWords.contains(word); 
    }
    
    public void insertSymbol(Symbol s) throws AlreadyDefinedSymbolException {
        if(reservedWords.contains(s.name)) {
            throw new AlreadyDefinedSymbolException();
        }
        HashMap<String, Symbol> currentBlock = st.get(st.size()-1);
        if (currentBlock.containsKey(s.name)) { // ya está
            throw new AlreadyDefinedSymbolException();
        } else {
            s.nivel = level;

            // Se inserta un array
            if (s instanceof SymbolArray) {    
                SymbolArray sa = (SymbolArray) s;
                // Y es un parámetro por valor o como declaración
                if (sa.parClass == Symbol.ParameterClass.VAL || sa.parClass == Symbol.ParameterClass.NONE) {
                    s.dir = CGUtils.memorySpaces[level];
                    CGUtils.memorySpaces[level] += sa.maxInd+1;
                } else { // Es un parámetro por referencia (@ del array únicamente)
                    s.dir = CGUtils.memorySpaces[level]++;
                }
            } else { // No es un array
                if (!(s instanceof SymbolFunction || s instanceof SymbolProcedure)) {
                    s.dir = CGUtils.memorySpaces[level]++; // Se asigna y aumenta una dirección si no es un proc/func
                }
            } 

            currentBlock.put(s.name, s);
        }
    }

    // Si no está, excepción. Si está, devuelve su referencia
    public Symbol getSymbol (String name) throws SymbolNotFoundException {
    	Symbol result = findSymbol(name); 
        if (result == null) {
        	throw new SymbolNotFoundException();
        }
        return result; 
    }
    
    // Comprueba si está el símbolo 
    public boolean containsSymbol (String name) {
    	return findSymbol(name) != null; 
    }
    
    // Para usar en "getSymbol" y "containsSymbol"
    private Symbol findSymbol (String name) {
    	for (int i=st.size()-1; i>=0; i--) {
            if (st.get(i).containsKey(name)) {
                return st.get(i).get(name);
            }
        }
        return null; 
    }

    // Devuelve la tabla como un string
    public String toString() {
    	final String linea = "------------------------------------------------\n";
    	StringBuilder builder = new StringBuilder(); 
        builder.append(linea); 
        String tabs = "";
        for (int i=0; i<st.size(); i++) {
            for (Map.Entry entry : st.get(i).entrySet()) {
				//crear secuencia de tabuladores
                tabs = new String(new char[i]).replace("\0", "\t");
                builder.append(tabs); 
                builder.append(entry.toString()); 
                builder.append("\n"); 
            }
        }
        builder.append(linea); 
        return builder.toString();
    }
}
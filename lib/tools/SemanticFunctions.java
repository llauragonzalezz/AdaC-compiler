//*****************************************************************
// Tratamiento de errores sintácticos
// Fichero:    SemanticFunctions.java
// Fecha:      03/03/2022
// Versión:    v1.0
// Asignatura: Procesadores de Lenguajes, curso 2021-2022
//*****************************************************************

package lib.tools;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Stack;

import java.util.*;
import traductor.Token;
import lib.attributes.*;
import lib.symbolTable.*;
import lib.symbolTable.Symbol.Types;
import lib.symbolTable.exceptions.*;
import lib.tools.codeGeneration.CGUtils;
import lib.errores.*;

public class SemanticFunctions {
	public ErrorSemantico errSem;

	// Se ha modificado SemanticFunctions para que contenga la tabla de símbolos
	// Esto permite encerrar la gran mayoría del análisis semántico y el poblado 
	// de la tabla de símbolos en la clase
	public SymbolTable st;
	public static Stack<Symbol> pilaLlamadas;
	public String nombre_proc_principal;

	
	public SemanticFunctions() {
		errSem = new ErrorSemantico();
		st = new SymbolTable();
		obtenerPalabrasReservadas(st);
		pilaLlamadas = new Stack<Symbol>();
	}

	private void obtenerPalabrasReservadas(SymbolTable st) {
		String[] s = new String[] { "boolean","character", "integer", "true", "false", "begin", "if", "mod", "div", "and", 
									"not", "else", "end", "then", "function", "procedure", "is", "while", "do", "return", 
									"put", "or", "put_line", "get", "int2char", "char2int", "skip_line", "val", "ref"};
		
		st.insertReservedWords(s);			
							
	}

	// Devuelve si encuentra un símbolo con el nombre dado en una lista de símbolos
	// A usar en declaraciones de procedimientos y funciones
	private boolean contiene_nombre(ArrayList<Symbol> listaSimbolos, String nombre)  {
		for (Symbol simbolo : listaSimbolos) {
			if (simbolo.name == nombre) {
				return true;
			}
		}

		return false;
	}

	public void tratar_declaracion_variable(Token t1, Token t2, Symbol.Types tipo) {
		int tam;
			try {
				switch(tipo) {
				case INT:
					if (t2 != null) { // Es un vector
						tam = Integer.parseInt(t2.image); 
						st.insertSymbol(new SymbolArray(t1.image, tam, tipo));
					} else {
						st.insertSymbol(new SymbolInt(t1.image));
					}

					break;

				case CHAR:
					if (t2 != null) { // Es un vector
						tam = Integer.parseInt(t2.image); 
						st.insertSymbol(new SymbolArray(t1.image, tam, tipo));
					} else {
						st.insertSymbol(new SymbolChar(t1.image));
					}
					break;

				case BOOL:
					if (t2 != null) { // Es un vector
						tam = Integer.parseInt(t2.image);
						st.insertSymbol(new SymbolArray(t1.image, tam, tipo));
					} else {
						st.insertSymbol(new SymbolBool(t1.image));
					}
					break;
				
				}
			} catch(AlreadyDefinedSymbolException e) {
				errSem.deteccion(e, t1);
			}
	}

	public void tratar_declaracion_funcion(boolean returnPresente, Token t){
		// Una vez se ha procesado la función, se disminuye el nivel
		st.removeBlock();
		pilaLlamadas.pop();

		if (!returnPresente) {
			errSem.warning("Debería haber una o más instrucciones de retorno en la función", t);
		}
	}

	public void tratar_cabecera_funcion(Token t1, Symbol.Types tipoDevuelto,ArrayList<Symbol> parametros, String etiqueta){
		// Se inserta la función y su tipo
		try {
			st.insertSymbol(new SymbolFunction(t1.image, parametros, tipoDevuelto, etiqueta));
	
			// Se aumenta el bloque una vez introducida la función en el actual y antes de introducir los parámetros, que irán al nuevo
			st.insertBlock(); 
			pilaLlamadas.push(new SymbolFunction(t1.image, parametros, tipoDevuelto, etiqueta));
			
			// Y se añaden los parámetros al nuevo bloque
			for (Symbol simbolo : parametros) {
				st.insertSymbol(simbolo);
			}
		} catch (AlreadyDefinedSymbolException e) {
			errSem.deteccion(e, t1);
		}
	}

	public void tratar_declaracion_procedimiento() {
		st.removeBlock();
		pilaLlamadas.pop();
	}

	public void tratar_cabecera_procedimiento(Token t1, ArrayList<Symbol> parametros, String etiqueta){
		try {
			if (st.level == -1) { // Es el bloque del programa principal
				st.insertBlock(); 
				st.insertSymbol(new SymbolProcedure(t1.image, parametros, etiqueta));
				pilaLlamadas.push(new SymbolProcedure(t1.image, parametros, etiqueta));
			} else{
				st.insertSymbol(new SymbolProcedure(t1.image, parametros, etiqueta));
				// Se aumenta el bloque una vez introducida la función en el actual y antes de introducir los parámetros, que irán al nuevo
				st.insertBlock(); 
				pilaLlamadas.push(new SymbolProcedure(t1.image, parametros, etiqueta));
				
				// Y se añaden los parámetros al nuevo bloque
				for (Symbol simbolo : parametros) {
					st.insertSymbol(simbolo);
				}
			}			
		} catch (AlreadyDefinedSymbolException e) {
			errSem.deteccion(e, t1);
		}		
	}

	public void tratar_declaracion_variableFuncionProc(ArrayList<Symbol> listaParametros, Symbol.Types tipo, Symbol.ParameterClass clase, Token t1, Token t2) {
		int tam;
			// Si el nombre ya está en la lista de símbolos, se lanza una excepción
			boolean existe = contiene_nombre(listaParametros, t1.image);
			if (existe) {
				errSem.error("No se puede repetir nombres de variables en la lista de parámetros", t1);
				return;
			}

			switch(tipo) {
			case INT:
				if (t2 != null) { // Es un vector
					tam = Integer.parseInt(t2.image); 
					listaParametros.add(new SymbolArray(t1.image, tam, tipo, clase));	
				} else {
					listaParametros.add(new SymbolInt(t1.image, clase));
				}

				break;

			case CHAR:
				if (t2 != null) { // Es un vector
					tam = Integer.parseInt(t2.image); 
					listaParametros.add(new SymbolArray(t1.image, tam, tipo, clase));
				} else {
					listaParametros.add(new SymbolChar(t1.image, clase));
				}
				break;

			case BOOL:
				if (t2 != null) { // Es un vector
					tam = Integer.parseInt(t2.image);
					listaParametros.add(new SymbolArray(t1.image, tam, tipo, clase));
				} else {
					listaParametros.add(new SymbolBool(t1.image, clase));
				}
				break;
			
			}
	}

	public void tratar_inst_leer(Token t, Attributes a){
		if (!a.esAsignable || !(a.esChar() || a.baseType != Symbol.Types.INT ||
		     a.baseType != Symbol.Types.CHAR || a.esInt())) {
			errSem.error("El parámetro " + a + "de get no es un asignable", t);											
		}	
	}

	public void tratar_inst_escribir(Token t, Attributes a) {
		SymbolFunction sf;
		
		if (a.noTieneTipo()) {
			errSem.error("El parámetro " + a + "de put no tiene un tipo definido", t);											
		} else if (a.esProcedimiento()) {
			errSem.error("El parámetro " + a + "de put es un procedimiento", t);	
		} else if (a.esFuncion() ) {
			sf = (SymbolFunction) a.simboloTabla;

			a.type = sf.returnType;
			
			if (sf.returnType != Symbol.Types.CHAR && sf.returnType != Symbol.Types.INT) {
				errSem.error("El parámetro " + a + " de put es una función que no devuelve integer o character", t);	
			}	
		}
	}

	public void tratar_inst_escribir_linea(Token t, ArrayList<Attributes> lista) {
		// La lista puede ser vacía, y es correcto
		SymbolFunction sf;

		for (Attributes a : lista) {
			if (a.noTieneTipo()) {
				errSem.error("El parámetro " + a + "de put_line no tiene un tipo definido", t);											
			} else if (a.esProcedimiento()) {
				errSem.error("El parámetro " + a + "de put_line es un procedimiento", t);	
			} else if (a.esFuncion()) {
				sf = (SymbolFunction) a.simboloTabla;

				a.type = sf.returnType;

				if (sf.returnType != Symbol.Types.CHAR && sf.returnType != Symbol.Types.INT) {
					errSem.error("El parámetro " + a + "de put es una función que no devuelve integer o character", t);	
				}		
			}	
		}
	}

	public Attributes tratar_inst_invoc_proc(Token t, ArrayList<Attributes> lista) {
		try {
			Symbol simbolo;
			ArrayList<Symbol> parametros = new ArrayList<Symbol>();
			Attributes attr;

			// Se obtiene el nombre de la función
			simbolo = st.getSymbol(t.image); // Si no existe, lanza una excepción
			
			// Si el símbolo obtenido no es un procedimiento, no se puede seguir
			if (simbolo.type == Symbol.Types.PROCEDURE){
				parametros = ((SymbolProcedure) simbolo).parList;
			}
			else if (simbolo.type == Symbol.Types.FUNCTION) {
				parametros = ((SymbolFunction) simbolo).parList;
			}
			else {
				errSem.error("Se ha intentado invocar a una variable que no es procedimiento o función", t);	
				return new Attributes();
			}
	
			//Si el número de parámetros de la función no coincide con los invocados
			if (parametros.size() == lista.size()) {
				for (int i = 0; i < lista.size(); i++){

					if (lista.get(i).type == Symbol.Types.FUNCTION ) {
						SymbolFunction sf = (SymbolFunction) lista.get(i).simboloTabla;
						Attributes a = lista.get(i);
						a.type = sf.returnType;
						lista.set(i, a);
					}

					// Se comprueba que:
					//		El parámetro n coincide en tipo con el del procedimiento
					//		Si el parámetro del procedimiento es por referencia, el parámetro introducido debe ser asignable
					//	Implícitamente, se comprueba que están en el mismo órden
					if ((parametros.get(i).parClass == Symbol.ParameterClass.REF) && !lista.get(i).esAsignable){
						errSem.error("El parámetro "+ parametros.get(i) +" del procedimiento o función es por referencia, y el introducido, "+lista.get(i)+", no es asignable", t);
						return new Attributes();
					} else if ( parametros.get(i).type != lista.get(i).type) {
						errSem.error("El parámetro "+ parametros.get(i) +" del procedimiento o función no coincide en tipo con su correspondiente, "+lista.get(i), t);
						return new Attributes(); 
					}
	
					// Comprobar que, si el parámetro es un array, son compatibles (mismas dimensiones)
					if (lista.get(i).type == Symbol.Types.ARRAY) {
						SymbolArray paramOriginal = (SymbolArray) parametros.get(i);
						
						if (paramOriginal.minInd != lista.get(i).minInd || paramOriginal.maxInd != lista.get(i).maxInd) {
							errSem.error( "El vector introducido por parámetro no tiene las mismas dimensiones, " +
										  "\nDimensiones del parámetro original:"+paramOriginal.minInd+"-"+paramOriginal.maxInd +
										  "\nDimensiones del parámetro introducido:"+lista.get(i).minInd+"-"+lista.get(i).maxInd, t);
						}
					}
				}
			}	
			else {
				errSem.error("El número de parámetros no coincide con los de la lista de params", t);
				return new Attributes();
			}
	
			if (simbolo.type == Symbol.Types.FUNCTION) {
				if (((SymbolFunction) simbolo).returnType == Symbol.Types.INT) {
					attr = new Attributes();
					attr.type = Symbol.Types.FUNCTION;
					attr.setNombre(t.image);
					attr.simboloTabla = simbolo;
					return attr;
				}
				else if (((SymbolFunction) simbolo).returnType == Symbol.Types.BOOL) {
					attr = new Attributes();
					attr.type = Symbol.Types.FUNCTION;
					attr.setNombre(t.image);
					attr.simboloTabla = simbolo;
					return attr;
				} 
				else if (((SymbolFunction) simbolo).returnType == Symbol.Types.CHAR) 
				{
					attr = new Attributes();
					attr.type = Symbol.Types.FUNCTION;
					attr.setNombre(t.image);
					attr.simboloTabla = simbolo;
					return attr;
				} 
				else {
					errSem.error("El valor devuelto por la función no es entero, booleano o carácter. Tipo devuelto: " + ((SymbolFunction) simbolo).returnType, t);
					return new Attributes();
				}
			} else { // Es un procedimiento
				attr = new Attributes();
				attr.type = Symbol.Types.PROCEDURE;
				attr.setNombre(t.image);
				attr.simboloTabla = simbolo;
				return attr; 
			}
		} catch (SymbolNotFoundException e) {
			if (t.image.equals(nombre_proc_principal)) { // Ha fallado la búsqueda en la tabla de símbolos, y se ha intentado llamar al procedimiento principal
				errSem.error("No se puede invocar al procedimiento principal", t);
			} else {
				errSem.deteccion(e, t);
			}
			
			return new Attributes();	
		}
		
	}

	public void tratar_inst_asignacion(Token t, Attributes a1, Attributes a2){
		// Tipos diferentes, sin ser el primero array
		if (a2.type == Symbol.Types.FUNCTION ) {
			SymbolFunction sf2 = (SymbolFunction) a2.simboloTabla;
			a2.type = sf2.returnType;
		}

		if (a1.type != a2.type && (a1.type == Symbol.Types.FUNCTION || a1.type == Symbol.Types.PROCEDURE) && a1.type != Symbol.Types.ARRAY) {
			errSem.error("La asignación a funciones o procedimientos no está permitida", t);
		} else if (a1.type != a2.type && a1.type != Symbol.Types.ARRAY ) {
			errSem.error("Los tipos de la variable y de la expresión no coinciden"+
			"\nVariable a la izquierda: "+a1+
			"\nVariable a la derecha: "+a2, t);
		}

		// Tipos iguales, y son arrays, que no se permite
		else if (a1.type == a2.type && a1.type == Symbol.Types.ARRAY){
			errSem.error("La asignación de vectores por completo no está permitda", t);
		}
		else if (a1.type == a2.type && (a1.type == Symbol.Types.FUNCTION || a1.type == Symbol.Types.PROCEDURE)){
			errSem.error("La asignación a funciones o procedimientos no está permitida", t);
		}
		// el asignable es un array
		else if (a1.type == Symbol.Types.ARRAY) {

			// si a1 es type array -> que los tipos de componentes y tipo de a2 sean iguales
			if (a1.baseType == Symbol.Types.UNDEFINED) {
				errSem.error("Se ha intentado asignar una variable de tipo " + 
				a2.type +
				"\n a un vector sin indexar ninguna componente", t);
			}
			else if (a1.baseType != a2.type) {
				errSem.error("La variable que se ha intentado asignar es de tipo " + 
				a2.type +
				"\n tiene diferente tipo base que el array, que es " +
				a1.baseType, t);
			}	
		} 
	}

	public Attributes tratar_asignable(Token t, Attributes a) {
		try {
			Symbol simbolo = st.getSymbol(t.image); // Si no existe, lanza una excepción
					
			if (a != null && simbolo.type == Symbol.Types.ARRAY) { // Es un indexado de vector
				// Hay que comprobar que la variable referenciada es un vector
				Attributes attr = new Attributes(simbolo);
				attr.setAsignable();
				attr.setNombre(t.image);
				attr.simboloTabla = simbolo;
				
				SymbolArray s = (SymbolArray) simbolo;
	
				attr.setBaseType(s.baseType);
				return attr;
			}
			else if (a == null) { // No es un indexado de vector
				Attributes attr = new Attributes(simbolo);
				attr.setAsignable();
				attr.simboloTabla = simbolo;

				return attr;
			}
			else {
				errSem.error("La variable referenciada, "+t.image+", no es un vector", t);
				return new Attributes();
			}
		} catch (SymbolNotFoundException e) {
			if (t.image.equals(nombre_proc_principal)) { // Ha fallado la búsqueda en la tabla de símbolos, y se ha intentado asignar al procedimiento principal
				errSem.error("No se puede modificar el procedimiento principal", t);
			} else {
				errSem.deteccion(e, t);
			}
			return new Attributes();
		}
	}

	public void tratar_asignable_2(Token t, Attributes a) {
		if (a.type == Symbol.Types.FUNCTION) {
			SymbolFunction sf = (SymbolFunction) a.simboloTabla;
			a.type = sf.returnType;
		}

		if (!a.esInt()) {
			errSem.error("Se ha intentado indexar "+t.image+" con una expresión no numérica", t);
		}	
	}	

	public void tratar_inst_seleccion(Token t, Attributes a) {
		if (a.type == Symbol.Types.FUNCTION) {
			SymbolFunction sf = (SymbolFunction) a.simboloTabla;
			a.type = sf.returnType;
		}

		if (!a.esBool()) {
			errSem.error("La condición de un if tiene que ser de tipo booleana", t);
		}
	}

	public Attributes tratar_expresion(Attributes a1, Attributes a2){
		if (a2 != null) {
			// comprobar los tipos
			if (a1.sonDelMismoTipo(a2) && !a1.esString()) {
				// Construye y devuelve un nuevo Attributes booleano, no asignable por defecto
				// (La expresión resultante ha dejado de ser asignable al componerse con otra)
				return new Attributes(true);
			}
			else {
				if (a1.esString()){
					errSem.error("Las expresión de la izquierda es un string"+
					"\nExpresión de la izquierda: " + a1 +
					"\nExpresión de la derecha: " + a2);
				}
				else if (!a1.sonDelMismoTipo(a2)){
					errSem.error("Las expresiones son de diferente tipo. "+
					"\nExpresión de la izquierda: " + a1 +
					"\nExpresión de la derecha: " + a2);
				}
				return a1;
			}
		}  
		return a1; // Si era asignable, sigue siéndolo
	}

	public void tratar_expresion_simple(Attributes a1, Attributes a2, boolean esOperadorAritmetico){
		// Comprobamos los tipos
		
		if (a1.sonDelMismoTipo(a2))  {
			SymbolFunction sf1 = null;
			SymbolFunction sf2 = null;
			if (a1.type == Symbol.Types.FUNCTION ) {
				sf1 = (SymbolFunction) a1.simboloTabla;
				a1.type = sf1.returnType;
			}
	
			if (a2.type == Symbol.Types.FUNCTION ) {
				sf2 = (SymbolFunction) a2.simboloTabla;
				a2.type = sf2.returnType;
			}
		
			if ((esOperadorAritmetico && a1.esInt()) || (!esOperadorAritmetico && a1.esBool())) {
				a1.unsetAsignable(); // La expresión simple resultante ha dejado de ser asignable al componerse con otra
			} else {
				String mensaje = (esOperadorAritmetico) ? "sí" : "no";
				errSem.error("EXPSIMPLE: El operador es incompatible para los tipos dados"+
				"\nExpresión de la izquierda: " + a1 +
				"\nExpresión de la derecha: " + a2 +
				"\nOperador aritmético: " + mensaje);
			}
		} else {
			if (a1.esString()){
				errSem.error("Al menos una de las expresiones es string");
			}
			else if (!a1.sonDelMismoTipo(a2)){
				errSem.error("Las expresiones son de diferente tipo. "+
					"\nExpresión de la izquierda: " + a1 +
					"\nExpresión de la derecha: " + a2);
			}
		}
	}

	public void tratar_termino(Attributes a1, Attributes a2, boolean esOperadorAritmetico){
		// comprobar los tipos
		SymbolFunction sf1 = null;
		SymbolFunction sf2 = null;
		if (a1.type == Symbol.Types.FUNCTION ) {
			sf1 = (SymbolFunction) a1.simboloTabla;
			a1.type = sf1.returnType;
		}

		if (a2.type == Symbol.Types.FUNCTION ) {
			sf2 = (SymbolFunction) a2.simboloTabla;
			a2.type = sf2.returnType;
		}

		if (a1.sonDelMismoTipo(a2)) {
			if ((esOperadorAritmetico && a1.esInt()) || (!esOperadorAritmetico && a1.esBool())) {
				a1.unsetAsignable(); // El factor resultante ha dejado de ser asignable al componerse con otro factor	
			} else {
				String mensaje = (esOperadorAritmetico) ? "sí" : "no";
				errSem.error("TRATAR_TERMINO: El operador es incompatible para los tipos dados"+
				"\nExpresión de la izquierda: " + a1 +
				"\nExpresión de la derecha: " + a2 +
				"\nOperador aritmético: " + mensaje);
			}
		} else {
			if (a1.esString()){
				errSem.error("Al menos una de las expresiones es string");
			}
			else if (!a1.sonDelMismoTipo(a2)){
				errSem.error("Las expresiones son de diferente tipo. "+
					"\nExpresión de la izquierda: " + a1 +
					"\nExpresión de la derecha: " + a2);
			}		
		}
	}

	public Attributes tratar_factor_1(Attributes a){
		if (a.type == Symbol.Types.FUNCTION) {
			SymbolFunction sf = (SymbolFunction) a.simboloTabla;
			a.type = sf.returnType;
		}

		if (!a.esBool()) {
			errSem.error("Sólo se pueden negar valores booleanos");
		}
		a.tipoConvertido = true;
		return a;
	}
	
	public Attributes tratar_factor_2(Attributes a){
		if (a == null) {
			errSem.error("No se puede invocar a int2char sin parámetros");
			return new Attributes('a');
		} else {
			if (a.type == Symbol.Types.FUNCTION ) {
				SymbolFunction sf = (SymbolFunction) a.simboloTabla;
				a.type = sf.returnType;
			}
			
			if (!a.esInt()) errSem.error("No se puede invocar a int2char con un valor que no sea de tipo integer");
		}

		a.type = Symbol.Types.CHAR;
		a.tipoConvertido = true;
		return a;
	}

	public Attributes tratar_factor_3 (Attributes a) {
		
		if (a == null) {
			errSem.error("No se puede invocar a char2int sin parámetros");
			return new Attributes(-1);
		}
		else{
			if (a.type == Symbol.Types.FUNCTION ) {
				SymbolFunction sf = (SymbolFunction) a.simboloTabla;
				a.type = sf.returnType;
			}
			
			if (!a.esChar()) {
				errSem.error("Solo se pueden usar variables de tipo char en char2int"); 
				return new Attributes(-1);
			}
		}

		a.type = Symbol.Types.INT;
		a.tipoConvertido = true;
		return a;
	}
		
	public Attributes tratar_factor_4 (Token t, Attributes a) {
		Symbol simbolo = null;
		Attributes attr = null;

		if (a.type == Symbol.Types.FUNCTION ) {
			SymbolFunction sf = (SymbolFunction) a.simboloTabla;
			a.type = sf.returnType;
		}
		
		try {	
			simbolo = st.getSymbol(t.image);
			
			// La expresión debe ser un entero, y el tipo base un array
			if (!a.esInt() || simbolo.type != Symbol.Types.ARRAY) {
				errSem.error("Se ha intentado indexar un array con una expresión no entera", t);
				return a;
			} else {
				// Se devuelve el tipo base del vector
				attr = new Attributes(simbolo);	
				attr.setAsignable(); // Las componentes de un vector son asignables
				attr.type = ((SymbolArray) simbolo).baseType; // Se pone como tipo el tipo base del array subyacente
				attr.setNombre(t.image);
				attr.simboloTabla = (SymbolArray) simbolo;
				attr.b = a.b; // Se añade el bloque de código de la expresión
				return attr;
			}
		} catch (SymbolNotFoundException e) {
			errSem.error("La variable " + t.image + " no está declarada en el bloque actual"); 
			return a;
		}
	}
	
	public Attributes tratar_factor_5(Token t){
		Symbol simbolo = null;
		try{
			simbolo = st.getSymbol(t.image);
			Attributes attr = new Attributes(simbolo);
			// Se marca como que es asignable
			attr.setAsignable();
			attr.setNombre(t.image);
			attr.simboloTabla = simbolo;

			// Si es un array, almacenar su dimensión y tipo base
			if (attr.type == Symbol.Types.ARRAY) {
				SymbolArray sa =  (SymbolArray) simbolo;
				attr.minInd = sa.minInd;
				attr.maxInd = sa.maxInd;
				attr.baseType = sa.baseType;
			}

			return attr;
		} catch (SymbolNotFoundException e) {
			errSem.deteccion(e,t); 
			return new Attributes(simbolo);
		}
	}
	
	public void tratar_inst_iteracion(Token t,Attributes a){
		if (a.type == Symbol.Types.FUNCTION ) {
			SymbolFunction sf = (SymbolFunction) a.simboloTabla;
			a.type = sf.returnType;
		}

		if (!a.esBool()) {
			errSem.error("La condición de un while tiene que ser de tipo booleana",t); 
		}
	}

	public void tratar_inst_return(Token t, Attributes a){
		if (pilaLlamadas.empty()) {
			errSem.error("No se puede hacer return en el procedimiento principal", t);
		} else if (pilaLlamadas.peek().type == Symbol.Types.PROCEDURE) {
			errSem.error("Se ha intentado devolver un valor en un procedimiento, y no en una función",t);

		} else if(a.type == Symbol.Types.FUNCTION) {
			SymbolFunction sf = (SymbolFunction) a.simboloTabla;

			if( ((SymbolFunction) pilaLlamadas.peek()).returnType != sf.returnType) {
				errSem.error("El valor de retorno de la función usada en el retorno (" + sf.returnType + 
							 ") no coincide con el de la función invocada: "+((SymbolFunction) pilaLlamadas.peek()).returnType,t);
			}
		} else if(a.type == Symbol.Types.ARRAY ) {
			if( ((SymbolFunction) pilaLlamadas.peek()).returnType != a.baseType) {
				errSem.error("El valor de retorno de la componente de array usada en el retorno (" + a.baseType + 
							 ") no coincide con el de la función invocada: "+((SymbolFunction) pilaLlamadas.peek()).returnType,t);
			}
		} else if (a.type != ((SymbolFunction) pilaLlamadas.peek()).returnType) {
			errSem.error("El valor de retorno (" + a + ")no coincide con el de la función: "+((SymbolFunction) pilaLlamadas.peek()).returnType,t);
		}
		
		
	}
	
	public void imprimirErrores() {
		System.out.println(errSem);
	}
}

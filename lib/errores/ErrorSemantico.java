//*****************************************************************
// Tratamiento de errores sintácticos
//
// Fichero:    ErrorSintactico.java
// Fecha:      03/03/2022
// Versión:    v1.0
// Asignatura: Procesadores de Lenguajes, curso 2021-2022
//*****************************************************************

package lib.errores;

import traductor.Token;
import lib.attributes.*;

import lib.symbolTable.exceptions.*; 

public class ErrorSemantico {
	final static String sep = "*******************************************************************************************";

	public static int contadorErrores = 0;
	private static int contadorWarnings = 0;

	public static void deteccion(AlreadyDefinedSymbolException e, Token t) {
		contadorErrores++;
		System.err.println(sep);
		System.err.println("ERROR SEMÁNTICO (" + t.beginLine + "," + t.beginColumn + "): " +
				"Símbolo: '" + t.image + "'. No se puede redefinir el símbolo");
		System.err.println(sep);
	}

	public static void deteccion(SymbolNotFoundException e, Token t) {
		contadorErrores++;
		System.err.println(sep);
		System.err.println("ERROR SEMÁNTICO (" + t.beginLine + "," + t.beginColumn + "): " +
				"Símbolo: '" + t.image + "'. El símbolo no está definido");
		System.err.println(sep);
	}

	public static void deteccion(String mensaje, Token t) {
		contadorErrores++;
		System.err.println(sep);
		System.err.println("ERROR SEMÁNTICO (" + t.beginLine + "," + t.beginColumn + "): " +
				"Símbolo: '" + t.image + "'. " + mensaje);
		System.err.println(sep);
	}

	public static void warning(String mensaje, Token t) {
		contadorWarnings++;
		System.err.println(sep);
		System.err.println("WARNING: (" + t.beginLine + "," + t.beginColumn + "): " +
				"Símbolo: '" + t.image + "'. " + mensaje);
		System.err.println(sep);
	}

	public static void error(String mensaje, Token t) {
		contadorErrores++;
		System.err.println(sep);
		System.err.println("ERROR: (" + t.beginLine + "," + t.beginColumn + "): " +
				"Símbolo: '" + t.image + "'. " + mensaje);
		System.err.println(sep);
	}

	public static void error(String mensaje) {
		contadorErrores++;
		System.err.println(sep);
		System.err.println("ERROR." + mensaje);
		System.err.println(sep);
	}

	public static int getContadorErrores() { // Lo sentimos (se ha expuesto como public)
		return contadorErrores;
	}

	public String toString() {
		String mensaje;
		if (contadorErrores > 0) {
			mensaje = "Ha habido un total de " + contadorErrores + " errores y " + contadorWarnings + " advertencias.";
		} else {
			mensaje = "Se ha compilado el programa sin errores";
			mensaje += (contadorWarnings == 0)? " y ninguna advertencia" : "";
			mensaje += (contadorWarnings == 1)? " y "+contadorWarnings + " advertencia" : "";
			mensaje += (contadorWarnings > 1)? " y "+contadorWarnings + " advertencias." : ".";
		}
		return mensaje;
	}
}

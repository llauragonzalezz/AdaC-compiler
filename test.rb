require 'open3'

ficheros_prueba = Dir.entries("test")

# Para cada fichero adac en el directorio de prueba, ejecuta el analizador léxico y
# almacena su salida en la carpeta de resultados bajo el nombre nombre_fichero.txt. 
# Si encuentra errores (stederr no nula) lo almacena en nombre_fichero_error.txt
ficheros_prueba.each do |f|
    if f != "." && f != ".." && f != "resultados" && f != "viejos" && f != "salidas_compilador_referencia" && f != "programas_de_prueba"
        puts "Compilando fichero: #{f}"
        
        stdin, stdout, stderr, wait_thr = Open3.popen3('java -jar ./dist/adac_4.jar ./test/' + f)
        salidaerr = stderr.read
        stderr.close

        Open3.popen3('mv ./test/*.pcode ./test/resultados/')
        
        if ! salidaerr.empty?
            puts "Ha habido errores al compilar:"
            puts salidaerr
        elsif
            puts "Sin errores"
        end  
    end
end

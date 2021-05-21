package ast.servicio.probatch.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestLeerArchivo {

	public static List<String> leerBytesArchivo(String fileName, int kBytesALeer) {
		List<String> listaBytesLeidos = new ArrayList<String>();
		int bytesALeer = kBytesALeer * 1024;
		 int tamañoArchivo;
		 int resto;
		 String ultimoStringLista;
		 String ultimoStringListaModificado;
		 
		try {
			FileInputStream fileInput = new FileInputStream(fileName);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInput);
			 tamañoArchivo = bufferedInputStream.available();
			 resto = tamañoArchivo% bytesALeer;
			byte[] bytes = new byte[bytesALeer];
						
			while (bufferedInputStream.read(bytes) != -1) {
				listaBytesLeidos.add(new String(bytes));
			}

			ultimoStringLista = listaBytesLeidos.get(listaBytesLeidos.size()-1);
			ultimoStringListaModificado = ultimoStringLista.substring(0, resto);
			
			listaBytesLeidos.set(listaBytesLeidos.size()-1, ultimoStringListaModificado);
			
			
			bufferedInputStream.close();

		} catch (FileNotFoundException e) {
			// la excepción provendria de no encontrar el archivo especificado
			System.err.println("FileStreamsTest: " + e);
		} catch (IOException e) {
			System.err.println("FileStreamsTest: " + e);
		}
		return listaBytesLeidos;
	}
}

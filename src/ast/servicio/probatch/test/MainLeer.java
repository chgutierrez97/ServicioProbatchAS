package ast.servicio.probatch.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ast.servicio.probatch.util.Utils;

public class MainLeer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		List<String> l = TestLeerArchivo.leerBytesArchivo("C:/Users/rodrigo.guillet/Desktop/p.pdf", 8192);
//		
//		
//		File file = new File("C:/Users/rodrigo.guillet/Desktop/destino.txt");
//		FileWriter fileWriter = new FileWriter(file);
//		
//		
//		fileWriter.write("PRIMERA LINEA");
//		fileWriter.write(l.get(0));
////		fileWriter.write("SEGUNDA LINEA");
////		fileWriter.write(l.get(1));
//		fileWriter.close();
//		System.out.println(l.get(0));
//	}
	
		String s="";
		System.out.println(Utils.obtenerUltimoSegmento(s, "/"));
		
		
	}
}

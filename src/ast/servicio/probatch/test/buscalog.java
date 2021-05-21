package ast.servicio.probatch.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class buscalog {
	public static void main(String[] args) {
		String directorio = "C:/Users/rodrigo.guillet/Documents/Java/Workspaces/MyEclipse/ServicioProBatch/batch";
		String idLog = "3398499";
		String fecha = "20120330";
		int limitKByte = 1050;
		int fragmentacion = 1;
		for (String string : buscaLogs(directorio, idLog, fecha, limitKByte, fragmentacion)) {
			System.out.println(string);
		}

	}

	public static List<String> buscaLogs(String directorio, String idLog, String fecha, int limitKByte, int fragmentacion) {
		try {
			return levantaLog(obtenerLogReciente(buscaArchivs(directorio, idLog, fecha)), limitKByte, fragmentacion);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private static File obtenerLogReciente(File logs[]) {
		if (logs == null || logs.length == 0) {
			return null;
		}
		if (logs.length == 1) {
			return logs[0];
		}

		String[] horas = new String[logs.length];
		for (int i = 0; i < horas.length; i++) {
			horas[i] = String.valueOf(i) + logs[i].getName();
		}

		Arrays.sort(horas, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return Integer.valueOf(s2.substring(s2.lastIndexOf("-") + 1, s2.length() - 4)).compareTo(
						Integer.valueOf(s1.substring(s1.lastIndexOf("-") + 1, s1.length() - 4)));
			}
		});

		return logs[Character.getNumericValue((horas[0].charAt(0)))];
	}

	/**
	 * Busca en un directoro determinado, todos los logs que coincidan con un id
	 * y fecha determinados. Devuelve un array de Archivos con los archivos
	 * encontrados que coincidan
	 * 
	 * @param dir
	 * @param id
	 * @param fecha
	 * @return
	 */

	private static File[] buscaArchivs(String dir, final String id, final String fecha) {
		File directorio = new File(dir);

		FilenameFilter filtro = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains("-" + id + "-" + fecha);
			}
		};

		File lista[] = directorio.listFiles(filtro);

		return lista;
	}

	/**
	 * Devuelve una lista de fragmentos (Strings) con el contenido del log,
	 * limitado segun la cantidad de kilobytes asignados. Cada linea esta
	 * separada por un '\n'. Si no se puede leer el archivo, devuelve NULL
	 * 
	 * @param log
	 * @param tamanioArchivo
	 * @param tamanioFragmento
	 * @return
	 * @throws IOException
	 */
	private static List<String> levantaLog(File log, int tamanioArchivo, int tamanioFragmento) throws IOException {
		if (log.canRead()) {

			String archivoLeido;
			int resto = 0;
			FileInputStream fileInput;
			fileInput = new FileInputStream(log);
			tamanioFragmento *= 1024;
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInput);
			if (tamanioArchivo >= bufferedInputStream.available())
				resto = bufferedInputStream.available() % tamanioArchivo;

			// leo determinada cantidad de bytes
			byte[] bytes = new byte[(tamanioArchivo)];
			bufferedInputStream.read(bytes);
			archivoLeido = new String(bytes);
			if (resto != 0)
				archivoLeido = archivoLeido.substring(0, resto);
			bufferedInputStream.close();

			return separarString(archivoLeido, tamanioFragmento);

		}
		return null;
	}

	private static List<String> separarString(String cadSeparar, int bytes) {
		List<String> listaResultado = new ArrayList<String>();
		int contador;

		for (contador = 0; contador + bytes <= cadSeparar.length(); contador = contador + bytes) {
			listaResultado.add(cadSeparar.substring(contador, contador + bytes));
		}
		listaResultado.add(cadSeparar.substring(contador, cadSeparar.length()));

		return listaResultado;
	}

	// private static List<String> levantaLog(File log, int tamanioArchivo, int
	// tamanioFragmento) throws IOException {
	//
	// if (log.canRead()) {
	//
	// List<String> listaFragmentos = new ArrayList<String>();
	// String ultimoStringListaModificado;
	//
	// FileInputStream fileInput;
	// fileInput = new FileInputStream(log);
	// tamanioFragmento *= 1024;
	// BufferedInputStream bufferedInputStream = new
	// BufferedInputStream(fileInput,5);
	// int resto = bufferedInputStream.available() % (tamanioFragmento);
	// System.out.println(bufferedInputStream.available());
	// byte[] bytes = new byte[(tamanioFragmento)];
	//			
	// while (bufferedInputStream.read(bytes) != -1) {
	// listaFragmentos.add(new String(bytes));
	// }
	// ultimoStringListaModificado = listaFragmentos.get(listaFragmentos.size()
	// - 1).substring(0, resto);
	//
	// listaFragmentos.set(listaFragmentos.size() - 1,
	// ultimoStringListaModificado);
	//
	// bufferedInputStream.close();
	// return listaFragmentos;
	//
	// }
	// return null;
	// }

}

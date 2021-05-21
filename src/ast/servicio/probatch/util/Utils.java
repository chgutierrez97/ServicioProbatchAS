package ast.servicio.probatch.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ch.qos.logback.core.db.dialect.MySQLDialect;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFile;

import ast.servicio.probatch.connection.Conexion;
import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.message.MensajeError;
import ast.servicio.probatch.message.MensajeValidacion;
import ast.servicio.probatch.os.service.OsServiceFactory;

public class Utils {
	public static Logger logger = LoggerFactory.getLogger(MensajeValidacion.class);

	/**
	 * Consturctor que genera el xml basado en el mensaje de entrada, valida
	 * ademas que el mensaje sea valido.
	 * 
	 * @param xmlMessage
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws MensajeError
	 */
	public static Document parsearMensaje(String xmlMessage) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		Document tramaXml = builder.parse(new InputSource(new StringReader(xmlMessage)));
		return tramaXml;

	}

	public static boolean esNumerico(String input) { // devuelve true si el
		// String Input consiste
		// solo de numeros
		if (input.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Xor para desencriptar o encriptar clave
	 * 
	 * @param key
	 *            - Clave en arch de config
	 * @param id
	 *            - id a concatenar
	 * @param arg
	 *            - Texto encriptado o a encriptar
	 * @return
	 */
	public static byte[] xorstr(String key, String id, String arg) {

		byte[] resultado = null;
		byte[] decryptArg = hex2Byte(arg);
		byte messageDigest[];
		byte[] s;

		String key_id = key + id;

		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(key_id.getBytes());

			messageDigest = algorithm.digest();

			int la = decryptArg.length;
			int lk = messageDigest.length;

			int tamS = (messageDigest.length * (la / lk)) + (la % lk);
			s = new byte[tamS];

			int base = 0;
			for (int i = 0; i < (la / lk); i++) {
				base = messageDigest.length * i;
				for (int j = 0; j < messageDigest.length; j++)
					s[base + j] = messageDigest[j];

			}

			base = tamS - (la % lk);
			for (int j = 0; j < (la % lk); j++)
				s[base + j] = messageDigest[j];

			int minimo = Math.min(decryptArg.length, s.length);

			resultado = new byte[minimo];

			for (int i = 0; i < minimo; i++) {
				resultado[i] = (byte) (decryptArg[i] ^ s[i]);
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			System.exit(5);
		}

		return resultado;
	}

	// Convert Byte Arrary to Hex String
	public static String byte2hex(byte[] b) {
		// String Buffer can be used instead
		String hs = "";
		String stmp = "";

		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));

			if (stmp.length() == 1) {
				hs = hs + "%" + "0" + stmp;
			} else {
				hs = hs + "%" + stmp;
			}

			if (n < b.length - 1) {
				hs = hs + "";
			}
		}

		return hs;
	}

	public static byte[] hex2Byte(String str) {
		String str1 = str.replaceAll("%", "");
		byte[] bytes = new byte[str1.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(str1.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

	/**
	 * Obtiene todos los segmentos que estan separados por el delim y los guarda
	 * en una lista. Por ejemplo: "string1/string2/string3/..../stringN" el
	 * metodo arma una lista de n elementos (del tipo String) y cada uno de sus
	 * elementos va a ser (0:string1,1:string2,2:string3,.....n-1:stringN)
	 * 
	 * @param cadena
	 * @param delim
	 * @return
	 */
	public static List<String> obtenerCadenas(String cadena, String delim) {
		List<String> lista = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(cadena, delim);
		while (tokens.hasMoreElements()) {
			lista.add(tokens.nextToken().trim());
		}
		return lista;

	}

	/**
	 * ejemplo: "hola/chau/chau2", usando el delimitador "/" se obtiene la
	 * cadena segunda: "chau"
	 * 
	 * @param cadena
	 * @param delim
	 * @return
	 */
	public static String obtenerSegundaCad(String cadena, String delim) {
		StringTokenizer tokens = new StringTokenizer(cadena, delim);
		String cadDer = null;
		tokens.nextToken();
		if (tokens.hasMoreElements())
			cadDer = tokens.nextToken();
		return cadDer;

	}

	/**
	 * ejemplo: "hola/chau2/chau3", usando el delimitador "/" se obtiene la
	 * cadena tercera: "chau3"
	 * 
	 * @param cadena
	 * @param delim
	 * @return
	 */
	public static String obtenerTerceraCad(String cadena, String delim) {

		StringTokenizer tokens = new StringTokenizer(cadena, delim);
		String terceraCadena = null;
		tokens.nextToken();
		if (tokens.hasMoreElements())
			tokens.nextToken();
		if (tokens.hasMoreElements())
			terceraCadena = tokens.nextToken();
		return terceraCadena;

	}

	/**
	 * ejemplo: "hola/chau2/chau3/chau4", usando el delimitador "/" se obtiene
	 * la cadena tercera: "chau4"
	 * 
	 * @param cadena
	 * @param delim
	 * @return
	 */
	public static String obtenerCuartaCad(String cadena, String delim) {

		StringTokenizer tokens = new StringTokenizer(cadena, delim);
		String cuartaCadena = null;
		tokens.nextToken();
		if (tokens.hasMoreElements())
			tokens.nextToken();
		if (tokens.hasMoreElements())
			tokens.nextToken();
		if (tokens.hasMoreElements())
			cuartaCadena = tokens.nextToken();
		return cuartaCadena;

	}

	/**
	 * valida existencia de un archivo
	 * 
	 * @param nombreArchivo
	 * @return
	 * @throws IOException
	 */
	public static boolean validarExistenciaArchivo(String nombreArchivo) {
		if (OsServiceFactory.isAS400()) {

			AS400 conexion = new AS400();
			try {
				IFSFile as400File = new IFSFile(conexion, nombreArchivo);

				
				if (as400File.exists())
					return true;
				return false;
			} catch (Exception e) {
				logger.debug(new MensajeError(e.getMessage()).getTramaString());
				return false;
			} finally {
				if (conexion != null) {
					conexion.disconnectAllServices();
				}
			}
		} else {
			File file = new File(nombreArchivo);
			if (file.exists()) {
				return true;
			}
			return false;
		}
	}

	/**
	 * valida existencia de un archivo en AS400
	 * 
	 * @param nombreArchivo
	 * @return boolean
	 */
	public static boolean validarExistenciaArchivoAS400(String nombreArchivo) {

		// logger.debug("*** Inicio del metodo validarExistenciaArchivoAS400 ********");
		boolean existeArchivo = false;
		AS400 myAS400 = null;
		try {

			myAS400 = new AS400();
			// logger.debug("*** Inicialización de conexion con el as400********");

			if (nombreArchivo.contains("QSYS.LIB") || nombreArchivo.contains("QDLS")) {

				existeArchivo = true;

			} else {
				IFSFile archivoAS400 = new IFSFile(myAS400, nombreArchivo);

				existeArchivo = archivoAS400.exists();
				// logger.debug("*** existe archivo : " + existeArchivo);

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			myAS400.disconnectAllServices();
		}

		return existeArchivo;
	}

	/**
	 * Obtiene un parametro (nombreParametro) de una determinada tramaXML
	 * (trama). Por ejemplo si deseo obtener el parametro "desde" que esta
	 * dentro de la trama; "<estado desde="1" hasta="3"/>" el resultado del
	 * metodo va a ser un String que tiene como valor "1"
	 * 
	 * @param trama
	 * @param nombreParametro
	 * @return
	 */
	public static String obtenerParametroTramaString(String trama, String nombreParametro) {
		String resultado = null;
		if (trama.contains(nombreParametro)) {
			resultado = trama.substring(trama.indexOf(nombreParametro));
			resultado = resultado.substring(resultado.indexOf('"') + 1, resultado.indexOf('"', resultado.indexOf('"') + 1));
		}
		return resultado;

	}

	/**
	 * Este metodo arma un String con todos los archivos no encontrados despues
	 * de validar la existencia de cada uno de ellos. Los arma separadaos por
	 * ";", de esta manera:
	 * archivo_no_encontrado_1;archivo_no_encontrado_2;....;
	 * archivo_no_encontrado_N
	 * 
	 * @param cadena
	 * @return
	 * @throws IOException
	 */
	public static String archivosNoEncontrados(String cadena) {
		String resultado = "";
		List<String> lista = obtenerCadenas(cadena, ";");
		for (String texto : lista) {
			texto = texto.replace("%d", Utils.seccionaFecha("dd"));
			texto = texto.replace("%m", Utils.seccionaFecha("MM"));
			texto = texto.replace("%y", Utils.seccionaFecha("yy"));
			texto = texto.replace("%Y", Utils.seccionaFecha("yyyy"));
			if (texto.contains("*")) {
				texto = OsServiceFactory.getOsService().reemplazaExpRegArchivo(texto);
			}
			if (!validarExistenciaArchivo(texto))
				resultado = resultado + texto + ";";
		}
		if (resultado.endsWith(";")) {
			resultado = resultado.substring(0, resultado.length() - 1);
		}
		return resultado;
	}

	/**
	 * ejemplo: "hola/chau", usando el delimitador "/" se obtiene la cadena
	 * izquierda: hola
	 * 
	 * @param cadena
	 * @param delim
	 * @return
	 */
	public static String obtenerCadIzquierda(String cadena, String delim) {
		StringTokenizer tokens = new StringTokenizer(cadena, delim);
		String cadIzq = tokens.nextToken();
		return cadIzq;
	}

	/**
	 * ejemplo: "hola/chau", usando el delimitador "/" se obtiene la cadena
	 * derecha: chau
	 * 
	 * @param cadena
	 * @param delim
	 * @return
	 */
	public static String obtenerCadDerecha(String cadena, String delim) {
		String cadDer = null;
		StringTokenizer tokens = new StringTokenizer(cadena, delim);
		tokens.nextToken();
		if (tokens.hasMoreElements())
			cadDer = tokens.nextToken();
		return cadDer;
	}

	/**
	 * ejemplo: "string1/string2/..../stringN", usando el delimitador "/" se
	 * obtiene la ultima cadena: stringN
	 * 
	 * @param cadena
	 * @param delim
	 * @return
	 */
	public static String obtenerUltimoSegmento(String cadena, String delim) {
		String ultimaCadena = null;
		StringTokenizer tokens = new StringTokenizer(cadena, delim);
		while (tokens.hasMoreElements())
			ultimaCadena = tokens.nextToken();
		return ultimaCadena;
	}

	public static boolean validaEsxpresionesRegulrares(String cadenaInput, String expresion) {

		Pattern p = Pattern.compile(expresion);
		Matcher m = p.matcher(cadenaInput);
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}

	public static String seccionaFecha(String seccion) {
		Date fechaActual = new Date();

		SimpleDateFormat dateFormat = new SimpleDateFormat(seccion);
		return "" + dateFormat.format(fechaActual);
	}

	public static String ultimoModificado(File[] listaInterfaces) {

		File ultimaInterfaz = listaInterfaces[0];

		for (int i = 0; i < listaInterfaces.length; i++) {
			if (listaInterfaces[i].lastModified() > ultimaInterfaz.lastModified()) {
				ultimaInterfaz = listaInterfaces[i];
			}
		}

		return ultimaInterfaz.toString();
	}

	public static List<EstadoProceso> levantarEstados(String fileName) {
		List<EstadoProceso> estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());
		File file = new File(fileName);
		String leido = "";
		if (file.exists()) {
			try {
				BufferedReader bufferedReader;
				bufferedReader = new BufferedReader(new FileReader(file));
				leido = bufferedReader.readLine();
				bufferedReader.close();

				estadoMensajes = stringEstadosToListaEstados(leido);

			} catch (IOException e) {
				logger.error("Error al leer el archivo de estados guardado: " + e.getMessage());
			}
		}

		return estadoMensajes;
	}

	public static List<EstadoProceso> stringEstadosToListaEstados(String stringEstados) {
		EstadoProceso estadoProceso;
		String id;
		String nombre;
		long ts;
		int pid;
		Integer estado;
		String estadoString;
		List<EstadoProceso> estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());

		if (!"".equals(stringEstados) && stringEstados != null) {
			stringEstados = stringEstados.replace("<estado-guardado>", "");
			stringEstados = stringEstados.replace("</estado-guardado>", "");

			stringEstados = stringEstados.replaceAll("</transicion>", "|");

			for (String estadoGuardado : obtenerCadenas(stringEstados, "|")) {
				id = obtenerParametroTramaString(estadoGuardado, "id");
				nombre = obtenerParametroTramaString(estadoGuardado, "nombre");
				ts = new Long(obtenerParametroTramaString(estadoGuardado, "ts"));
				pid = 0;
				estadoString = obtenerParametroTramaString(estadoGuardado, "estado");

				if ("exito".equals(estadoString))
					estado = 0;
				else if ("muerte".equals(estadoString))
					estado = -9999;
				else if ("falla".equals(estadoString))
					estado = 1;
				else
					estado = null;

				estadoProceso = new EstadoProceso(id, nombre, ts, pid, estado);
				estadoProceso.setDump(true);

				estadoMensajes.add(estadoProceso);
			}

		}

		return estadoMensajes;
	}

	public static boolean matar(int pid) {
		try {
			OsServiceFactory.getOsService().executeCommand((OsServiceFactory.getOsService().getKillCommand(pid)));
		} catch (Exception e) {
			logger.error("Error al matar el proceso con pid: " + pid);
			logger.trace(e.getMessage());
			return false;
		}
		return true;
	}

}

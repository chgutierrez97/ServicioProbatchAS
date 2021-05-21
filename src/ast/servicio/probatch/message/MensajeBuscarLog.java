package ast.servicio.probatch.message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.os.service.OsService;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

/**
 * Devuelve los logs de un proceso ejecutado.
 * 
 * @author rodrigo.guillet
 * 
 */
public class MensajeBuscarLog extends Mensaje {
	final String CATEGORIA = "batch";
	public static Logger logger = LoggerFactory.getLogger(MensajeBuscarLog.class);

	public MensajeBuscarLog(String mensajeEntrada) {
		super(mensajeEntrada);
	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {
		int tamanioLog = 0;
		String truncado = "no";
		String mensajeCompleto = "";
		String nombreArchivo = new String();
		String tramaString = this.getTramaString();
		String idLog = Utils.obtenerParametroTramaString(tramaString, "id");
		String ts = Utils.obtenerParametroTramaString(tramaString, "ts");

		if (idLog == null || idLog.equals("")) {
			return MessageFactory.crearMensajeError(null, "Debe especificar un id");
		}

		if (ts == null || ts.equals("")) {
			return MessageFactory.crearMensajeError(null, "Debe especificar ts");
		}

		/*
		 * DEBUG------------------------------------------------------------------
		 */
		// File pbwinDir = new File(ServicioAgente.cfg.getWrkdir() + "/");
		// logger.debug("**************************Elementos dentro de " +
		// pbwinDir.getAbsolutePath() + ":");
		// logger.debug(Arrays.toString(pbwinDir.list()));
		// File batchDir = new File(ServicioAgente.cfg.getWrkdir() + "/" +
		// CATEGORIA);
		// if (batchDir.exists() && batchDir.isDirectory()) {
		// logger.debug("DIRECTORIO batch/ ENCONTRADO Y ES VALIDO!");
		// } else {
		// logger.debug("DIRECTORIO batch/ ES INVALIDO!");
		// }
		// if (Utils.validarExistenciaArchivo(pbwinDir.getAbsolutePath())) {
		// logger.debug("Utils.validarExistenciaArchivo(pbwinDir.getAbsolutePath()) ES TRUE!");
		// } else {
		// logger.debug("Utils.validarExistenciaArchivo(pbwinDir.getAbsolutePath()) ES FALSE!");
		// }
		// if (Utils.validarExistenciaArchivo(batchDir.getAbsolutePath())) {
		// logger.debug("Utils.validarExistenciaArchivo(batchDir.getAbsolutePath()) ES TRUE!");
		// } else {
		// logger.debug("Utils.validarExistenciaArchivo(batchDir.getAbsolutePath()) ES FALSE!");
		// }
		/*---------------------------------------------------------------------------*/

		/*
		 * CAMBIO MARTIN ZARAGOZA
		 * 10/02/2015--------------------------------------
		 * -------------------------------------------
		 */
		// String directorio = ServicioAgente.cfg.getWrkdir() + "/" + CATEGORIA;
		// if (Utils.validarExistenciaArchivo(directorio) == false) {
		// Mensaje mensaje = MessageFactory.crearMensajeError(null,
		// "La carpeta \"" + CATEGORIA + "\" no existe");
		// return mensaje;
		// }

		String wrkdir = ServicioAgente.cfg.getWrkdir();
		File batchDir = (wrkdir.contentEquals(".") || wrkdir.contentEquals("./")) ? new File(CATEGORIA) : new File(wrkdir + "/" + CATEGORIA);
		String directorio = batchDir.getAbsolutePath();
		if (Utils.validarExistenciaArchivo(directorio) == false) {
			Mensaje mensaje = MessageFactory.crearMensajeError(null, "La carpeta \"" + CATEGORIA + "\" no existe");
			return mensaje;
		}

		/* DEGUG---------------------------------------------------- */
		logger.debug("ServicioAgente.cfg.getWrkdir=" + wrkdir);
		String[] s_list = batchDir.list();
		if (s_list == null || s_list.length == 0) {
			logger.debug("El directorio " + batchDir.getAbsolutePath() + " esta vacio!");
		} else {
			logger.debug("El directorio " + batchDir.getAbsolutePath() + " contiene: " + Arrays.toString(s_list));
		}
		/*
		 * FIN DE CAMBIO MARTIN ZARAGOZA
		 * 10/02/2015------------------------------
		 * ---------------------------------------------------
		 */

		int limitKByte = ServicioAgente.cfg.getMax_returned_log_size();
		int fragmentacion = ServicioAgente.cfg.getOutput_maxsize();

		ts = ts.replaceAll("/", "");

		try {

			/*
			 * FIXME : mapLogEncontrado puede devolver null , chequear en dicho
			 * caso e informar.
			 */
			Map<String, List<String>> mapLogEncontrado = buscaLogs(directorio, idLog, ts, limitKByte, fragmentacion, tamanioLog, truncado);
			if (mapLogEncontrado == null || mapLogEncontrado.isEmpty()) {
				logger.error("No se encontraron logs con las caracteristicas: directorio=" + directorio + ", idLog=" + idLog);
				return MessageFactory.crearMensajeError(null, "No hay logs con esas caracteristicas");
			}

			Set<String> keysMap = mapLogEncontrado.keySet();

			for (Iterator<String> iterator = keysMap.iterator(); iterator.hasNext();) {
				nombreArchivo = (String) iterator.next();
			}
			List<String> listaLogsEncontrados = mapLogEncontrado.get(keysMap.iterator().next());

			if (listaLogsEncontrados != null && !listaLogsEncontrados.isEmpty()) {

				for (String stringLog : listaLogsEncontrados) {
					mensajeCompleto = mensajeCompleto + stringLog;
				}

				// mensajeCompleto =
				// OsServiceFactory.getOsService().escapaSaltosDeLinea(mensajeCompleto);
				// mensajeCompleto =
				// StringEscapeUtils.escapeXml(mensajeCompleto).replaceAll("" +
				// (char) 13 + (char) 10, "#x0a;").replaceAll("" + (char) 10,
				// "#x0a;");
				mensajeCompleto = MessageFactory.crearMensajeRespuestaLogs(idLog, nombreArchivo, mensajeCompleto, CATEGORIA, truncado, fragmentacion,
						limitKByte, true).getTramaString();
				osSalida.write(mensajeCompleto.getBytes());
				logger.debug("CLI <-- " + mensajeCompleto);
			} else {
				return MessageFactory.crearMensajeError(null, "No hay logs con esas caracteristicas");
			}
		} catch (IOException e) {
			throw new MensajeErrorException("No se pudo escribir en la salida " + e.getMessage());
		}

		return null;
	}

	/**
	 * 
	 * Devuelve una lista de String con la cadena terminada y lista para listar.
	 * Retorna null en caso de no hallar logs.
	 * 
	 * @param directorio
	 * @param idLog
	 * @param fecha
	 * @param limitKByte
	 * @param fragmentacion
	 * @return
	 * @throws MensajeErrorException
	 */
	public Map<String, List<String>> buscaLogs(String directorio, String idLog, String fecha, int limitKByte, int fragmentacion, int tamanioLog, String truncado)
			throws MensajeErrorException {

		try {
			File[] listFiles = buscaArchivos(directorio, idLog, fecha);
			if (listFiles.length != 0) {
				HashMap<String, List<String>> mapaRespuesta = new HashMap<String, List<String>>();
				File file = obtenerLogReciente(listFiles);
				mapaRespuesta.put(file.getName(), levantaLog(file, limitKByte, fragmentacion, tamanioLog, truncado));
				return mapaRespuesta;
			}
			// logger.error("No se encontro archivo");
			return null;
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.trace(e.getMessage());
			throw new MensajeErrorException("Error al leer el archivo de log");
		}
	}

	/**
	 * Devuelve el ultimo log con respecto a la fecha entre varios archivos de
	 * log.
	 * 
	 * @param logs
	 * @return
	 */
	private File obtenerLogReciente(File logs[]) {
		if (logs == null || logs.length == 0) {
			return null;
		}
		if (logs.length == 1) {
			return logs[0];
		}

		String[] horas = new String[logs.length];
		for (int i = 0; i < horas.length; i++) {
			horas[i] = String.valueOf(i) + "|" + logs[i].getName();
		}

		Arrays.sort(horas, new Comparator<String>() {
			public int compare(String s1, String s2) {

				return (new Long(s2.substring(s2.lastIndexOf("_") + 1, s2.length() - 4)).compareTo(new Long(s1.substring(s1.lastIndexOf("_") + 1,
						s1.length() - 4))));
			}
		});

		return logs[new Integer(Utils.obtenerCadIzquierda(horas[0], "|"))];
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

	private File[] buscaArchivos(String dir, final String id, final String fecha) {
		File directorio = new File(dir);

		FilenameFilter filtro = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains("-" + id + "-" + fecha + "_");
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
	 * @param tamanioMaximoArchivo
	 * @param tamanioFragmento
	 * @return
	 * @throws IOException
	 */
	private List<String> levantaLog(File log, int tamanioMaximoArchivo, int tamanioFragmento, int tamanioLog, String truncado) throws IOException {
		if (log.canRead()) {

			String archivoLeido;
			int resto = 0;
			FileInputStream fileInput;
			fileInput = new FileInputStream(log);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInput);
			tamanioLog = bufferedInputStream.available();

			if (bufferedInputStream.available() <= 0)
				return new ArrayList<String>();

			if (tamanioMaximoArchivo >= bufferedInputStream.available())
				resto = bufferedInputStream.available() % tamanioMaximoArchivo;

			// leo determinada cantidad de bytes
			byte[] bytes = new byte[(tamanioMaximoArchivo)];
			bufferedInputStream.read(bytes);
			archivoLeido = new String(bytes);
			if (resto != 0) {
				archivoLeido = archivoLeido.substring(0, resto);
				truncado = "si";
			}
			bufferedInputStream.close();

			return separarString(archivoLeido, tamanioFragmento);

		}
		return null;
	}

	/**
	 * 
	 * Se encarga de separar una cadena en tantas subcadenas sean necesarias
	 * para lograr cadenas de una determinada cantidad de bytes, colocandolas en
	 * una lista de String
	 * 
	 * @param cadSeparar
	 * @param bytes
	 * @return
	 */
	private List<String> separarString(String cadSeparar, int bytes) {
		List<String> listaResultado = new ArrayList<String>();
		int contador;

		for (contador = 0; contador + bytes <= cadSeparar.length(); contador = contador + bytes) {
			listaResultado.add(cadSeparar.substring(contador, contador + bytes));
		}
		listaResultado.add(cadSeparar.substring(contador, cadSeparar.length()));

		return listaResultado;
	}

}

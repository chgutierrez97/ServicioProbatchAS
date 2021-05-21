package ast.servicio.probatch.threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.connection.OutputWriter;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class ServiceProcessMessage extends Thread {

	public static Logger logger = LoggerFactory.getLogger(EjecutarProceso.class);
	public static Logger loggerProceso;
	InputStream entrada = null;
	OutputWriter output = null;
	String strMensaje = null;

	public ServiceProcessMessage(InputStream entrada, OutputWriter output, String strMensaje) throws IOException {
		this.entrada = entrada;
		this.output = output;
		this.strMensaje = strMensaje;

	}

	@Override
	public void run() {
		logger.info("mensaje a procesar=" + strMensaje);

		List<String> sxmlList = new ArrayList<String>();
		sxmlList = Utils.obtenerCadenas(strMensaje, "" + (char) 13 + (char) 10);

		try {
			for (Iterator<String> iterator = sxmlList.iterator(); iterator.hasNext();) {
				/*
				 * se obtiene un mensaje a procesar y se lo procesa como si
				 * fuese un requerimiento
				 */
				String xmlString = (String) iterator.next();
				Mensaje respuesta = procesarRequerimiento(xmlString, output);

				if (respuesta != null) {
					if (respuesta.getTramaString().length() > ServicioAgente.cfg.getOutput_maxsize()) {
						logger.debug("MENSAJE DE RESPUESTA A ENVIAR ES MUY LARGO! RECORTANDO MENSAJE DE RESPUESTA...");
						
						String msjRecortado = respuesta.getTramaString().substring(0, ServicioAgente.cfg.getOutput_maxsize()) + "\n";
						output.write(msjRecortado.getBytes());
					} else {
						/*aqui mismo se tiene que concatenar \n antes de enviar el mensaje...*/
//						output.write(respuesta.getTramaString().getBytes());
						output.write(respuesta.getTramaString().getBytes());
					}

					output.flush();
				}
			}

		} catch (IOException e) {
			// Logs
			logger.error("El cliente cerro la conexion");
			logger.trace(e.getMessage());
			ServicioAgente.connectionStatus = false;
		} catch (Exception e) {
			// Logs
			logger.error(e.getMessage());
			logger.trace(e.getMessage());
			ServicioAgente.connectionStatus = false;
		}
	}// run

	private Mensaje procesarRequerimiento(String strMensaje, OutputStream osSalida) {
		Mensaje respuesta = null;
		try {
			Mensaje mensajeEntrada = MessageFactory.crearMensaje(strMensaje);
			logger.debug("procesarRequerimiento:procesador de mensaje obtenido es " + mensajeEntrada.getClass().getName());
			respuesta = mensajeEntrada.procesarMensaje(osSalida);
			if (respuesta != null) {
				logger.debug("CLI <-- " + respuesta.getTramaString());
			}
		} catch (MensajeErrorException e) {
			// Logs
			respuesta = MessageFactory.crearMensajeRespuesta(e.getRespuestaError().toString());
			logger.error(e.getRespuestaError().toString());
			logger.trace(e.getMessage());
		}
		return respuesta;
	}
}// ServiceProcessMessage

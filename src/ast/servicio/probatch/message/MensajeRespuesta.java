package ast.servicio.probatch.message;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringEscapeUtils;

import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.service.ServicioAgente;

public class MensajeRespuesta extends Mensaje {

	public MensajeRespuesta(String texto) {
		super(texto);
	}

	public MensajeRespuesta(String tipo, String etiqueta, String id, String nombre, String mensaje, Long ts, boolean escapeSpecialChar) {
		super(generarMensajeRespuesta(tipo, etiqueta, id, nombre, mensaje, ts, escapeSpecialChar));
	}

	public MensajeRespuesta(String id, String nombre, String mensaje, String categoria, String truncado, int cantidad, int maximo, boolean escapeSpecialChar) {
		super(generarMensajeRespuestaLogs(id, nombre, mensaje, categoria, truncado, cantidad, maximo, escapeSpecialChar));
	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) {
		// TODO Auto-generated method stub
		return this;
	}

	/**
	 * Mensaje de error con parametros, todos son opcionales.
	 * 
	 * @param id
	 * @param nombre
	 * @param mensaje
	 * @param interfaces
	 * @return
	 */
	private static String generarMensajeRespuesta(String tipo, String etiqueta, String id, String nombre, String mensaje, Long ts, boolean escapeSpecialChar) {
		if (tipo == null || tipo.length() < 1)
			tipo = "mensaje";
		StringBuffer respuesta = new StringBuffer();
		respuesta.append("<");
		respuesta.append(tipo);
		if (etiqueta != null && etiqueta.length() > 0) {
			respuesta.append(" etiqueta=\"").append(etiqueta).append("\"");
		}
		if (id != null && id.length() > 0) {
			respuesta.append(" id=\"").append(id).append("\"");
		}
		if (nombre != null && nombre.length() > 0) {
			respuesta.append(" nombre=\"").append(nombre).append("\"");
		}
		respuesta.append(" ts=\"").append(ts == null ? calcularTS() : ts).append("\"");
		if (mensaje != null) {
			respuesta.append(" >");
			respuesta.append(getMessage(mensaje, escapeSpecialChar));
			respuesta.append("</");
			respuesta.append(tipo);
			respuesta.append(">\r\n");
		} else {
			respuesta.append("/>\r\n");
		}
		return respuesta.toString();
	}

	private static String generarMensajeRespuestaLogs(String id, String nombre, String mensaje, String categoria, String truncado, int cantidad, int maximo,
			boolean escapeSpecialChar) {
		String etiqueta = "resultado-logs";
		String tipo = "corrida";
		StringBuffer respuesta = new StringBuffer();
		respuesta.append("<");
		respuesta.append(etiqueta);
		respuesta.append(">");
		respuesta.append("<");
		respuesta.append(tipo);
		respuesta.append(" id=\"").append(id).append("\"");
		respuesta.append(" nombre=\"").append(nombre).append("\"");
		respuesta.append(" categoria=\"").append(categoria).append("\"");
		respuesta.append(" truncado=\"").append(truncado).append("\"");
		respuesta.append(" cantidad=\"").append(cantidad).append("\"");
		respuesta.append(" maximo=\"").append(maximo).append("\"");

		if (mensaje != null) {
			respuesta.append(">");
			respuesta.append(getMessage(mensaje, escapeSpecialChar));
			respuesta.append("</");
			respuesta.append(tipo);
			respuesta.append(">");
			respuesta.append("</");
			respuesta.append(etiqueta);
			respuesta.append(">");
		} else {
			respuesta.append("/>");
		}
		return respuesta.toString();
	}

	private static String getMessage(String mensaje, boolean escapeSpecialChar) {

		byte[] mensajeByte;

		mensajeByte = mensaje.getBytes();

		if (escapeSpecialChar) {
			OsServiceFactory.getOsService().escapaSaltosDeLinea(StringEscapeUtils.escapeXml(mensaje));
		}

		if (mensajeByte.length > ServicioAgente.cfg.getOutput_maxsize()) {
			return (mensaje.substring(0, ServicioAgente.cfg.getOutput_maxsize()));
		} else {
			return (mensaje);
		}
	}
}

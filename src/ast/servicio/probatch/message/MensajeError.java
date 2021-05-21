/**
 * 
 */
package ast.servicio.probatch.message;

import java.io.OutputStream;

import org.apache.commons.lang3.StringEscapeUtils;

import ast.servicio.probatch.exception.MensajeErrorException;

/**
 * @author javier.padin
 * 
 */
public class MensajeError extends Mensaje {

	/**
	 * @param tramaXml
	 * @param mensajeEntrada
	 * @throws MensajeErrorException
	 */
	public MensajeError(String mensajeEntrada) {
		super(mensajeEntrada);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ast.servicio.probatch.message.Mensaje#procesarMensaje()
	 */
	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) {
		// TODO Auto-generated method stub
		return this;
	}

	public MensajeError(String tipo, String id, String nombre, String mensaje, String interfaces) {
		super(null);
		String tramaString = generarMensajeError(tipo, id, nombre,
				mensaje, this.getTs(), interfaces);
		this.setTramaString(tramaString);

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
	private static String generarMensajeError(String tipo, String id,
			String nombre, String mensaje, long ts, String interfaces) {
		if (tipo == null || tipo.length() < 1)
			tipo = "error";
		StringBuffer respuesta = new StringBuffer();
		respuesta.append("<");
		respuesta.append(tipo);
		if (id != null && id.length() > 0) {
			respuesta.append(" id=\"").append(id).append("\"");
		}
		if (nombre != null && nombre.length() > 0) {
			respuesta.append(" nombre=\"").append(nombre).append("\"");
		}
		respuesta.append(" ts=\"").append(ts).append("\"");
		if (interfaces != null) {
			respuesta.append(" interfaces=\"").append(interfaces).append("\"");
		} 
		if (mensaje != null) {
			respuesta.append(">");
			respuesta.append(StringEscapeUtils.escapeXml(mensaje));
			respuesta.append("</");
			respuesta.append(tipo);
			respuesta.append(">\r\n");
		} else {
			respuesta.append("/>\r\n");
		}
		return respuesta.toString();
	}

}

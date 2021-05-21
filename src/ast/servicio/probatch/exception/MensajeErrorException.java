package ast.servicio.probatch.exception;

import java.util.Date;

import org.apache.commons.lang3.StringEscapeUtils;

public class MensajeErrorException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5883951335297888291L;
	
	private StringBuffer respuestaError;
	private long ts;
	
	/**
	 * Mensaje de error generico, solo con texto. Tag default ERROR
	 * @param mensaje
	 * @return
	 */
	public MensajeErrorException(String mensaje) {
		respuestaError = crearMensajeError(null, null, null, mensaje);
	}
	
	/**
	 * Mensaje de error generico, solo con texto
	 * @param mensaje
	 * @return
	 */
	public MensajeErrorException(String tipo, String mensaje) {
		respuestaError = crearMensajeError(tipo, null, null, mensaje);
	}
	
	/**
	 * Mensaje de error con parametros, todos son opcionales.
	 * @param id
	 * @param nombre
	 * @param mensaje
	 * @return
	 */
	public MensajeErrorException(String tipo, String id, String nombre, String mensaje) {
		respuestaError = crearMensajeError(tipo, id, nombre, mensaje);
	}
	
	/**
	 * Mensaje de error con parametros, todos son opcionales.
	 * @param id
	 * @param nombre
	 * @param interfaces
	 * @return
	 */
	private StringBuffer crearMensajeError(String tipo, String id, String nombre, String mensaje){
		this.calcularTS();
		if(tipo == null )
			tipo = "error";
		StringBuffer respuesta = new StringBuffer();
		if(tipo.equals("SOLO")){
			respuesta.append(mensaje);
			return respuesta;
		}
			
		respuesta.append("<");
		respuesta.append(tipo);
		if (id != null && id.length() > 0) {
			respuesta.append(" id=\"").append(id).append("\"");
		}
		if (nombre != null && nombre.length() > 0) {
			respuesta.append(" nombre=\"").append(nombre).append("\"");
		}
		respuesta.append(" ts=\"").append(this.ts).append("\"");
		if(mensaje != null){
			respuesta.append(" >");
			respuesta.append(StringEscapeUtils.escapeXml(mensaje));
			respuesta.append("</");
			respuesta.append(tipo);
			respuesta.append(">\r\n");
		}else{
			respuesta.append("/>\r\n");
		}
		return respuesta;
	}
	
	private void calcularTS() {
		ts = new Date().getTime() / 1000;
	}
	
	public StringBuffer getRespuestaError() {
		return respuestaError;
	}

	

}

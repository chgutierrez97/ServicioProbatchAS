package ast.servicio.probatch.factory;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.message.MensajeAutenticacion;
import ast.servicio.probatch.message.MensajeBuscarLog;
import ast.servicio.probatch.message.MensajeError;
import ast.servicio.probatch.message.MensajeEstado;
import ast.servicio.probatch.message.MensajeHoraAS400;
import ast.servicio.probatch.message.MensajeLimpiar;
import ast.servicio.probatch.message.MensajeMatar;
import ast.servicio.probatch.message.MensajeProceso;
import ast.servicio.probatch.message.ConsultaProcesoAS400;
import ast.servicio.probatch.message.MensajeRespuesta;
import ast.servicio.probatch.message.MensajeValidacion;
import ast.servicio.probatch.message.TiposMensaje;
import ast.servicio.probatch.message.TiposMensaje.tipoMensaje;

/**
 * Genera una instancia de Mensaje determinada a partir de un tipo de mensaje
 * especifico.
 * 
 * @author matias.brino
 * 
 */
public class MessageFactory {

	/**
	 * Genera a traves del xml de entrada un tipo de mensaje segun corresponda.
	 * 
	 * @param mensajeEntrada
	 * @return
	 * @throws MensajeErrorException
	 */
	public static Mensaje crearMensaje(String mensajeEntrada) throws MensajeErrorException {
		mensajeEntrada = mensajeEntrada.trim();
		Document tramaXml = parsearMensaje(mensajeEntrada);
		if (tipoMensaje.AUTENTICACION.toString().equals(tramaXml.getDocumentElement().getNodeName()))
			return new MensajeAutenticacion(mensajeEntrada);
		if (tipoMensaje.VALIDAR.toString().equals(tramaXml.getDocumentElement().getNodeName()))
			return new MensajeValidacion(mensajeEntrada);
		if (tipoMensaje.PROCESO.toString().equals(tramaXml.getDocumentElement().getNodeName()))
			return new MensajeProceso(mensajeEntrada);
		if (tipoMensaje.LIMPIAR.toString().equals(tramaXml.getDocumentElement().getNodeName()))
			return new MensajeLimpiar(mensajeEntrada);
		if (tipoMensaje.ESTADO.toString().equals(tramaXml.getDocumentElement().getNodeName()))
			return new MensajeEstado(mensajeEntrada);
		if (tipoMensaje.MATAR.toString().equals(tramaXml.getDocumentElement().getNodeName()))
			return new MensajeMatar(mensajeEntrada);
		if (tipoMensaje.BUSCAR_LOG.toString().equals(tramaXml.getDocumentElement().getNodeName()))
			return new MensajeBuscarLog(mensajeEntrada);
		if (tipoMensaje.HORA_AS400.toString().equals(tramaXml.getDocumentElement().getNodeName()))
			return new MensajeHoraAS400();
		if (tipoMensaje.PROCESO_AS400.toString().equals(tramaXml.getDocumentElement().getNodeName()))
			return new ConsultaProcesoAS400(mensajeEntrada);
		else
			throw new MensajeErrorException("Tipo de mensaje no reconocido");
	}

	/**
	 * Genera el mensaje de autenticacion para enviar al cliente la primera vez.
	 * 
	 * @return
	 */
	public static Mensaje crearMensajeConfirmacionAutenticacion() {
		return new MensajeAutenticacion();
	}

	/**
	 * Crea mensaje de error permitiendo ingresar tipo y mensaje.
	 * 
	 * @param tipo
	 *            -- Tipo de error -- Default "error"
	 * @param id
	 *            -- default null
	 * @param nombre
	 *            -- default null
	 * @param mensaje
	 *            -- default null
	 * @param interfaces
	 *            -- default null
	 * @return
	 */
	public static Mensaje crearMensajeError(String tipo, String id, String nombre, String mensaje, String interfaces) {
		return new MensajeError(tipo, id, nombre, mensaje, interfaces);
	}

	/**
	 * Crea el mensaje de error con el xml ingresado.
	 * 
	 * @param xmlString
	 *            -- XML en formato string
	 * @return
	 */
	public static Mensaje crearMensajeError(String xmlString) {
		return new MensajeError(xmlString);
	}

	/**
	 * Crea mensaje de error permitiendo ingresar tipo y mensaje.
	 * 
	 * @param tipo
	 *            -- Tipo de error -- Default "error"
	 * @param mensaje
	 *            -- Mensaje de error
	 * @return
	 */
	public static Mensaje crearMensajeError(String tipo, String mensaje) {
		return new MensajeError(tipo, null, null, mensaje, null);
	}

	/**
	 * Crea el mensaje de respuesta con el xml ingresado.
	 * 
	 * @param xmlString
	 *            -- XML de respuesta en formato String
	 * @return
	 */
	public static Mensaje crearMensajeRespuesta(String xmlString) {
		return new MensajeRespuesta(xmlString);
	}

	/**
	 * Crea el mensaje de respuesta con los parametros ingresados. Escapa los
	 * caracteres especiales del mensaje.
	 * 
	 * @param tipo
	 *            -- Default "mensaje"
	 * @param etiqueta
	 *            -- Default null
	 * @param id
	 *            -- Default null
	 * @param nombre
	 *            -- Default null
	 * @param mensaje
	 *            -- Default null
	 * @param ts
	 *            -- Se genera dinamicamente
	 * @return Mensaje de Respuesta
	 */
	public static Mensaje crearMensajeRespuesta(String tipo, String etiqueta, String id, String nombre, String mensaje, Long ts) {
		return new MensajeRespuesta(tipo, etiqueta, id, nombre, mensaje, ts, true);
	}

	/**
	 * Crea el mensaje de respuesta con los parametros ingresados. Con la
	 * posibilidad de escapar o no los caracteres especiales del mensaje.
	 * 
	 * @param tipo
	 *            -- Default "mensaje"
	 * @param etiqueta
	 *            -- Default null
	 * @param id
	 *            -- Default null
	 * @param nombre
	 *            -- Default null
	 * @param mensaje
	 *            -- Default null
	 * @param ts
	 *            -- Se genera dinamicamente
	 * @return Mensaje de Respuesta
	 * @param escapeSpecialChar
	 *            -- Permite elegir si se desea escapar o no los caracteres
	 *            especiales del mensaje.
	 * @return
	 */
	public static Mensaje crearMensajeRespuesta(String tipo, String etiqueta, String id, String nombre, String mensaje, Long ts, boolean escapeSpecialChar) {
		return new MensajeRespuesta(tipo, etiqueta, id, nombre, mensaje, ts, escapeSpecialChar);
	}

	/**
	 * @return
	 */
	public static Mensaje crearMensajeRespuestaLogs(String id, String nombre, String mensaje, String categoria, String truncado, int cantidad, int maximo,
			boolean escapeSpecialChar) {
		return new MensajeRespuesta(id, nombre, mensaje, categoria, truncado, cantidad, maximo, escapeSpecialChar);
	}

	/**
	 * Consturctor que genera el xml basado en el mensaje de entrada, valida
	 * ademas que el mensaje sea valido.
	 * 
	 * @param xmlMessage
	 * @throws MensajeError
	 */

	public static Mensaje crearMensajeHoraAS400() {
		return new MensajeHoraAS400();
	}

	private static Document parsearMensaje(String xmlMessage) throws MensajeErrorException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document tramaXml = builder.parse(new InputSource(new StringReader(xmlMessage)));

			String tipo = tramaXml.getDocumentElement().getNodeName();

			if (!validarTipoMensaje(tipo)) {
				throw new MensajeErrorException("SOLO", "comando no reconocido");
			}
			return tramaXml;
		} catch (MensajeErrorException eM) {
			throw new MensajeErrorException(eM.getRespuestaError().toString());
		} catch (Exception e) {
			throw new MensajeErrorException(e.getMessage());
		}
	}

	/**
	 * Valida si el tipo de mensaje es permitido.
	 * 
	 * @param tipo
	 * @return
	 */
	private static boolean validarTipoMensaje(String tipo) {
		for (TiposMensaje.tipoMensaje tipoM : TiposMensaje.tipoMensaje.values()) {
			if (tipoM.toString().equals(tipo))
				return true;
		}
		return false;
	}

}

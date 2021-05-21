package ast.servicio.probatch.message;

import java.io.OutputStream;
import java.util.Date;

import org.w3c.dom.Document;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.util.Utils;

/**
 * Clase que representa a un mensaje generico. De ella heredan todos los
 * mensajes particulares que puede enviar o recibir el servidor
 * 
 * comentario
 * 
 * @author javier.padin
 * 
 */
public abstract class Mensaje {

	private Document tramaXml;
	private String tramaString;
	private long ts;

	public Mensaje(String mensajeEntrada) {
		super();
		this.tramaString = mensajeEntrada;
		this.ts = calcularTS();
	}

	public abstract Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException;

	/**
	 * Calcula el TS, tiempo q marca el inicio del proceso.
	 */
	public static long calcularTS() {
		return new Date().getTime() / 1000;
	}

	public void setTramaXml(Document tramaXml) {
		this.tramaXml = tramaXml;
	}

	public void setTramaString(String tramaString) {
		this.tramaString = tramaString;
	}

	/**
	 * Devuelve el mensaje en formato XML
	 * 
	 * @return
	 * @throws MensajeErrorException
	 */
	public Document getTramaXml() throws MensajeErrorException {
		if (tramaXml == null)
			try {
				return Utils.parsearMensaje(this.getTramaString());
			} catch (Exception e) {
				throw new MensajeErrorException(e.getMessage());
			}
		return this.tramaXml;
	}

	public String getTramaString() {
		return tramaString;
	}

	public long getTs() {
		return this.ts;
	}

}

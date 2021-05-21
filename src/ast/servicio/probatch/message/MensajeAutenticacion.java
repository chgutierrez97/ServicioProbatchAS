package ast.servicio.probatch.message;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Document;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class MensajeAutenticacion extends Mensaje {

	public MensajeAutenticacion(String mensajeEntrada) {
		super(mensajeEntrada);
		// TODO Auto-generated constructor stub
	}

	public MensajeAutenticacion() {
		super(null);
		StringBuffer msjAutenticacion = new StringBuffer();
		msjAutenticacion.append("<autenticacion>");
		msjAutenticacion.append(this.generarClaveAutenticacion(this.getTs()));
		msjAutenticacion.append("</autenticacion>");
		this.setTramaString(msjAutenticacion.toString());
	}

	private String generarClaveAutenticacion(long ts) {
		String tsConexion = Long.toString(ts);
		String autenticacion = Utils.byte2hex(Utils.xorstr(ServicioAgente.cfg.getKey(), "", Utils.byte2hex(tsConexion.getBytes())));
		return autenticacion;
	}

	public boolean validarAutenticacion() throws MensajeErrorException {
		try {
			byte[] key = null;

			Document tramaXml = getTramaXml();

			String clave = tramaXml.getDocumentElement().getTextContent();

			key = Utils.xorstr(Long.toString(getTs()), ServicioAgente.cfg.getKey(), clave);

			String keyResultado = new String(key, "UTF-8");

//			System.out.println("Clave a comparar: " + keyResultado);

			if (keyResultado.equals(ServicioAgente.cfg.getKey()))
				return true;
		} catch (UnsupportedEncodingException e) {
			throw new MensajeErrorException("Error en el encoding de la autenticación");
		}
		return false;
	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) {
		// TODO Auto-generated method stub
		return this;
	}

}

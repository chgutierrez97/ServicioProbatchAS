package ast.servicio.probatch.message;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

/**
 * Devuelve información sobre los procesos lanzados por el servidor. Se puede
 * obtener estados desde un estado determinado a otro (desde="n1" hasta="n3").
 * 
 * @author rodrigo.guillet
 * 
 */
public class MensajeEstado extends Mensaje {

	public MensajeEstado(String mensajeEntrada) {
		super(mensajeEntrada);
	}

	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {
		List<EstadoProceso> listaEstados = ServicioAgente.getEstadoMensajes();
		StringBuffer listaTransicionEstados = new StringBuffer();
		String etiqueta = Utils.obtenerParametroTramaString(this.getTramaString(), "etiqueta");
		if (!listaEstados.isEmpty()) {
			String desdeString = Utils.obtenerParametroTramaString(this.getTramaString(), "desde");
			String hastaString = Utils.obtenerParametroTramaString(this.getTramaString(), "hasta");
			if (desdeString.equals("") || hastaString.equals(""))
				throw new MensajeErrorException("Error en la sintaxis del mensaje.");

			int desde = new Integer(desdeString);
			int hasta = new Integer(hastaString);
			synchronized (listaEstados) {
				for (Iterator<EstadoProceso> iterator = listaEstados.iterator(); iterator.hasNext();) {
					EstadoProceso estadoProceso = iterator.next();
					Integer id = new Integer(estadoProceso.getId());
					if (id >= desde && id <= hasta)
						listaTransicionEstados.append(estadoProceso.getMensajeTransicionEstado().getTramaString());
				}
			}

		}
		return MessageFactory.crearMensajeRespuesta("estado", etiqueta, null, null, listaTransicionEstados.toString(), null, false);
	}

}

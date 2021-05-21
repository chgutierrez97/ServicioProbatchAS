package ast.servicio.probatch.message;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class MensajeMatar extends Mensaje {
	
	public static Logger logger = LoggerFactory.getLogger(MensajeMatar.class);

	public MensajeMatar(String mensajeEntrada) {
		super(mensajeEntrada);

	}

	public Mensaje procesarMensaje(OutputStream oSalida) {

		List<EstadoProceso> listaEstados = ServicioAgente.getEstadoMensajes();
		String idProceso = Utils.obtenerParametroTramaString(this.getTramaString(), "id");
		synchronized (listaEstados) {
			for (Iterator<EstadoProceso> iterator = listaEstados.iterator(); iterator.hasNext();) {
				EstadoProceso estadoProceso = (EstadoProceso) iterator.next();
				if (estadoProceso.getId().equals(idProceso)) {
					if (estadoProceso.getEstado() == null) {
						logger.debug("CMD["+estadoProceso.getId()+"]"+" matar pid="+estadoProceso.getPid());
						matar(estadoProceso.getPid());
						estadoProceso.setEstado(-9999);
						return estadoProceso.getMensajeTransicionEstado();
					} else {
						return MessageFactory.crearMensajeError(null, "El proceso ya ha terminado con valor" + estadoProceso.getEstado());
					}
				}

			}
		}

		return MessageFactory.crearMensajeError("No se encontro el proceso id: " + idProceso);

	}

	public boolean matar(int pid) {
		try {
			Runtime.getRuntime().exec(OsServiceFactory.getOsService().getKillCommand(pid));
		} catch (Exception e1) {
			// Logs
			return false;
		}
		return true;
	}

}

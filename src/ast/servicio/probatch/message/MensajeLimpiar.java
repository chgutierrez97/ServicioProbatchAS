package ast.servicio.probatch.message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.service.ServicioAgente;

public class MensajeLimpiar extends Mensaje {

	public static Logger logger = LoggerFactory.getLogger(MensajeLimpiar.class);

	public MensajeLimpiar(String mensajeEntrada) {
		super(mensajeEntrada);
	}

	/**
	 * Limpia los procesos en memoria (non-Javadoc)
	 * 
	 * @see ast.servicio.probatch.message.Mensaje#procesarMensaje(java.io.OutputStream)
	 */
	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {

		List<EstadoProceso> lista = ServicioAgente.getEstadoMensajes();
		if (lista.isEmpty()) {
			logger.debug("No hay estados de procesos en memoria para limpiar");
			return MessageFactory.crearMensajeRespuesta("exito", null, null, null, null, null);
		} else {
			synchronized (lista) {
				for (Iterator<EstadoProceso> iterator = lista.iterator(); iterator.hasNext();) {
					EstadoProceso estadoProceso = iterator.next();
					if (estadoProceso.getEstado() == null) {
						logger.debug("No se puede limpiar los procesos debido a que existen procesos activos");
						return MessageFactory.crearMensajeRespuesta("error", null, null, null, "Hay procesos en ejecución", null);
					}
				}
				ServicioAgente.borrarListaEstadoMensajes();
				File file = new File(ServicioAgente.cfg.getWrkdir() + "/" + ServicioAgente.cfg.getDump_file());

				if (file.exists()) {
					try {
						PrintWriter printWriter = new PrintWriter(file);
						printWriter.print("<estado-guardado></estado-guardado>");
						printWriter.close();
					} catch (FileNotFoundException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}

				}
			}
		}
		return MessageFactory.crearMensajeRespuesta("exito", null, null, null, null, null);
	}

}

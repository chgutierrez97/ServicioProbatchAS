package ast.servicio.probatch.threads;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.service.ServicioAgente;

public class ServiceCleanDump extends Thread {

	public static Logger logger = LoggerFactory.getLogger(ServiceCleanDump.class);

	private long intervaloBorrado;
	private long intervaloBorradoSegundos;
	private String wkdir;
	private String dumpFile;
	private File fichero = null;
	private boolean terminarEsteThread;
	List<EstadoProceso> estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());
	List<EstadoProceso> lista = ServicioAgente.getEstadoMensajes();
	PrintWriter printWriter = null;

	public ServiceCleanDump(long intervaloBorrado, String wkdir, String dumpFile) {
		super("ServiceCleanDump Thread");
		this.intervaloBorrado = intervaloBorrado;
		this.wkdir = wkdir;
		this.dumpFile = dumpFile;
	}

	@Override
	public void run() {
		long timeActual;
		try {
			while (!ServicioAgente.terminarThreads && !terminarEsteThread) {
				sleep(intervaloBorrado);
				intervaloBorradoSegundos = intervaloBorrado / 1000;

				estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());
				timeActual = new Date().getTime() / 1000;
				fichero = new File(wkdir + "/" + dumpFile);
				printWriter = new PrintWriter(fichero);
				if (fichero.exists()) {

					synchronized (lista) {
						for (EstadoProceso estadoProceso : lista) {
							if (timeActual - estadoProceso.getTs() < intervaloBorradoSegundos) {
								estadoMensajes.add(estadoProceso);
							}
						}

						ServicioAgente.setEstadoMensajes(estadoMensajes);

					}

					printWriter.print("<estado-guardado>");

					for (EstadoProceso estadoProceso : ServicioAgente.getEstadoMensajes()) {
						estadoProceso.setDump(true);
						printWriter.print(estadoProceso.getMensajeTransicionEstado().getTramaString().trim());
					}
					printWriter.print("</estado-guardado>");

					printWriter.close();

				}

			}
		} catch (InterruptedException e) {
			terminarEsteThread = true;
			System.out.println(e);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

}

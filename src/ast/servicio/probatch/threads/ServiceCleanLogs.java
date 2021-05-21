package ast.servicio.probatch.threads;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.service.ServicioAgente;

public class ServiceCleanLogs extends Thread {

	public static Logger logger = LoggerFactory.getLogger(ServiceCleanLogs.class);

	private long intervaloBorrado;
	private String wkdir;
	private String dirLog;
	private boolean terminarEsteThread;

	public ServiceCleanLogs(long intervaloBorrado, String wkdir, String dirLog) {
		super("ServiceCleanLogs Thread");
		this.intervaloBorrado = intervaloBorrado;
		this.wkdir = wkdir;
		this.dirLog = dirLog;
	}

	@Override
	public void run() {
		try {
			while (!ServicioAgente.terminarThreads && !terminarEsteThread) {
				sleep(intervaloBorrado);
				File directorio = new File(wkdir + "/" + dirLog);
				if (directorio.isDirectory()) {
					File[] ficheros = directorio.listFiles();
					for (int indx = 0; indx < ficheros.length; indx++) {
						ficheros[indx].delete();
					}
				}

			}
		} catch (InterruptedException e) {
			terminarEsteThread = true;
		}
	}

}

package ast.servicio.probatch.threads;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class ServiceDump extends Thread {

	public static Logger logger = LoggerFactory.getLogger(ServiceDump.class);

	private long intervaloBajada;
	private String wkdir;
	private String dumpFile;
	private File file;
	private FileWriter fileWriter = null;
	private FileReader fileReader = null;
	private BufferedReader bufferedReader = null;
	String leido = "";
	private PrintWriter printWriter = null;
	private boolean terminarEsteThread;
	List<EstadoProceso> estadosSinBajar;
	List<EstadoProceso> listaEstadosArchivo = Collections.synchronizedList(new LinkedList<EstadoProceso>());
	private boolean flag = false;

	public ServiceDump(long intervaloBajada, String wkdir, String dumpFile) {
		super("ServiceDump Thread");
		this.intervaloBajada = intervaloBajada;
		this.wkdir = wkdir;
		this.dumpFile = dumpFile;
	}

	@Override
	public void run() {
		try {
			while (!ServicioAgente.terminarThreads && !terminarEsteThread) {

				estadosSinBajar = estadosNoDump(ServicioAgente.getEstadoMensajes());

				
					try {
						file = new File(wkdir + "/" + dumpFile);

						if (file.exists()) {
							fileReader = new FileReader(file);
							bufferedReader = new BufferedReader(fileReader);
							leido = bufferedReader.readLine();
							fileReader.close();
						}

						listaEstadosArchivo = Utils.stringEstadosToListaEstados(leido);

						for (EstadoProceso estadoSinBajar : estadosSinBajar) {
							flag = false;
							for (EstadoProceso estadoArchivo : listaEstadosArchivo) {
								if (estadoSinBajar.getId().equals(estadoArchivo.getId())) {
									estadoArchivo.setEstado(estadoSinBajar.getEstado());
									estadoArchivo.setId(estadoSinBajar.getId());
									estadoArchivo.setNombre(estadoSinBajar.getNombre());
									estadoArchivo.setPid(estadoSinBajar.getPid());
									estadoArchivo.setTs(estadoSinBajar.getTs());
									flag = true;
									break;
								}
							}
							if (!flag) {
								listaEstadosArchivo.add(estadoSinBajar);
							}
							estadoSinBajar.setDump(true);
						}
						
						fileWriter = new FileWriter(wkdir + "/" + dumpFile);
						printWriter = new PrintWriter(fileWriter);
						printWriter.print("<estado-guardado>");

						for (Iterator<EstadoProceso> iterator = listaEstadosArchivo.iterator(); iterator.hasNext();) {
							EstadoProceso estado = (EstadoProceso) iterator.next();
							printWriter.print(estado.getMensajeTransicionEstado().getTramaString().trim());
						}
						printWriter.print("</estado-guardado>");
						fileWriter.close();
						logger.info("Dump!");
						sleep(intervaloBajada);
					} catch (IOException iO) {
						logger.error("No se puede escribir en archivo" + dumpFile);
						logger.trace(iO.getMessage());
					} finally {
						try {
							if (fileWriter != null) {
								fileWriter.close();
							}
						} catch (IOException iO) {
							logger.error("No se puede escribir en archivo" + dumpFile);
							logger.trace(iO.getMessage());
						}
					}


			}
		} catch (InterruptedException e) {
			terminarEsteThread = true;
		}

	}

	private List<EstadoProceso> estadosNoDump(List<EstadoProceso> estadoMensajes) {
		List<EstadoProceso> listaResultado = Collections.synchronizedList(new LinkedList<EstadoProceso>());
		for (Iterator<EstadoProceso> iterator = estadoMensajes.iterator(); iterator.hasNext();) {
			EstadoProceso estado = (EstadoProceso) iterator.next();
			if (!estado.isDump()) {
				listaResultado.add(estado);
			}
		}
		return listaResultado;
	}
}

package ast.servicio.probatch.threads;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.CommandCall;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.message.MensajeProceso;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.os.service.domain.IProcess;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.LoggerUtils;
import ast.servicio.probatch.util.Utils;

public class EjecutarProceso extends Thread {

	public static Logger logger = LoggerFactory.getLogger(EjecutarProceso.class);
	public static Logger loggerProceso;
	private OutputStream osSalida;
	private ParametrosProceso parametroP;
	private IProcess process;
	public static boolean procesoTerminado;
	private long fechaInicio;

	public EjecutarProceso(IProcess process, OutputStream osSalidaSocket, ParametrosProceso parametroP) {
		super("Ejecucion Proceso (" + parametroP.getId() + ")");
		this.process = process;
		this.osSalida = osSalidaSocket;
		this.parametroP = parametroP;
		/*debug-----*/
		logger.debug("Creando instancia de EjecutarProceso...");
		/*debug-----*/
		loggerProceso = LoggerUtils.createLoggerProceso(parametroP);
		fechaInicio = Calendar.getInstance().getTimeInMillis();
		procesoTerminado = false;

	}

	@Override
	public void run() {

		try {
			MensajeProceso.terminarThreadsLocal = false;
			loggerProceso.info(" *** " + parametroP.getComando() + " *** ");
			int exitVal = 0;

			if (OsServiceFactory.isAS400()) {

				ListenerProcesoAS400 procesoInput = new ListenerProcesoAS400(process, osSalida, parametroP);
				procesoInput.start();

				/*se mata el proceso si ocurre un timeout de socket.*/
				/*FIXME: dentro del bucle while no hay una sentencia sleep entre cada corrida del bucle*/
//				while (procesoInput.isAlive() && !MensajeProceso.terminarThreadsLocal) {
//					long tiempoTranscurridoInicioProceso = Calendar.getInstance().getTimeInMillis() - fechaInicio;
//					int timeoutSocket = ServicioAgente.cfg.getTimeout_socket();
//					
//					if (tiempoTranscurridoInicioProceso > timeoutSocket) {
//						Utils.matar(parametroP.getPid());
//						exitVal = 1;
//						MensajeProceso.terminarThreadsLocal = true;
//					}
//				}

				try {

					String mensajeTransicion = "";
					byte[] respuesta = null;
					boolean muerteIntencional = false;

					if (exitVal == 1) {

						mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS() + "\">"
								+ traducirEstado(exitVal) + "</transicion>";

						logger.debug("CLI <-- " + mensajeTransicion);
						logger.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

						osSalida.write(("CLI <-- " + mensajeTransicion).getBytes());
						loggerProceso.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

						MensajeProceso.actualizarEstadoProceso(parametroP, exitVal);

						// Valido si la falla fue por el mensaje matar, si fue
						// asi
						// la muerte es intencional y no muestro nada en la
						// salida y
						// los logs ya que de eso se encarga el mensaje matar
					} else {

						exitVal = process.waitFor();

						// Valido si hubo errores al terminar
						if (exitVal == 0 && procesoInput.getErrorFatal() == null) {

							if (validacionInterfaces(parametroP.getInterfaces(), "salida") != null) {

								mensajeTransicion = validacionInterfaces(parametroP.getInterfaces(), "salida").getTramaString();
								logger.debug(mensajeTransicion);
								logger.error("No se generaron las interfaces de salida para el proceso de id " + parametroP.getId() + " con estado = " + exitVal);

							} else if (parametroP.getResultado() != null && validacionResultado(parametroP.getResultado()) != null) {

								mensajeTransicion = validacionResultado(parametroP.getResultado()).getTramaString();
								logger.debug(mensajeTransicion);
								logger.error("No se generó la interfaz de resultado para el proceso de id " + parametroP.getId() + " con estado = " + exitVal);

							} else {
								// Proceso finalizo correctamente
								if (parametroP.getResultado() != null && !parametroP.getResultado().getValor().equals("")) {
									List<String> listaResultado = leerBytesArchivo(parametroP.getResultado().getValor(), ServicioAgente.cfg.getResult_maxsize());

									for (Iterator<String> iterator = listaResultado.iterator(); iterator.hasNext();) {
										String mensajeResutlado = (String) iterator.next();

										mensajeTransicion = "<mensaje id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS()
												+ "\">" + mensajeResutlado + "</mensaje>";
										osSalida.write(mensajeTransicion.getBytes());
										logger.debug(mensajeTransicion);
										loggerProceso.info(mensajeTransicion);
									}
								}

								mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS() + "\">"
										+ traducirEstado(exitVal) + "</transicion>";

								logger.debug("CLI <-- " + mensajeTransicion);
								logger.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

								loggerProceso.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);
							}
							respuesta = mensajeTransicion.getBytes();
						} else if (exitVal == 0 && procesoInput.getErrorFatal() != null) {

							loggerProceso.info("Termino el proceso de id " + parametroP.getId() + " con error fatal : " + procesoInput.getErrorFatal());
							mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS() + "\">"
									+ traducirEstado(exitVal) + "</transicion>";
							logger.debug(mensajeTransicion);
							respuesta = procesoInput.getErrorFatal().getBytes();
							exitVal = -9999;
						} else if (exitVal != 0) {

							mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS() + "\">"
									+ traducirEstado(exitVal) + "</transicion>";

							logger.debug("CLI <-- " + mensajeTransicion);
							logger.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

							osSalida.write(("CLI <-- " + mensajeTransicion).getBytes());
							loggerProceso.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

							// Valido si la falla fue por el mensaje matar, si
							// fue
							// asi
							// la muerte es intencional y no muestro nada en la
							// salida y
							// los logs ya que de eso se encarga el mensaje
							// matar
						} else if (!validarMuerteIntencional(parametroP)) {

							mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + parametroP.getTs() + "\">"
									+ traducirEstado(exitVal) + "</transicion>";

							logger.info("Termino el proceso de id " + parametroP.getId() + " con status " + exitVal);
							loggerProceso.info("Termino el proceso de id " + parametroP.getId() + " con status " + exitVal);
							logger.debug("CLI <-- " + mensajeTransicion);
							respuesta = mensajeTransicion.getBytes();
							MensajeProceso.actualizarEstadoProceso(parametroP, exitVal);
							osSalida.write(respuesta);
						} else {
							muerteIntencional = true;
						}
						// Solo se actualiza estado si no fue muerte
						// intencional, ya
						// que en este ultimo caso se actualiza
						// en la operacion MensajeMatar
						if (!muerteIntencional) {
							MensajeProceso.actualizarEstadoProceso(parametroP, exitVal);
							osSalida.write(respuesta);
						}

						String comandoFinalizado = "[" + parametroP.getId() + "] FIN pid=" + parametroP.getPid() + " exit=cmd='";
						comandoFinalizado = comandoFinalizado + parametroP.toString();
						logger.debug(comandoFinalizado);

					}
				} catch (FileNotFoundException e) {
					logger.error("Error al leer la interfaz resultado " + e.getMessage());
					logger.trace(e.getMessage());
				} catch (Exception e) {

					if (process.exitValue() != 1) {
						logger.error(e.getMessage());
					}
				}

			} else {

				ListenerProceso procesoInput = new ListenerProceso(ListenerProceso.TYPE_INPUT, process, osSalida, parametroP);
				procesoInput.start();

				ListenerProceso procesoError = new ListenerProceso(ListenerProceso.TYPE_ERROR, process, osSalida, parametroP);
				procesoError.start();

				/*se mata el proceso si ocurre un timeout de socket.*/
				/*FIXME: dentro del bucle while no hay una sentencia sleep entre cada corrida del bucle*/
//				while ((procesoInput.isAlive() || procesoError.isAlive()) && !MensajeProceso.terminarThreadsLocal) {
//					long tiempoTranscurridoInicioProceso = Calendar.getInstance().getTimeInMillis() - fechaInicio;
//					int timeoutSocket = ServicioAgente.cfg.getTimeout_socket();
//
//					if (tiempoTranscurridoInicioProceso > timeoutSocket) {
//
//						Utils.matar(parametroP.getPid());
//						exitVal = 1;
//						MensajeProceso.terminarThreadsLocal = true;
//					}
//
//				}

				try {

					String mensajeTransicion = "";
					byte[] respuesta = null;
					boolean muerteIntencional = false;

					if (exitVal == 1) {

						mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS() + "\">"
								+ traducirEstado(exitVal) + "</transicion>";

						logger.debug("CLI <-- " + mensajeTransicion);
						logger.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

						osSalida.write(("CLI <-- " + mensajeTransicion).getBytes());
						loggerProceso.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

						MensajeProceso.actualizarEstadoProceso(parametroP, exitVal);

						// Valido si la falla fue por el mensaje matar, si fue
						// asi
						// la muerte es intencional y no muestro nada en la
						// salida y
						// los logs ya que de eso se encarga el mensaje matar
					} else {

						exitVal = process.waitFor();

						// Valido si hubo errores al terminar
						if (exitVal == 0 && procesoInput.getErrorFatal() == null) {

							if (validacionInterfaces(parametroP.getInterfaces(), "salida") != null) {

								mensajeTransicion = validacionInterfaces(parametroP.getInterfaces(), "salida").getTramaString();
								logger.debug(mensajeTransicion);
								logger.error("No se generaron las interfaces de salida para el proceso de id " + parametroP.getId() + " con estado = " + exitVal);

							} else if (parametroP.getResultado() != null && validacionResultado(parametroP.getResultado()) != null) {

								mensajeTransicion = validacionResultado(parametroP.getResultado()).getTramaString();
								logger.debug(mensajeTransicion);
								logger.error("No se generó la interfaz de resultado para el proceso de id " + parametroP.getId() + " con estado = " + exitVal);

							} else {
								// Proceso finalizo correctamente
								if (parametroP.getResultado() != null && !parametroP.getResultado().getValor().equals("")) {
									List<String> listaResultado = leerBytesArchivo(parametroP.getResultado().getValor(), ServicioAgente.cfg.getResult_maxsize());

									for (Iterator<String> iterator = listaResultado.iterator(); iterator.hasNext();) {
										String mensajeResutlado = (String) iterator.next();

										mensajeTransicion = "<mensaje id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS()
												+ "\">" + mensajeResutlado + "</mensaje>";
										osSalida.write(mensajeTransicion.getBytes());
										logger.debug(mensajeTransicion);
										loggerProceso.info(mensajeTransicion);
									}
								}

								mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS() + "\">"
										+ traducirEstado(exitVal) + "</transicion>";

								logger.debug("CLI <-- " + mensajeTransicion);
								logger.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

								loggerProceso.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);
							}
							respuesta = mensajeTransicion.getBytes();
						} else if (exitVal == 0 && procesoInput.getErrorFatal() != null) {

							loggerProceso.info("Termino el proceso de id " + parametroP.getId() + " con error fatal : " + procesoInput.getErrorFatal());
							mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS() + "\">"
									+ traducirEstado(exitVal) + "</transicion>";
							logger.debug(mensajeTransicion);
							respuesta = procesoInput.getErrorFatal().getBytes();
							exitVal = -9999;
						} else if (exitVal != 0) {

							mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS() + "\">"
									+ traducirEstado(exitVal) + "</transicion>";

							logger.debug("CLI <-- " + mensajeTransicion);
							logger.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

							osSalida.write(("CLI <-- " + mensajeTransicion).getBytes());
							loggerProceso.info("Termino el proceso de id #" + parametroP.getId() + " con status " + exitVal);

							// Valido si la falla fue por el mensaje matar, si
							// fue
							// asi
							// la muerte es intencional y no muestro nada en la
							// salida y
							// los logs ya que de eso se encarga el mensaje
							// matar
						} else if (!validarMuerteIntencional(parametroP)) {

							mensajeTransicion = "<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + parametroP.getTs() + "\">"
									+ traducirEstado(exitVal) + "</transicion>";

							logger.info("Termino el proceso de id " + parametroP.getId() + " con status " + exitVal);
							loggerProceso.info("Termino el proceso de id " + parametroP.getId() + " con status " + exitVal);
							logger.debug("CLI <-- " + mensajeTransicion);
							respuesta = mensajeTransicion.getBytes();
							MensajeProceso.actualizarEstadoProceso(parametroP, exitVal);
							osSalida.write(respuesta);
						} else {
							muerteIntencional = true;
						}
						// Solo se actualiza estado si no fue muerte
						// intencional, ya
						// que en este ultimo caso se actualiza
						// en la operacion MensajeMatar
						if (!muerteIntencional) {
							MensajeProceso.actualizarEstadoProceso(parametroP, exitVal);
							osSalida.write(respuesta);
						}

						String comandoFinalizado = "[" + parametroP.getId() + "] FIN pid=" + parametroP.getPid() + " exit=cmd='";
						comandoFinalizado = comandoFinalizado + parametroP.toString();
						logger.debug(comandoFinalizado);

					}
				} catch (FileNotFoundException e) {
					logger.error("Error al leer la interfaz resultado " + e.getMessage());
					logger.trace(e.getMessage());
				} catch (Exception e) {

					if (process.exitValue() != 1) {
						logger.error(e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error al escribir en socket: " + e.getMessage());
			e.printStackTrace();
			logger.trace(e.getMessage());
		} finally {
			LoggerUtils.removeLoggerAppender(loggerProceso.getName());
		}

	}

	/**
	 * Valida si el fin del proceso se dio por una muerte intencional a traves
	 * del mensaje matar. En ese caso no registra ningun mensa en la salida ni
	 * en logs ya que de eso se encarga el mensaje matar
	 * 
	 * @param param
	 * @return
	 */
	public boolean validarMuerteIntencional(ParametrosProceso param) {
		List<EstadoProceso> listaEstados = ServicioAgente.getEstadoMensajes();
		String idProceso = param.getId();
		synchronized (listaEstados) {
			for (Iterator<EstadoProceso> iterator = listaEstados.iterator(); iterator.hasNext();) {
				EstadoProceso estadoProceso = (EstadoProceso) iterator.next();
				if (estadoProceso.getId().equals(idProceso)) {
					if (estadoProceso.getEstado() != null && estadoProceso.getEstado() == -9999) {
						return true;
					}
				}

			}
		}
		return false;
	}

	public Mensaje validacionInterfaces(Collection<Atributo> interfaces, String tipo) {

		String interfaz = null;
		String noEcontrados = null;
		String controlarTodos = null;

		for (Iterator<Atributo> iterator = interfaces.iterator(); iterator.hasNext();) {
			Atributo atributo = iterator.next();
			if (tipo.equalsIgnoreCase(atributo.getNombre())) {
				interfaz = atributo.getValor();
				controlarTodos = atributo.getControlar_todos();

			}
		}
		if (interfaz != null) {

			noEcontrados = Utils.archivosNoEncontrados(interfaz);

			if (!noEcontrados.equals("")) {

				if (controlarTodos.equals("1")) {
					return MessageFactory.crearMensajeRespuesta("<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
							+ Mensaje.calcularTS() + "\"><fin estado=\"falla\" valor=\"No se puede leer el archivo " + StringEscapeUtils.escapeXml(noEcontrados)
							+ ": No such file or directory\"/></transicion>");
				} else if (controlarTodos.equals("0") && noEcontrados.equals(interfaz)) {
					return MessageFactory.crearMensajeRespuesta("<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
							+ Mensaje.calcularTS() + "\"><fin estado=\"falla\" valor=\"No se puede leer el archivo " + StringEscapeUtils.escapeXml(noEcontrados)
							+ ": No such file or directory\"/></transicion>");
				}

			}
		}
		return null;

	}

	public Mensaje validacionResultado(Atributo resultado) {

		String interfaz = resultado.getValor();
		String noEcontrados = null;

		if (interfaz != null) {

			noEcontrados = Utils.archivosNoEncontrados(interfaz);

			if (!noEcontrados.equals("")) {

				return MessageFactory.crearMensajeRespuesta("<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\"" + Mensaje.calcularTS()
						+ "\"><fin estado=\"falla\" valor=\"0\" interfaces=" + StringEscapeUtils.escapeXml(noEcontrados) + "/></transicion>");

			}
		}
		return null;

	}

	public static List<String> leerBytesArchivo(String fileName, int kBytesALeer) throws FileNotFoundException, IOException {
		List<String> listaBytesLeidos = new ArrayList<String>();
		int bytesALeer = kBytesALeer * 1024;
		int tamañoArchivo;
		int resto;
		String ultimoStringLista;
		String ultimoStringListaModificado;

		FileInputStream fileInput = new FileInputStream(fileName);
		BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInput);
		tamañoArchivo = bufferedInputStream.available();
		if (tamañoArchivo > 0) {
			resto = tamañoArchivo % bytesALeer;
			byte[] bytes = new byte[bytesALeer];

			while (bufferedInputStream.read(bytes) != -1) {
				listaBytesLeidos.add(new String(bytes));
			}

			ultimoStringLista = listaBytesLeidos.get(listaBytesLeidos.size() - 1);
			ultimoStringListaModificado = ultimoStringLista.substring(0, resto);

			listaBytesLeidos.set(listaBytesLeidos.size() - 1, ultimoStringListaModificado);
		}
		bufferedInputStream.close();
		return listaBytesLeidos;
	}

	public String traducirEstado(int estado) {

		if (estado == 0)
			return "<fin estado=\"exito\" />";
		else if (estado == -9999)
			return "<fin estado=\"muerte\" />";
		else
			return "<fin estado=\"falla\" valor=\"" + estado + "\"/>";

	}
}
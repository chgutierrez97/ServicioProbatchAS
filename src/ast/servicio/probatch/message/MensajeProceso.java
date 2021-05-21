package ast.servicio.probatch.message;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.os.service.domain.ASTProcess;
import ast.servicio.probatch.os.service.domain.ASTRunCommand;
import ast.servicio.probatch.os.service.domain.IProcess;
import ast.servicio.probatch.os.service.impl.UnixService;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.threads.EjecutarProceso;
import ast.servicio.probatch.util.Utils;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.JobLog;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.QueuedMessage;

public class MensajeProceso extends Mensaje {

	private static final String EXEC = "exec";
	private static final String EXPORT = "export";
	private static final String PUTNO_COMA = ";";
	private static final String STRING_EMPTY = " ";

	public static Logger logger = LoggerFactory.getLogger(MensajeProceso.class);
	public static boolean terminarThreadsLocal;

	public MensajeProceso(String mensajeEntrada) {
		super(mensajeEntrada);
	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {
		if (processExist()) {
			return MessageFactory.crearMensajeError("error", "El proceso no puede ejecutarse por que ya hay un proceso corriendo con ese mismo ID");
		}
		Mensaje respuesta = null;
		ParametrosProceso parametrosProceso = XmlToObject(this.getTramaXml());
		respuesta = validaciones(parametrosProceso);
		if (respuesta == null) {
			return ejecutarProceso(osSalida, parametrosProceso);
		}
		return respuesta;
	}

	/**
	 * Valida si el proceso esta corriendo, en caso de ser asi en el array de
	 * estados el proceso tendra estado null. En caso de finalizar tendra un
	 * valor int.
	 * 
	 * @return
	 */
	private boolean processExist() {
		List<EstadoProceso> listaEstado = ServicioAgente.getEstadoMensajes();
		if (listaEstado.isEmpty())
			return false;
		String id = obtenerId(this.getTramaString());
		synchronized (listaEstado) {
			for (Iterator<EstadoProceso> iterator = listaEstado.iterator(); iterator.hasNext();) {
				EstadoProceso estadoProceso = iterator.next();
				if (estadoProceso.getId().equals(id) && estadoProceso.getEstado() == null) {
					return true;
				}
			}
		}
		return false;
	}

	private String obtenerId(String cad) {
		String conseguirId = cad.substring(cad.indexOf("id"));
		conseguirId = conseguirId.substring(conseguirId.indexOf('"') + 1, conseguirId.indexOf('"', conseguirId.indexOf('"') + 1));
		return conseguirId;
	}

	private Mensaje ejecutarProceso(OutputStream osSalida, ParametrosProceso parametroP) {
		String debugMsgHeader = this.getClass().getName() + "ejecutarProceso:";
		logger.debug(debugMsgHeader + "inicio del metodo...");

		try {
			String[] cmd = OsServiceFactory.getOsService().getExecuteCommand(parametroP);
			String comando = OsServiceFactory.getOsService().getExecuteCommand(parametroP)[0];
			String comandoLanzado = "Lanzando el comando: " + (parametroP.getId() != null ? "CMD[" + parametroP.getId() + "] '" : "");
			String strDirChdir = parametroP.getChdir();

			File dirChdir = new File(strDirChdir);
			File dirCategoria;

			strDirChdir = dirChdir.exists() ? dirChdir.getAbsolutePath() : strDirChdir;
			if (!Utils.validarExistenciaArchivo(strDirChdir)) {
				Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
						+ parametroP.getTs() + "\">El directorio no existe: " + StringEscapeUtils.escapeXml(strDirChdir) + "</error>");
				return mensaje;
			}

			if (parametroP.getCategoria().equals("")) {
				Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
						+ parametroP.getTs() + "\">Debe especificarse 'categoria'</error>");
				return mensaje;
			}

			dirCategoria = new File(ServicioAgente.cfg.getWrkdir() + "/" + parametroP.getCategoria());

			if (!dirCategoria.exists() || !dirCategoria.canWrite()) {
				Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
						+ parametroP.getTs() + "\"> La carpeta \"" + parametroP.getCategoria() + "\" no existe o no tiene permiso de escritura " + "</error>");
				return mensaje;
			}

			if (OsServiceFactory.isAS400()) {
				IProcess process = null;
				try {
					
					process = processFactory(parametroP, cmd, dirChdir);
					parametroP.setPid(process.getPid());
					logger.info(debugMsgHeader + comandoLanzado + parametroP.toString() + " pid=" + parametroP.getPid());
					logger.info(debugMsgHeader + "varEntorno " + parametroP.getVarEntornoString() + "Comando " + parametroP.getComando() + "Parametros "
							+ parametroP.getArgumentosString());
					logger.debug(parametroP.toString());
				} catch (Exception e) {

					logger.error(e.getMessage());
					logger.debug(e.toString());
					Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre()
							+ "\" ts=\"" + parametroP.getTs() + "\">La ruta al programa debe ser una especificacion absoluta o ser un comando predefinido: "
							+ comando + "</error>");
					return mensaje;
				}

				// Revisar Estados //
				actualizarEstadoProceso(parametroP, null);
				EjecutarProceso ejecutarProceso = new EjecutarProceso(process, osSalida, parametroP);
				ejecutarProceso.start();
				terminarThreadsLocal = true;
				return MessageFactory.crearMensajeRespuesta("<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
						+ parametroP.getTs() + "\"><inicio/></transicion>");
			} else {

				IProcess process = null;
				try {

					for (String string : cmd) {

						if (("$login/$pwd -Mvar -Debug").equals(string)) {
							string = "sysid/accusys123 -Mvar -Debug";
						}
					}

					process = processFactory(parametroP, cmd, dirChdir);
					parametroP.setPid(process.getPid());
					logger.info(comandoLanzado + parametroP.toString() + " pid=" + parametroP.getPid());
					logger.info("varEntorno " + parametroP.getVarEntornoString() + "Comando " + parametroP.getComando() + "Parametros "
							+ parametroP.getArgumentosString());
					logger.debug(parametroP.toString());

				} catch (Exception e) {
					logger.error(e.getMessage());
					logger.debug(e.toString());
					Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre()
							+ "\" ts=\"" + parametroP.getTs() + "\">La ruta al programa debe ser una especificacion absoluta o ser un comando predefinido: "
							+ cmd[0] + "</error>");

					return mensaje;
				}

				// Revisar Estados //
				actualizarEstadoProceso(parametroP, null);
				EjecutarProceso ejecutarProceso = new EjecutarProceso(process, osSalida, parametroP);
				ejecutarProceso.start();
				terminarThreadsLocal = true;
				return MessageFactory.crearMensajeRespuesta("<transicion id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
						+ parametroP.getTs() + "\"><inicio/></transicion>");
			}

		} catch (Exception e) {

			logger.error("Error al ejecutar el proceso " + e.getMessage());
			Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre() + "\" ts=\""
					+ parametroP.getTs() + "\">Error al ejecutar el proceso</error>");
			return mensaje;
		}
	}

	/**
	 * Ejecuta el scipt segun el SO
	 * 
	 * @param parametroP
	 * @param cmd
	 * @param dirChdir
	 * @return
	 * @throws IOException
	 */
	private IProcess processFactory(ParametrosProceso parametroP, String[] cmd, File dirChdir) throws IOException {
		Process process = null;
		IProcess astProcess;
		if (OsServiceFactory.isUnixSO()) {

			String scriptUnix = builtTramaUNIX(parametroP, dirChdir);
			logger.info("Trama armada: " + scriptUnix);
			process = OsServiceFactory.getOsService().executeCommand(scriptUnix);
			astProcess = new ASTProcess(process);
		} else if (OsServiceFactory.isAS400()) {

			logger.debug("parametroP usuario,valor = "+parametroP.getUsuario().getNombre() + " : "+parametroP.getUsuario().getValor());
			AS400 conexion = new AS400(ServicioAgente.cfg.getaS400Server(), parametroP.getUsuario().getNombre(), parametroP.getUsuario().getValor());
			CommandCall comandoInvocado = new CommandCall(conexion, cmd[0]);
			astProcess = new ASTRunCommand(comandoInvocado);
			astProcess.run();
		} else {

			process = OsServiceFactory.getOsService().executeCommand(cmd, parametroP.getVarEntornoArray(), dirChdir);
			astProcess = new ASTProcess(process);
		}

		return astProcess;
	}

	/**
	 * Crea un String que contiene el script a ejecutar en UNIX
	 * 
	 * @param parametroP
	 * @param dirChdir
	 * @return String scriptUnix
	 */
	private String builtTramaUNIX(ParametrosProceso parametroP, File dirChdir) {

		StringBuffer scriptBuffer = new StringBuffer();
		scriptBuffer.append(UnixService.IMPERSONALIZACION_USER + " -");
		scriptBuffer.append(STRING_EMPTY);
		scriptBuffer.append(parametroP.getUsuario().getNombre());
		scriptBuffer.append(STRING_EMPTY);
		scriptBuffer.append("-c cd");
		scriptBuffer.append(STRING_EMPTY);
		scriptBuffer.append(dirChdir.getAbsolutePath() + PUTNO_COMA);
		scriptBuffer.append(STRING_EMPTY);
		scriptBuffer.append(EXPORT);
		scriptBuffer.append(STRING_EMPTY);
		scriptBuffer.append(parametroP.getVarEntornoString() + PUTNO_COMA);
		scriptBuffer.append(STRING_EMPTY);
		scriptBuffer.append(EXEC);
		scriptBuffer.append(STRING_EMPTY);
		scriptBuffer.append(this.getArrayCommands(parametroP));
		return scriptBuffer.toString();
	}

	/**
	 * Genera un String con el comando y todos sus paramentros para UNIX
	 * 
	 * @param pParameters
	 * @return
	 */
	private String getArrayCommands(ParametrosProceso pParameters) {
		StringBuilder parametersBuilder = new StringBuilder();
		parametersBuilder.append(pParameters.getComando() + STRING_EMPTY);
		if (pParameters.getArgumentos() != null) {
			for (Iterator<Atributo> iterator = pParameters.getArgumentos().iterator(); iterator.hasNext();) {
				Atributo atributo = iterator.next();
				parametersBuilder.append(atributo.getValor() + STRING_EMPTY);
			}
		}

		parametersBuilder.toString().trim();
		parametersBuilder.append(PUTNO_COMA);
		return parametersBuilder.toString();
	}

	/**
	 * Busca el estado, si lo encuentra lo pisa, si no crea una entrada nueva
	 * para el estado del proceso actual.
	 * 
	 * @param parametroP
	 * @param pid
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static void actualizarEstadoProceso(ParametrosProceso parametroP, Integer estado) {

		List<EstadoProceso> listaProcesos = ServicioAgente.getEstadoMensajes();
		synchronized (listaProcesos) {
			EstadoProceso procesoActual = buscarEstadoProceso(listaProcesos, parametroP.getId());
			if (procesoActual != null) {
				procesoActual.setEstado(estado);
				procesoActual.setNombre(parametroP.getNombre());
				procesoActual.setPid(parametroP.getPid());
				procesoActual.setTs(parametroP.getTs());
				procesoActual.setDump(false);

			} else {
				listaProcesos.add(new EstadoProceso(parametroP.getId(), parametroP.getNombre(), parametroP.getTs(), parametroP.getPid(), null));
			}
		}
	}

	public static EstadoProceso buscarEstadoProceso(List<EstadoProceso> listaProcesos, String id) {

		for (Iterator<EstadoProceso> iterator = listaProcesos.iterator(); iterator.hasNext();) {
			EstadoProceso estadoProceso = iterator.next();
			if (estadoProceso.getId().equals(id))

				return estadoProceso;
		}
		return null;
	}

	/**
	 * Verifica los parametros de un proceso a lanzar. En caso que los mismos
	 * sean invalidos, se retorna un mensaje de error. En caso de todo estar
	 * bien, se retorna null.
	 * 
	 * @param parametrosProceso
	 *            - Parametros de proceso a lanzar que se deben verificar.
	 * @return En caso que los parametros del proceso sean invalidos, se retorna
	 *         un mensaje de error. En caso de todo estar bien, se retorna null.
	 * @throws MensajeErrorException
	 */
	private Mensaje validaciones(ParametrosProceso parametrosProceso) throws MensajeErrorException {

		boolean existeUsuario = false;
		ArrayList<String> lista = ServicioAgente.cfg.getUsuarios();
		Atributo usuario = parametrosProceso.getUsuario();
		File categoria = new File(ServicioAgente.cfg.getWrkdir() + "/" + parametrosProceso.getCategoria());
		if (!categoria.isDirectory()) {

			return MessageFactory.crearMensajeError("error", parametrosProceso.getId(), parametrosProceso.getNombre(),
					"La carpeta: " + "'" + parametrosProceso.getCategoria() + "'" + " no existe", null);
		}
		for (Iterator<String> iterator = lista.iterator(); iterator.hasNext();) {

			String usuarioConfig = iterator.next();
			if (usuario.getNombre().equalsIgnoreCase(usuarioConfig)) {

				existeUsuario = true;
				break;
			}
		}

		if (parametrosProceso.getUsuario().getNombre().equals("")) {

			return MessageFactory.crearMensajeError("error", parametrosProceso.getId(), parametrosProceso.getNombre(), "Debe especificarse 'usuario'", null);
		}

		if (!existeUsuario) {

			return MessageFactory.crearMensajeError("error", parametrosProceso.getId(), parametrosProceso.getNombre(), "Usuario invalido: "
					+ parametrosProceso.getUsuario().getNombre(), null);
		}

		if (validacionInterfaces(parametrosProceso.getInterfaces(), "entrada", parametrosProceso.getPatrones(), parametrosProceso.getId(),
				parametrosProceso.getNombre()) != null) {
			return MessageFactory.crearMensajeError(
					"error",
					parametrosProceso.getId(),
					parametrosProceso.getNombre(),
					"Faltan archivos de entrada: "
							+ validacionInterfaces(parametrosProceso.getInterfaces(), "entrada", parametrosProceso.getPatrones(), parametrosProceso.getId(),
									parametrosProceso.getNombre()), null);
		}

		return null;
	}

	public String validacionInterfaces(Collection<Atributo> interfaces, String tipo, Collection<Atributo> patrones, String id, String nombre)
			throws MensajeErrorException {

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
			validaPatron(patrones, noEcontrados, id, nombre);
			if (!noEcontrados.equals("")) {

				if (controlarTodos.equals("1")) {

					return noEcontrados;
				} else if (controlarTodos.equals("0") && noEcontrados.equals(interfaz)) {

					return noEcontrados;
				}
			}
		}

		return null;
	}

	public ParametrosProceso XmlToObject(Document input) throws MensajeErrorException {

		ParametrosProceso paramProc = new ParametrosProceso();
		try {
			Element raiz = input.getDocumentElement();
			NodeList nodosHijos = raiz.getChildNodes();
			if (Utils.esNumerico(raiz.getAttribute("id"))) {

				paramProc.setId(raiz.getAttribute("id"));
			} else {

				throw new MensajeErrorException("error", raiz.getAttribute("id"), raiz.getAttribute("nombre"), "Id no numerica: " + raiz.getAttribute("id"));
			}

			String key = ServicioAgente.cfg.getKey();
			Atributo aux = new Atributo("id", "");
			paramProc.setTs(getTs());
			Collection<Atributo> argumentos = new ArrayList<Atributo>();
			Collection<Atributo> entorno = new ArrayList<Atributo>();
			Collection<Atributo> interfaces = new ArrayList<Atributo>();
			Collection<Atributo> patrones = new ArrayList<Atributo>();
			paramProc.setNombre(raiz.getAttribute("nombre"));
			paramProc.setCategoria(raiz.getAttribute("categoria"));
			paramProc.setClase(raiz.getAttribute("clase"));
			int i = 0;
			for (i = 0; i < nodosHijos.getLength(); i++) {

				String nodo = nodosHijos.item(i).getNodeName().trim();
				if (nodo.equals("chdir")) {

					paramProc.setChdir(nodosHijos.item(i).getTextContent());
				} else if (nodo.equals("comando")) {

					if (nodosHijos.item(i).getTextContent() == null || nodosHijos.item(i).getTextContent().equals(""))

						throw new MensajeErrorException("El campo comando es obligatorio");

					paramProc.setComando(ServicioAgente.cfg.getComandos(nodosHijos.item(i).getTextContent()));
				} else if (nodo.equals("arg")) {

					aux = new Atributo("", nodosHijos.item(i).getTextContent());
					if (nodosHijos.item(i).getAttributes().getNamedItem("tipo") != null) {

						aux.setTipo(nodosHijos.item(i).getAttributes().getNamedItem("tipo").getTextContent());
						if (aux.getTipo().equals("oculto")) {

							aux.setValor(new String(Utils.xorstr(key, paramProc.getId(), nodosHijos.item(i).getTextContent())).trim());
							aux.setValorMostrar(new String(Utils.xorstr(key, paramProc.getId(), nodosHijos.item(i).getTextContent())));
						}
					}

					argumentos.add(aux);
				} else if (nodo.equals("usuario")) {

					aux = new Atributo(nodosHijos.item(i).getTextContent(), new String(Utils.xorstr(key, paramProc.getId(), nodosHijos.item(i).getAttributes()
							.getNamedItem("clave").getTextContent())));
					paramProc.setUsuario(aux);
				} else if (nodo.equals("entorno")) {

					NodeList subHijos = nodosHijos.item(i).getChildNodes();
					for (int a = 0; a < subHijos.getLength(); a++) {

						if (subHijos.item(a).getNodeName().trim().equals("var")) {

							aux = new Atributo(subHijos.item(a).getAttributes().getNamedItem("nombre").getTextContent(), subHijos.item(a).getTextContent());
							if (subHijos.item(a).getAttributes().getNamedItem("tipo") != null) {

								aux.setTipo(subHijos.item(a).getAttributes().getNamedItem("tipo").getTextContent());
								if (aux.getTipo().equals("oculto"))
									aux.setValor(new String(Utils.xorstr(key, paramProc.getId(), subHijos.item(a).getTextContent())));

								aux.setValorMostrar(new String(Utils.xorstr(key, paramProc.getId(), subHijos.item(a).getTextContent())));
							}
							entorno.add(aux);
						}
					}
				} else if (nodo.equals("patron")) {

					NodeList subHijos = nodosHijos.item(i).getChildNodes();
					for (int a = 0; a < subHijos.getLength(); a++) {

						aux = new Atributo(subHijos.item(a).getNodeName().trim(), subHijos.item(a).getTextContent());
						if (subHijos.item(a).getAttributes().getNamedItem("tipo") != null) {

							aux.setTipo(subHijos.item(a).getAttributes().getNamedItem("tipo").getTextContent());
							if (aux.getTipo().equals("oculto")) {

								aux.setValor(new String(Utils.xorstr(key, paramProc.getId(), nodosHijos.item(i).getTextContent())));
							}
						} else {

							aux.setTipo("glob");
						}

						patrones.add(aux);
					}

				} else if (nodo.equals("resultado")) {

					aux = new Atributo("resultado", nodosHijos.item(i).getTextContent());
					paramProc.setResultado(aux);
				} else if (nodo.equals("interfaces")) {

					NodeList subHijos = nodosHijos.item(i).getChildNodes();
					for (int a = 0; a < subHijos.getLength(); a++) {

						aux = new Atributo(subHijos.item(a).getNodeName().trim(), subHijos.item(a).getTextContent());
						if (subHijos.item(a).getAttributes().getNamedItem("controlar_todos") != null) {

							aux.setControlar_todos(subHijos.item(a).getAttributes().getNamedItem("controlar_todos").getTextContent());
						}

						interfaces.add(aux);
					}
				}
			}

			if (paramProc.getComando() == null || paramProc.getComando().equals("")) {

				throw new MensajeErrorException("El campo comando es obligatorio");
			} else if (paramProc.getNombre() == null || paramProc.getNombre().equals("")) {

				throw new MensajeErrorException("El campo nombre es obligatorio");
			}

			paramProc.setEntorno(entorno);
			paramProc.setInterfaces(interfaces);
			paramProc.setPatrones(patrones);
			paramProc.setArgumentos(argumentos);
		} catch (MensajeErrorException m) {

			throw m;
		} catch (Exception e) {

			logger.error("Error en la sintaxis del mensaje. Exception: " + e.getMessage());
			throw new MensajeErrorException("Error en la sintaxis del mensaje.");
		}

		return paramProc;
	}

	public String validaPatron(Collection<Atributo> patrones, String salidaProceso, String id, String nombre) throws MensajeErrorException {

		if (!patrones.isEmpty()) {

			for (Iterator<Atributo> iterator = patrones.iterator(); iterator.hasNext();) {

				Atributo atributo = (Atributo) iterator.next();
				if (ParametrosProceso.FATAL.equals(atributo.getNombre()) && Utils.validaEsxpresionesRegulrares(salidaProceso, atributo.getValor())) {

					throw new MensajeErrorException("fatal", id, nombre, salidaProceso);
				}
			}

			for (Iterator<Atributo> iterator = patrones.iterator(); iterator.hasNext();) {

				Atributo atributo = (Atributo) iterator.next();
				if (ParametrosProceso.IGNORAR.equals(atributo.getNombre()) && atributo.getTipo().equals("glob")
						&& Utils.validaEsxpresionesRegulrares(salidaProceso, atributo.getValor())) {

					return salidaProceso;
				} else if (ParametrosProceso.IGNORAR.equals(atributo.getNombre())

						&& atributo.getTipo().equals("re")
						&& (Utils.validaEsxpresionesRegulrares(salidaProceso, atributo.getValor()) || Utils.validaEsxpresionesRegulrares(salidaProceso,
								ServicioAgente.cfg.getIgnore_re()))) {
					return salidaProceso;
				}

				return null;
			}
		}

		return null;
	}

	private static int do_run(AS400 sys, String command) throws AS400SecurityException, IOException, ErrorCompletingRequestException, InterruptedException,
			ObjectDoesNotExistException {
		sys.connectService(AS400.COMMAND);

		AS400 sys_mon = new AS400(sys);
		sys_mon.connectService(AS400.COMMAND);

		CommandCall cmd = new CommandCall(sys, command);

		Job job;
		job = cmd.getServerJob();
		byte[] internalJobIdentifier = (byte[]) job.getValue(Job.INTERNAL_JOB_IDENTIFIER);
		Job job_mon = new Job(sys_mon, internalJobIdentifier);

		JobLog jlog_mon = job_mon.getJobLog();
		ASTRunCommand rc = new ASTRunCommand(cmd);

		Thread t = new Thread(rc);

		Thread hook = new ShutdownThread(job, job_mon);

		Runtime.getRuntime().addShutdownHook(hook);
		t.start();
		byte[] last = null;

		while (t.isAlive()) {

			jlog_mon.setStartingMessageKey(last);

			Enumeration<?> messageList = jlog_mon.getMessages();

			if (last != null) {
				messageList.nextElement();
			}

			while (messageList.hasMoreElements()) {
				QueuedMessage message = (QueuedMessage) messageList.nextElement();
				printMessage(message);
				logger.debug(message.getMessage());
				last = message.getKey();
			}

			Thread.sleep(500);
		}

		t.join();

		try {
			Runtime.getRuntime().removeShutdownHook(hook);
		} catch (IllegalStateException e) {
		}
		;

		JobLog jlog = job.getJobLog();
		jlog.setStartingMessageKey(last);

		Enumeration<?> messageList = jlog.getMessages();

		if (messageList.hasMoreElements())
			messageList.nextElement();

		while (messageList.hasMoreElements()) {
			QueuedMessage message = (QueuedMessage) messageList.nextElement();
			// printMessage(message);
			logger.debug(message.getMessage());
		}

		if (rc.sucessfulExecution()) {
			logger.debug("" + "0 El proceso termino correctamente");
			return 0;
		} else {
			logger.debug("" + "1 El proceso termino incorrectamente");
			return 1;
		}

	}

	private static void printMessage(QueuedMessage message) {
		System.out.println(message.getFromProgram() + ":" + message.getAlertOption() + ":" + message.getSeverity() + ":" + message.getText());
	}

	private static class ShutdownThread extends Thread {
		private Job job, job_mon;

		ShutdownThread(Job job, Job job_mon) {
			this.job = job;
			this.job_mon = job_mon;
		}

		public void run() {
			try {
				System.out.println("Voy a matar el job");
				job_mon.end(-1);
				job.end(-1);
				System.out.println("Maté el job");
			} catch (Exception e) {
				logger.debug(e.getMessage());
			}

		}
	}
}

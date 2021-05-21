package ast.servicio.probatch.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.configuration.Configurador;
import ast.servicio.probatch.connection.Conexion;
import ast.servicio.probatch.connection.OutputWriter;
import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.threads.Heartbeat;
import ast.servicio.probatch.threads.ServiceCleanDump;
import ast.servicio.probatch.threads.ServiceCleanLogs;
import ast.servicio.probatch.threads.ServiceDump;
import ast.servicio.probatch.threads.ServiceProcessMessage;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class ServicioAgente {
	public final static String VERSION = "5.0.0";
	final String CATEGORIA = "batch";
	public static String LOCALHOST = "locahost";
	public static Configurador cfg;
	private static List<EstadoProceso> estadoMensajes;
	public static String winExecAs;
	public static boolean connectionStatus = false;
	public static boolean terminarThreads;
	private static String osName = System.getProperty("os.name");

	public static Logger logger = LoggerFactory.getLogger(ServicioAgente.class);

	public static boolean printVersion(String[] args) {
		if (args == null || args.length == 0)
			return false;

		String arg = args[0].toLowerCase();
		if (arg.contentEquals("-v") || arg.contentEquals("--version"))
			return true;

		return false;
	}// printVersion
	
	public static void main(String[] args) throws IOException {
//		logger.debug("Cantidad argumentos del array main: " + args.length);
		if (printVersion(args)) {
			System.out.println("Servicio Agente Probatch V" + VERSION);
			return;
		}
		
		String archCfg;
		
		if(args == null || args.length < 1)
			archCfg = "mantis.conf";
		else
			archCfg = args[0];
		
		initLogger(archCfg);

		System.out.println("Archivo de Configuracion: " + archCfg);
		ServicioAgente probatch = new ServicioAgente(archCfg);
//		logger.debug(osName);
		
			if (osName.contains("Xp") || osName.contains("2003")) {
				winExecAs = cfg.getWinRunAs();
			} else {
				winExecAs = cfg.getRunAsUser();
			}


		probatch.ejecutarServicio();

	}
	
	
	private static void initLogger(String fileConfig) {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(context);
		context.reset(); // override default configuration
		// inject the name of the current application as "application-name"
		// property of the LoggerContext
		context.putProperty("PROBATCH-CONFIG", fileConfig);
		try {
			File file = new File("ServicioAgente.jar");
			JarFile jarFile;
			jarFile = new JarFile(file);

			InputStream is = jarFile.getInputStream(jarFile.getEntry("logback.xml"));

			jc.doConfigure(is);
		} catch (JoranException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ServicioAgente(String archCfg) {
		cfg = new Configurador(archCfg);
		// Declara los tipos de mensajes validos.
		// estadoMensajes = Utils.levantarEstados(cfg.getWrkdir() + "/" +
		// cfg.getDump_file());
		estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());
	}

	public void ejecutarServicio() throws IOException {
		logger.info("Probatch version " + VERSION);
		logger.info("Escuchando en el puerto " + cfg.getPort());
		Socket socket = null;
		ServerSocket serverSocket = null;
		while (true) {
			// Establece una unica conexion con un unico cliente.
			try {
				serverSocket = Conexion.obtenerServerSocket(cfg.getPort());
				terminarThreads = false;
				socket = Conexion.obtenerConexion(serverSocket);
				socket.setKeepAlive(true);
//				socket.setSoTimeout(cfg.getTimeout_socket());
				InputStream entrada = socket.getInputStream();
				OutputWriter output = new OutputWriter(socket.getOutputStream(), cfg.getOutput_maxsize());

				// Thread de latido
				int heartbeatTimeIntervalMillis = ServicioAgente.cfg.getHeartbeat_interval() * 1000;
				Heartbeat hearbeat = new Heartbeat(heartbeatTimeIntervalMillis, output);
				// Thread de resguardo de estados
				ServiceDump serviceDump = new ServiceDump(ServicioAgente.cfg.getDump_interval() * 1000, ServicioAgente.cfg.getWrkdir(), ServicioAgente.cfg
						.getDump_file());
				// Thread para limpiar archivo de estados
				double timeoutCleanLogs = ServicioAgente.cfg.getTimeout();
				ServiceCleanDump serviceCleanDump = new ServiceCleanDump((long) (timeoutCleanLogs * 3600000), ServicioAgente.cfg.getWrkdir(),
						ServicioAgente.cfg.getDump_file());
				// Thread para limpiar logs.
				ServiceCleanLogs cleanLogs = new ServiceCleanLogs(ServicioAgente.cfg.getClean_logs() * 1000, ServicioAgente.cfg.getWrkdir(), CATEGORIA);

				if (Conexion.autenticarCliente(entrada, output)) {
					ServicioAgente.connectionStatus = true;
					// Inicio el thread de Latido.
					hearbeat.start();
					// Inicio el thread de ServiceDump.
					serviceDump.start();
					// Inicio el thread de ServiceCleanDump.
					serviceCleanDump.start();
					// Inicio el thread de CleanLogs.
					cleanLogs.start();
					//					
					while (ServicioAgente.connectionStatus) {
						try {
							output.flush();

							/*se lee un mensaje de entrada*/
							byte[] msjEntrada = new byte[ServicioAgente.cfg.getOutput_maxsize()];
							entrada.read(msjEntrada);

							//se parsea el mensaje de entrada
							String strMensaje = new String(msjEntrada, "UTF-8").trim();
							logger.debug("CLI --> " + strMensaje);
							if (strMensaje == null || strMensaje.length() < 1) {
								logger.error("Se perdio la conexion");
								throw new IOException("Se perdio la conexión");
							}
							
							//se procesa el mensaje en paralelo
							ServiceProcessMessage serviceCut = new ServiceProcessMessage(entrada, output, strMensaje);
							serviceCut.start();
						} catch (IOException e) {
							// Logs
							logger.error("El cliente cerro la conexion");
							logger.trace(e.getMessage());
							ServicioAgente.connectionStatus = false;
						} catch (Exception e) {
							// Logs
							logger.error(e.getMessage());
							logger.trace(e.getMessage());
							ServicioAgente.connectionStatus = false;
						}
					}
				}
				// Termino los threads
				terminarThreads = true;
				terminarThreads(hearbeat, serviceDump, cleanLogs, serviceCleanDump);
				// Cuando pierdo la conexion cierro los streams de entrada y
				// salida,
				// para permitir una nueva conexion.

			} catch (Exception e) {
				logger.trace(e.getMessage());
				logger.error("Error en la conexion");
				System.err.print("Error en la conexion");
			} finally {
				if (socket != null)
					Conexion.cerrarStreams(socket);
				if (serverSocket != null)
					serverSocket.close();
				terminarThreads = true;
			}
		}
	}//ejecutarServicio

	/**
	 * Termina los threads para reiniciar el servicio.
	 * 
	 * @param hearbeat
	 * @param serviceDump
	 * @param cleanLogs
	 * @param serviceCleanDump
	 */
	private void terminarThreads(Heartbeat hearbeat, ServiceDump serviceDump, ServiceCleanLogs cleanLogs, ServiceCleanDump serviceCleanDump) {
		try {
			hearbeat.interrupt();
			serviceDump.interrupt();
			cleanLogs.interrupt();
			serviceCleanDump.interrupt();
		} catch (Exception e) {
			logger.trace(e.getMessage());
		}
	}

	public static List<EstadoProceso> getEstadoMensajes() {
		return estadoMensajes;
	}

	public static void setEstadoMensajes(List<EstadoProceso> estadoMensajes) {
		ServicioAgente.estadoMensajes = estadoMensajes;
	}

	public static void borrarListaEstadoMensajes() {
		ServicioAgente.estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());
	}

//	private Mensaje procesarRequerimiento(String strMensaje, OutputStream osSalida) {
//		Mensaje respuesta = null;
//		try {
//
//			Mensaje mensajeEntrada = MessageFactory.crearMensaje(strMensaje);
//			respuesta = mensajeEntrada.procesarMensaje(osSalida);
//			if (respuesta != null) {
//				logger.debug("CLI <-- " + respuesta.getTramaString());
//			}
//		} catch (MensajeErrorException e) {
//			// Logs
//			respuesta = MessageFactory.crearMensajeRespuesta(e.getRespuestaError().toString());
//			logger.error(e.getRespuestaError().toString());
//			logger.trace(e.getMessage());
//		}
//		return respuesta;
//	}
}

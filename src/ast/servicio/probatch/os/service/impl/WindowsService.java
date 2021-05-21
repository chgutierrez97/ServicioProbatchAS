package ast.servicio.probatch.os.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.domain.UsuarioPermiso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.message.MensajeValidacion;
import ast.servicio.probatch.os.service.OsService;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.os.service.util.Kernel32;
import ast.servicio.probatch.os.service.util.W32API;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

import com.sun.jna.Pointer;

public class WindowsService extends OsService {
	public static Logger logger = LoggerFactory.getLogger(MensajeValidacion.class);

	public WindowsService() {
	}

	@Override
	public boolean is_absolute_path(String x) {
		Pattern absolutePath = Pattern.compile("^[a-z]\\:[\\/].*");
		Matcher absoluteMatcher = absolutePath.matcher(x);

		return absoluteMatcher.matches();
	}

	@Override
	public int getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field f = proceso.getClass().getDeclaredField("handle");
		f.setAccessible(true);
		long handl = f.getLong(proceso);

		Kernel32 kernel = Kernel32.INSTANCE;
		W32API.HANDLE handle = new W32API.HANDLE();
		handle.setPointer(Pointer.createConstant(handl));
		return kernel.GetProcessId(handle);
	}

	@Override
	public String[] getExecuteCommand(ParametrosProceso parametroP) throws Exception {

		String atributos = "";
		ArrayList<String> listaParam = new ArrayList<String>();
		listaParam.add(ServicioAgente.winExecAs);
		listaParam.add(parametroP.getChdir());

		// listaParam.add(dominio);
		// listaParam.add(usuario);
		listaParam.add(parametroP.getUsuario().getNombre());

		listaParam.add(parametroP.getUsuario().getValor());
		listaParam.add("\"" + parametroP.getComando() + "\"");
		// listaParam.add(parametroP.getComando());
		if (parametroP.getComando().contains("cmd")) {
			atributos = "/c";
		}
		if (parametroP.getArgumentos() != null) {
			for (Iterator<Atributo> iterator = parametroP.getArgumentos().iterator(); iterator.hasNext();) {
				Atributo atributo = iterator.next();
				atributos = atributos + " " + atributo.getValor();
			}
		}
		listaParam.add("\"" + atributos + "\"");
		String[] comandoAsArray = new String[listaParam.size()];
		int i = 0;
		for (Iterator<String> iterator = listaParam.iterator(); iterator.hasNext();) {
			String var = iterator.next();
			comandoAsArray[i] = var;
			i++;
		}
		return comandoAsArray;
	}

	@Override
	public String getKillCommand(int pid) {
		return "TASKKILL /F /T /PID " + pid;
	}

	@Override
	public boolean buscarUsuarioPermisos(String usuario, String permisos, String path, String dominio) throws MensajeErrorException {
		String pathFinal = path;
		pathFinal = path;
		if (pathFinal.contains("\\"))
			pathFinal = pathFinal.replace('\\', '/');

		boolean resultado = false;

		for (UsuarioPermiso usuarioPermiso : permisosUsuario(path)) {
			if (resultado)
				break;
			if (usuarioPermiso.getUsuario().equalsIgnoreCase(usuario) && usuarioPermiso.getDominio().equalsIgnoreCase("BUILTIN")) {

				if (getEquivalenciasPermisos(permisos, usuarioPermiso.getPermisos())) {
					resultado = true;
					break;
				}
			}
			if (usuarioPermiso.getUsuario().equalsIgnoreCase(usuario) && usuarioPermiso.getDominio().equalsIgnoreCase(dominio)) {
				if (getEquivalenciasPermisos(permisos, usuarioPermiso.getPermisos()))
					resultado = true;
			} else {
				if (usuarioPermiso.getDominio().equalsIgnoreCase("BUILTIN")) {
					// administradores o usuarios
					for (UsuarioPermiso usuarioPermisoNetLocalGroup : listaNetLocalGroup(usuarioPermiso.getUsuario())) {
						if (resultado)
							break;
						if (usuarioPermisoNetLocalGroup.getUsuario().equalsIgnoreCase(usuario)
								&& usuarioPermisoNetLocalGroup.getDominio().equalsIgnoreCase(dominio)) {
							if (getEquivalenciasPermisos(permisos, usuarioPermiso.getPermisos()))
								resultado = true;
						} else {
							if (dominio.equalsIgnoreCase(usuarioPermisoNetLocalGroup.getDominio())) {
								if (perteneceGrupoDominio(usuarioPermisoNetLocalGroup.getUsuario(), usuarioPermisoNetLocalGroup.getDominio(), usuario)) {
									if (getEquivalenciasPermisos(permisos, usuarioPermiso.getPermisos()))
										resultado = true;
								}
							}
						}

					}
				}
			}
		}

		return resultado;
	}

	/**
	 * Devuelve una lista de objetos del tipo "UsuarioPermiso", en los cuales se
	 * incluye usuario,permisos y dominio en un determinado path
	 * 
	 * @param path
	 * @return
	 * @throws MensajeErrorException
	 */
	private List<UsuarioPermiso> permisosUsuario(String path) throws MensajeErrorException {
		try {
			List<UsuarioPermiso> usuarioPermisos = new ArrayList<UsuarioPermiso>();
			String usarioPermiso = null;
			// Obtengo el nombre del Sistema Operativo
			// Se lanza el ejecutable.
			Process p = executeCommand(new String[] { "cmd", "/c", "cacls", path });
			// Se obtiene el stream de salida del programa
			InputStream is = p.getInputStream();
			// Se prepara un bufferedReader para poder leer la salida más
			// comodamente.
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// Se lee la primera linea
			String aux = br.readLine();
			String usuario;
			String permiso;
			String dominio = null;
			String hostName = getHostName();

			while (aux != null) {
				while ((StringUtils.countMatches(aux, "\\") - 1) > 0) {
					aux = aux.substring(aux.indexOf("\\") + 1, aux.length());
				}
				if (aux.contains("\\")) {
					usarioPermiso = Utils.obtenerUltimoSegmento(aux, "\\");
					dominio = Utils.obtenerCadIzquierda(aux, "\\");
					dominio = dominio.substring(dominio.indexOf(" ") + 1, dominio.length());
					dominio = dominio.trim();
				}

				if (dominio.equalsIgnoreCase("NT AUTHORITY") || dominio.equalsIgnoreCase(hostName))
					dominio = "";
				if (usarioPermiso != null) {
					usuario = Utils.obtenerCadIzquierda(usarioPermiso, ":");
					permiso = Utils.obtenerCadDerecha(usarioPermiso, ":");
					permiso = Utils.obtenerUltimoSegmento(permiso, ")");
					permiso = permiso.replaceAll(" ", "");
					if (permiso.equalsIgnoreCase("F") || permiso.equalsIgnoreCase("M")) {
						permiso = "rxw";
					}
					usuarioPermisos.add(new UsuarioPermiso(usuario, permiso, dominio));
					usarioPermiso = null;
				}
				aux = br.readLine();
			}
			return usuarioPermisos;

		} catch (IOException e) {
			throw new MensajeErrorException("Error al ejecutar el comando de validacion de permisos.");
		}
	}

	/**
	 * 
	 * Devuelve una lista de objetos del tipo "UsuarioPermiso", en los cuales se
	 * incluye usuarios, grupos de usuario, permisos y dominios de un
	 * determinado grupo, el cual ademas de poseer usuarios, puede poseer
	 * tambien subgrupos.
	 * 
	 * @param parametro
	 * @return
	 * @throws MensajeErrorException
	 */
	private List<UsuarioPermiso> listaNetLocalGroup(String parametro) throws MensajeErrorException {
		try {
			List<UsuarioPermiso> usuarioPermisos = new ArrayList<UsuarioPermiso>();
			Process p = executeCommand(new String[] { "cmd", "/c", "net", "localgroup", parametro });
			InputStream is = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String aux = br.readLine();
			String usuario;
			String dominio = "";

			while (!aux.contains("----------------") && aux != null) {
				aux = br.readLine();
			}
			if (aux != null) {
				aux = br.readLine();
			}

			while (aux != null) {
				dominio = "";
				if (aux.contains("\\")) {
					dominio = Utils.obtenerCadIzquierda(aux, "\\");
					usuario = Utils.obtenerCadDerecha(aux, "\\");
				} else {
					usuario = aux;
				}

				usuarioPermisos.add(new UsuarioPermiso(usuario, dominio));
				aux = br.readLine();

			}
			for (int i = 0; i < 2; i++) {
				usuarioPermisos.remove(usuarioPermisos.size() - 1);
			}
			return usuarioPermisos;

		} catch (IOException e) {
			throw new MensajeErrorException("Error al ejecutar el comando de validacion de permisos.");
		}
	}

	/**
	 * Valida si un determinado usuario pertenece a un determinado grupo en un
	 * cierto dominio, asi como tambien se esta validando la posibilidad de que
	 * el grupo ingresado como parametro no sea grupo.
	 * 
	 * @param grupoAVerificar
	 * @param dominioAVerificar
	 * @param usuario
	 * @return
	 */
	private boolean perteneceGrupoDominio(String grupoAVerificar, String dominioAVerificar, String usuario) {
		boolean resultado = false;
		if (getDominioSistema().equalsIgnoreCase(dominioAVerificar)) {
			try {

				Process p2 = executeCommand(new String[] { "cmd", "/c", "net", "group", grupoAVerificar, "/Domain", "|", "findstr", "/R",
						"\"\\<" + usuario + "\\>\"" });

				InputStream is = p2.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String aux = br.readLine();
				int exitValue = p2.waitFor();
				if (exitValue == 0 && aux != null) {

					resultado = analizarResultadoFindStr(aux, usuario);
				}

			} catch (IOException e) {
				logger.error("Error al intentar verificar si " + usuario + " pertenece al dominio: " + dominioAVerificar + e.getMessage());
				logger.trace(e.getMessage());
			} catch (InterruptedException ie) {
				logger.error("Error al intentar verificar si " + usuario + " pertenece al dominio: " + dominioAVerificar + ie.getMessage());
				logger.trace(ie.getMessage());

			}
		}

		return resultado;
	}

	/**
	 * Valida si el usuario que se busca se encuentra en el resultado del
	 * findstr
	 * 
	 * @param cadenaUsuarios
	 * @param usuarioBuscado
	 * @return
	 */
	private boolean analizarResultadoFindStr(String cadenaUsuarios, String usuarioBuscado) {
		StringTokenizer stk = new StringTokenizer(cadenaUsuarios, " ");
		while (stk.hasMoreElements()) {
			String usuario = stk.nextToken().trim();
			if (usuario.equalsIgnoreCase(usuarioBuscado))
				return true;
		}
		return false;
	}

	/**
	 * Obtiene el dominio del sistema, dominio actual.
	 * 
	 * @return
	 */
	private String getDominioSistema() {
		String dominio = "";
		try {
			InetAddress address = InetAddress.getLocalHost();
			dominio = address.getCanonicalHostName();
			if (dominio.contains(".")) {
				dominio = Utils.obtenerSegundaCad(dominio, ".");
			} else {
				dominio = System.getenv("USERDOMAIN");
			}
		} catch (UnknownHostException e) {
			logger.error("Error al intentar obtener el dominio: " + e.getMessage());
			logger.trace(e.getMessage());
		}
		return dominio;
	}

	/**
	 * Obtiene el nombre del host del sistema
	 * 
	 * @return
	 */
	private String getHostName() {
		String hostName = "";
		try {
			InetAddress address = InetAddress.getLocalHost();
			hostName = address.getCanonicalHostName();
			if (hostName.contains(".")) {
				hostName = Utils.obtenerCadIzquierda(hostName, ".");
			} else {
				logger.error("Error al intentar obtener el host name");
			}
		} catch (UnknownHostException e) {
			logger.error("Error al intentar obtener el host name: " + e.getMessage());
		}
		return hostName;
	}

	public char getCaracterBarra() {
		return '\\';
	}

	public String reemplazaExpRegArchivo(String nombre) {

		File directorio = new File(nombre.substring(0, nombre.lastIndexOf(OsServiceFactory.getOsService().getCaracterBarra()) + 1));

		String nombreAbsoluto = nombre.substring(nombre.lastIndexOf(OsServiceFactory.getOsService().getCaracterBarra()) + 1, nombre.length());

		final String partes[] = { Utils.obtenerCadIzquierda(nombreAbsoluto, "*"), Utils.obtenerCadDerecha(nombreAbsoluto, "*") };

		FilenameFilter filtro = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().startsWith(partes[0].toLowerCase()) && name.toLowerCase().endsWith(partes[1].toLowerCase());
			}
		};
		File lista[] = directorio.listFiles(filtro);
		if (lista != null && lista.length > 0) {
			return Utils.ultimoModificado(lista);
		}

		return nombre;

	}

	public String resuelveVariablesDeSistema(String mensaje) {
		String mensajeC = mensaje;

		while (mensajeC.contains("%")) {
			String sisVar = "";
			sisVar = mensajeC.substring(mensajeC.indexOf('%') + 1, mensajeC.substring(mensajeC.indexOf('%') + 1).indexOf('%') + mensajeC.indexOf('%') + 1);
			if (!sisVar.equals("")) {
				String sisVarValue = "";
				sisVarValue = System.getenv(sisVar);
				if (!sisVarValue.equals("")) {
					mensaje = mensaje.replace("%" + sisVar + "%", sisVarValue);
				}
			}
			mensajeC = mensajeC.substring(mensajeC.substring(mensajeC.indexOf('%') + 1).indexOf('%') + mensajeC.indexOf('%') + 2);
		}
		return mensaje;
	}
	
	public String escapaSaltosDeLinea(String cadena){
		
		return StringEscapeUtils.escapeXml(cadena).replaceAll("" + (char) 13 + (char) 10, "#x0a;");
	}
}

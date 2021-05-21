package ast.servicio.probatch.os.service.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.domain.UsuarioPermiso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.os.service.OsService;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.util.Utils;

public class LinuxService extends OsService {

	public static Logger logger = LoggerFactory.getLogger(LinuxService.class);

	public LinuxService() {
	}

	public int getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		int pid = 0;

		if (proceso.getClass().getName().equals("java.lang.UNIXProcess")) {
			/* get the PID on unix/linux systems */

			Field f = proceso.getClass().getDeclaredField("pid");
			f.setAccessible(true);
			pid = f.getInt(proceso);

		}

		return pid;
	}

	@Override
	public String[] getExecuteCommand(ParametrosProceso parametroP) throws Exception {
		int indx = parametroP.getArgumentos().size();
		String[] retorno = new String[indx + 4];
		int i = 0;
		retorno[i] = "sudo";
		i++;
		retorno[i] = "-u";
		i++;
		retorno[i] = parametroP.getUsuario().getNombre();
		i++;
		retorno[i] = parametroP.getComando();

		for (Iterator<Atributo> iterator = parametroP.getArgumentos().iterator(); iterator.hasNext();) {
			Atributo atributo = iterator.next();
			i++;
			retorno[i] = atributo.getValor();
		}
		return retorno;
	}

	@Override
	public String getKillCommand(int pid) {
		return "kill -9 " + pid;
	}

	@Override
	public boolean buscarUsuarioPermisos(String usuario, String permisos, String path, String dominio) throws MensajeErrorException {
		boolean resultado = false;
		if (usuario.equals("root")) {
			if (getEquivalenciasPermisos(permisos, "rwx")) {
				resultado = true;
			}
		}
		if (existeUsuario(usuario)) {
			if (usuario.equalsIgnoreCase(permisosUsuario(path).get(0).getUsuario())) {
				if (getEquivalenciasPermisos(permisos, permisosUsuario(path).get(0).getPermisos())) {
					resultado = true;
				}
			} else if (buscarGrupo(usuario, permisosUsuario(path).get(1).getUsuario())) {
				if (getEquivalenciasPermisos(permisos, permisosUsuario(path).get(1).getPermisos())) {
					resultado = true;
				}
			} else {
				if (getEquivalenciasPermisos(permisos, permisosUsuario(path).get(2).getPermisos())) {
					resultado = true;
				}
			}
		}
		return resultado;
	}

	/**
	 * 
	 * Arma una lista de UsuarioPermiso, con los usuarios y grupos y sus
	 * determinados permisos en un cierto path
	 * 
	 * @param path
	 * @return
	 * @throws MensajeErrorException
	 */
	private List<UsuarioPermiso> permisosUsuario(String path) throws MensajeErrorException {
		List<UsuarioPermiso> usuarioPermisos = new ArrayList<UsuarioPermiso>();
		// String aux = ejecutarComandoLinux("ls -al ", path);
		String aux = executeCommandOutputString(new String[] { "ls", "-al", path });
		String usuario;
		String permiso;

		// Siempre va a devolver una linea sola: Ejemplo
		// -rw-r--r-- 1 ubuntu ubuntu 56054 2012-03-09 08:35
		// /home/ubuntu/Downloads/Validar3.jar
		if (aux != null) {
			if (aux.startsWith("total")) {
				aux = aux.substring(aux.indexOf('\n') + 1, aux.length());

			}
			permiso = aux.substring(1, 4);
			permiso.replaceAll("-", "");
			usuario = Utils.obtenerTerceraCad(aux, " ");
			usuarioPermisos.add(new UsuarioPermiso(usuario, permiso, ""));
			permiso = aux.substring(4, 7);
			usuario = Utils.obtenerCuartaCad(aux, " ");
			permiso = permiso.replaceAll("-", "");
			usuarioPermisos.add(new UsuarioPermiso(usuario, permiso, ""));
			permiso = aux.substring(7, 10);
			permiso = permiso.replaceAll("-", "");
			usuario = "users";
			usuarioPermisos.add(new UsuarioPermiso(usuario, permiso, ""));
		}
		return usuarioPermisos;
	}

	/**
	 * 
	 * Valida si un usuario pertenece a un determinado grupo
	 * 
	 * @param usuario
	 * @param grupo
	 * @return
	 * @throws MensajeErrorException
	 */
	private boolean buscarGrupo(String usuario, String grupo) throws MensajeErrorException {
		String grupos = executeCommandOutputString(new String[] { "groups", usuario });
		boolean resultado = false;
		if (grupos.indexOf(grupo) > 0)
			resultado = true;
		return resultado;

	}

	/**
	 * Valida si el usuario es un usuario correcto del sistema.
	 * 
	 * @param usuario
	 * @return
	 * @throws MensajeErrorException
	 */
	private boolean existeUsuario(String usuario) throws MensajeErrorException {
		if (executeCommandOutputString(new String[] { "id", usuario }).length() > 30)
			return true;
		else
			return false;
	}

	@Override
	public boolean is_absolute_path(String x) {
		// TODO Auto-generated method stub
		return false;
	}

	public char getCaracterBarra() {
		return '/';
	}

	public String reemplazaExpRegArchivo(String nombre) {

		File directorio = new File(nombre.substring(0, nombre.lastIndexOf(OsServiceFactory.getOsService().getCaracterBarra()) + 1));

		String nombreAbsoluto = nombre.substring(nombre.lastIndexOf(OsServiceFactory.getOsService().getCaracterBarra()) + 1, nombre.length());

		final String partes[] = { Utils.obtenerCadIzquierda(nombreAbsoluto, "*"), Utils.obtenerCadDerecha(nombreAbsoluto, "*") };

		FilenameFilter filtro = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(partes[0]) && name.endsWith(partes[1]);
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

		while (mensajeC.contains("$")) {
			String sisVar = "";
			sisVar = mensajeC.substring(mensajeC.indexOf('$') + 1, mensajeC.substring(mensajeC.indexOf('$') + 1).indexOf(' ') + mensajeC.indexOf('$') + 1);
			if (!sisVar.equals("")) {
				String sisVarValue = "";
				sisVarValue = System.getenv(sisVar);
				if (sisVarValue != null && !sisVarValue.equals("")) {
					mensaje = mensaje.replace("$" + sisVar, sisVarValue);
				}
			}
			mensajeC = mensajeC.substring(mensajeC.substring(mensajeC.indexOf('$') + 1).indexOf(' ') + mensajeC.indexOf('$') + 2);
		}
		return mensaje;
	}

	public String escapaSaltosDeLinea(String cadena) {

		return StringEscapeUtils.escapeXml(cadena).replaceAll("" + (char) 10, "#x0a;");
	}
}

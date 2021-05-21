package ast.servicio.probatch.os.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.message.MensajeValidacion;

public abstract class OsService {

	public static Logger logger = LoggerFactory.getLogger(MensajeValidacion.class);

	public abstract int getPid(java.lang.Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException;

	public abstract boolean is_absolute_path(String x);

	public abstract String[] getExecuteCommand(ParametrosProceso parametroP) throws Exception;

	public abstract String getKillCommand(int pid);

	public abstract char getCaracterBarra();

	public abstract String reemplazaExpRegArchivo(String nombre);

	public Process executeCommand(String[] comandoCmd, String[] entorno, File dirEjecucion) throws IOException {
		return Runtime.getRuntime().exec(comandoCmd, entorno, dirEjecucion);
	}

	public Process executeCommand(String comandoCmd, String[] entorno, File dirEjecucion) throws IOException {
		return Runtime.getRuntime().exec(comandoCmd, entorno, dirEjecucion);
	}

	public Process executeCommand(String[] comandoCmd) throws IOException {
		return Runtime.getRuntime().exec(comandoCmd);
	}

	public Process executeCommand(String comandoCmd) throws IOException {
		return Runtime.getRuntime().exec(comandoCmd);
	}

	public String executeCommandOutputString(String[] comandos) throws MensajeErrorException {
		StringBuffer ret = new StringBuffer();
		try {
			Process proc = executeCommand(comandos);
			InputStream is = proc.getInputStream();
			int size;
			proc.waitFor();
			while ((size = is.available()) != 0) {
				byte[] b = new byte[size];
				is.read(b);
				ret.append(new String(b));
			}
		} catch (Exception e) {
			String comandosString = new String();
			for (int i = 0; i < comandos.length; i++) {
				comandosString = comandosString + comandos[i];
			}
			throw new MensajeErrorException("Error al ejecutar el comando - " + "[" + comandosString + "]");
		}
		return ret.toString();
	}

	public boolean getEquivalenciasPermisos(String permisosAVerificar, String permisosDevueltos) {
		boolean validacion = false;
		String permisosAVerificarMinuscula = permisosAVerificar.toLowerCase();
		String permisosDevueltosMinuscula = permisosDevueltos.toLowerCase();
		String cadAuxiliar = "";
		for (int i = 0; i < permisosAVerificar.length(); i++) {
			if (permisosAVerificarMinuscula.charAt(i) == 'r' && permisosDevueltosMinuscula.contains("r") && !cadAuxiliar.contains("r")) {
				validacion = true;
				cadAuxiliar = cadAuxiliar + "r";
			} else if (permisosAVerificarMinuscula.charAt(i) == 'x' && permisosDevueltosMinuscula.contains("x") && !cadAuxiliar.contains("x")) {
				validacion = true;
				cadAuxiliar = cadAuxiliar + "x";
			} else if (permisosAVerificarMinuscula.charAt(i) == 'w' && permisosDevueltosMinuscula.contains("w") && !cadAuxiliar.contains("w")) {
				validacion = true;
				cadAuxiliar = cadAuxiliar + "w";
			} else {
				validacion = false;
				break;
			}
		}
		return validacion;
	}

	/**
	 * 
	 * Valida si un usuario posee ciertos permisos (y con la posibilidad, en
	 * windows, de incluir dominio) en un determinado path (archivo,directorio)
	 * 
	 * @param usuario
	 * @param permisos
	 * @param path
	 * @param dominio
	 * @return
	 * @throws MensajeErrorException
	 */
	public abstract boolean buscarUsuarioPermisos(String usuario, String permisos, String path, String dominio) throws MensajeErrorException;

	public abstract String resuelveVariablesDeSistema(String mensaje);

	public abstract String escapaSaltosDeLinea(String cadena);
}

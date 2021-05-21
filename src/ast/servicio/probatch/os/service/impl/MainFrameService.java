package ast.servicio.probatch.os.service.impl;

import org.apache.commons.lang3.StringEscapeUtils;

import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.os.service.OsService;

public class MainFrameService extends OsService {

	public MainFrameService() {
	}

	@Override
	public boolean is_absolute_path(String x) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getExecuteCommand(ParametrosProceso parametroP) throws Exception {
		String[] retorno = new String[] { "", "" };
		return retorno;
	}

	@Override
	public String getKillCommand(int pid) {
		return "kill -9 " + pid;
	}

	@Override
	public boolean buscarUsuarioPermisos(String usuario, String permisos, String path, String dominio) throws MensajeErrorException {
		// TODO Auto-generated method stub
		return false;
	}

	public char getCaracterBarra() {
		return '/';
	}

	public String reemplazaExpRegArchivo(String nombre) {

		return nombre;

	}

	public String resuelveVariablesDeSistema(String mensaje) {
		return null;
	}

	public String escapaSaltosDeLinea(String cadena) {

		return StringEscapeUtils.escapeXml(cadena).replaceAll("" + (char) 10, "#x0a;");
	}
}

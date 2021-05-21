package ast.servicio.probatch.os.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.domain.UsuarioPermiso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.message.MensajeError;
import ast.servicio.probatch.message.MensajeValidacion;
import ast.servicio.probatch.os.service.OsService;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.Permission;
import com.ibm.as400.access.QSYSPermission;
import com.ibm.as400.access.RootPermission;
import com.ibm.as400.access.User;

public class AS400Service extends OsService {

	public static Logger logger = LoggerFactory.getLogger(MensajeValidacion.class);

	public AS400Service() {
	}

	@Override
	public boolean is_absolute_path(String x) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Valida si el usuario es un usuario correcto del sistema.
	 * 
	 * @param usuario
	 * @return
	 * @throws MensajeErrorException
	 * @throws ObjectDoesNotExistException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ErrorCompletingRequestException
	 * @throws AS400SecurityException
	 */
	private boolean existeUsuario(String usuario) throws MensajeErrorException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException,
			IOException, ObjectDoesNotExistException {

//		logger.debug("****** Inicio metodo existeUsuario ********");
//		logger.debug("Parametros Usuario: " + usuario);
		AS400 conection = new AS400();

		User usuarioAS400 = new User(conection, usuario);

		if (usuarioAS400.exists()) {
//			logger.debug("Existe usuario: " + usuarioAS400.exists());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getPid(Process proceso) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getExecuteCommand(ParametrosProceso parametroP) throws Exception {

		StringBuffer comandos = new StringBuffer();
		comandos.append(parametroP.getComando());
		comandos.append(" ");
		comandos.append(parametroP.getArgumentosAS400String());
		String[] retorno = new String[] { comandos.toString() };
		return retorno;
	}

	@Override
	public String getKillCommand(int pid) {
		return "";
	}

	public boolean buscarUsuarioPermisos(String usuario, String permisos, String path, String dominio) throws MensajeErrorException {

		boolean resultado = false;

		// logger.debug("**************** Inicio metodo buscarUsuarioPermisos");
		try {
			if (existeUsuario(usuario)) {

				// logger.debug("******* Existe Usuario");
				List<UsuarioPermiso> usuarioPermisos = permisosUsuario(path);

				// logger.debug("******** Se ejecuto el metodo permisosUsuario con le parametro "
				// + path);
				if (usuarioPermisos != null) {
					for (int i = 0; i < usuarioPermisos.size(); i++) {

						// logger.debug("Usuario: " +
						// usuarioPermisos.get(i).getUsuario());
						if (usuario.equalsIgnoreCase(usuarioPermisos.get(i).getUsuario())) {

							// logger.debug("*****Conincide el usuario: " +
							// usuarioPermisos.get(i).getUsuario());
							if (getEquivalenciasPermisos(permisos, usuarioPermisos.get(i).getPermisos())) {
								return true;
							} else {
								return false;
							}

						}
					}
					// logger.debug(usuarioPermisos.get(0).getListaAS());
					if (usuarioPermisos.get(0).getListaAS() != null && !usuarioPermisos.get(0).getListaAS().equalsIgnoreCase("*NONE")) {

						String pathLista = "/QSYS.LIB/" + usuarioPermisos.get(0).getListaAS() + ".AUTL";
						return buscarUsuarioPermisos(usuario, permisos, pathLista, dominio);
					} else {

						for (int i = 0; i < usuarioPermisos.size(); i++) {

							// logger.debug("Usuario public: " +
							// usuarioPermisos.get(i).getUsuario());
							if (usuarioPermisos.get(i).getUsuario().contains("PUBLIC")) {

								// logger.debug("Usuario public: " +
								// usuarioPermisos.get(i).getUsuario());
								if (getEquivalenciasPermisos(permisos, usuarioPermisos.get(i).getPermisos())
										|| usuarioPermisos.get(i).getPermisos().equalsIgnoreCase("*ALL")) {
									// logger.debug("Coinciden permisos");
									return true;
								} else {
									return false;
								}

							}
						}

					}
				}
			}
		} catch (Exception e) {

			throw new MensajeErrorException(e.getMessage());
		}
		return resultado;
	}

	/**
	 * 
	 * Arma una lista de UsuarioPermiso, con los usuarios, grupos y los permisos
	 * para path introducido por paramettro
	 * 
	 * @param path
	 * @return
	 * @throws MensajeErrorException
	 */
	private List<UsuarioPermiso> permisosUsuario(String path) throws MensajeErrorException {
		List<UsuarioPermiso> usuarioPermisos = new ArrayList<UsuarioPermiso>();

//		logger.debug("********* Inicio del metodo permisosUsuario");
		// // String aux = ejecutarComandoLinux("ls -al ", path);
		String aux = executeCommandOutputStringAS400(path);
		// // Siempre va a devolver una linea sola: Ejemplo
		// // *PUBLIC:*RWX ACCUSYS:*RWX QDIRSRV:*X
		// // usuario:*permisos
		if (aux != null) {

			int puntoComaIndex = aux.indexOf(";");
			String listaAS = aux.substring(puntoComaIndex + 1);
			aux = aux.substring(0, puntoComaIndex);
			String[] usuarioPermisoVector = aux.split(" ");

			for (int i = 0; i < usuarioPermisoVector.length; i++) {

				String[] usuarioPermisosSplit = usuarioPermisoVector[i].split(":");

				UsuarioPermiso usuarioPermiso = new UsuarioPermiso();
				usuarioPermiso.setUsuario(usuarioPermisosSplit[0]);
				usuarioPermiso.setPermisos(usuarioPermisosSplit[1]);
				usuarioPermiso.setListaAS(listaAS);
				usuarioPermisos.add(usuarioPermiso);

			}
		}
		// for (int i = 0; i < usuarioPermisos.size(); i++) {
		//
		// logger.debug(usuarioPermisos.get(i).getUsuario() +
		// usuarioPermisos.get(i).getPermisos());
		// }

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

	/*
	 * Como parametro de entrada recibe un array de String que representa cada
	 * carpeta o archivo al que se le verificara los permisos
	 */

	public String executeCommandOutputStringAS400(String path) throws MensajeErrorException {

		StringBuffer ret = new StringBuffer();
		AS400 sys = new AS400();
//		logger.debug("***** Inicio del metodo executeCommandOutputStringAS400");
		try {

//			logger.debug("******** Parametros para inicializar la clase: " + sys.toString() + " " + path);
			Permission misPermisos = new Permission(sys, path);

//			logger.debug("*********** Se instancio la clase Permission");
			if (Permission.TYPE_ROOT == misPermisos.getType()) {
				Enumeration enumm = misPermisos.getUserPermissions();

				while (enumm.hasMoreElements()) {
					RootPermission tipoPermiso = (RootPermission) enumm.nextElement();

					ret.append(tipoPermiso.getUserID());
					ret.append(":");
					ret.append(tipoPermiso.getDataAuthority());
					ret.append(" ");
				}
				ret.append(";");
				ret.append(misPermisos.getAuthorizationList());
			} else if (Permission.TYPE_QSYS == misPermisos.getType()) {

				Enumeration enumm = misPermisos.getUserPermissions();

				while (enumm.hasMoreElements()) {
					QSYSPermission tipoPermiso = (QSYSPermission) enumm.nextElement();

					ret.append(tipoPermiso.getUserID());
					ret.append(":");
					ret.append(tipoPermiso.getObjectAuthority());
					ret.append(" ");
				}
				ret.append(";");
				ret.append(misPermisos.getAuthorizationList());
			} else {

				throw new MensajeErrorException("Error, el path no tiene el formato esperado");
			}
		} catch (Exception e) {

//			logger.debug(e.getMessage());
			logger.error(new MensajeError("Error al solicitar permisos").getTramaString());
			throw new MensajeErrorException("Error al solicitar permisos");
		} finally {
			try {
				sys.disconnectAllServices();

			} catch (Exception e) {

				throw new MensajeErrorException("Error al cerrar la conexion con el servidor AS400");
			}
		}
		return ret.toString();
	}

	public Process executeCommandAS400(String[] comandoCmd) throws IOException {
		return Runtime.getRuntime().exec(comandoCmd);
	}

	public boolean getEquivalenciasPermisos(String permisosAVerificar, String permisosObtenidos) {
		boolean validacion = false;
		String permisosAVerificarMinuscula = permisosAVerificar.toLowerCase();
		String permisosObtenidosMinuscula = permisosObtenidos.toLowerCase();
		String permisosEquivalentes = convertirPermisosAS400(permisosAVerificar);
//		logger.debug("**** Permisos obtenidos: " + permisosObtenidos.toLowerCase() + " *********");
//		logger.debug("***** Permisos Equivalentes: " + permisosEquivalentes + "*******");
		String cadAuxiliar = "";

		if (permisosObtenidosMinuscula.contains("none") || permisosObtenidosMinuscula.contains("exclude")) {

			return false;
		} else if (permisosEquivalentes.equalsIgnoreCase(permisosObtenidos)) {

//			logger.debug("***** Salio por true ******");
			return true;
		}

//		logger.debug("****** Va el bucle *****");
		for (int i = 0; i < permisosAVerificar.length(); i++) {
			if (permisosAVerificarMinuscula.charAt(i) == 'r' && permisosObtenidosMinuscula.contains("r") && !cadAuxiliar.contains("r")) {
				validacion = true;
				cadAuxiliar = cadAuxiliar + "r";
			} else if (permisosAVerificarMinuscula.charAt(i) == 'x' && permisosObtenidosMinuscula.contains("x") && !cadAuxiliar.contains("x")) {
				validacion = true;
				cadAuxiliar = cadAuxiliar + "x";
			} else if (permisosAVerificarMinuscula.charAt(i) == 'w' && permisosObtenidosMinuscula.contains("w") && !cadAuxiliar.contains("w")) {
				validacion = true;
				cadAuxiliar = cadAuxiliar + "w";
			} else {
				validacion = false;
				break;
			}
		}
		return validacion;
	}

	public String convertirPermisosAS400(String permisos) {

		if (permisos.equalsIgnoreCase("r") || permisos.equalsIgnoreCase("x") || permisos.equalsIgnoreCase("rx")) {
			return "*USE";
		} else if (permisos.equalsIgnoreCase("w") || permisos.equalsIgnoreCase("rw") || permisos.equalsIgnoreCase("wx")) {
			return "*CHANGE";
		} else
			return "*ALL";
	}

}

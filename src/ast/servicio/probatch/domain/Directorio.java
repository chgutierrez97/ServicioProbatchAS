package ast.servicio.probatch.domain;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.message.MensajeValidacion;

/**
 * Clase que para guardar de a un directorio leido en la trama xml de validar
 * con todos sus archivos, ya que posee una lista de archivos. Para usar en la
 * clase MensajeValidacion
 * 
 * @author rodrigo.guillet
 * 
 */
public class Directorio {
	public static Logger logger = LoggerFactory.getLogger(MensajeValidacion.class);
	String idDirectorio;
	String pathDirectorio;
	String pathDirMostrar;
	String usuarioDominioDirectorio;
	String usuarioDirectorio;
	String claveDirectorio;
	String permisoDirectorio;
	String dominioDirectorio;

	List<Archivo> archivos = new ArrayList<Archivo>();

	public Directorio(String idDirectorio, String pathDirectorio, String pathDirMostrar, String usuarioDominioDirectorio, String usuarioDirectorio,
			String permisoDirectorio, String claveDirectorio, String dominioDirectorio) {

		super();
		this.idDirectorio = idDirectorio;
		this.pathDirectorio = pathDirectorio;
		this.pathDirMostrar = pathDirMostrar;
		this.usuarioDirectorio = usuarioDirectorio;
		this.permisoDirectorio = permisoDirectorio;
		this.claveDirectorio = claveDirectorio;
		this.dominioDirectorio = dominioDirectorio;
		this.usuarioDominioDirectorio = usuarioDominioDirectorio;

	}

	public String getPathDirMostrar() {
		return pathDirMostrar;
	}

	public void setPathDirMostrar(String pathDirMostrar) {
		this.pathDirMostrar = pathDirMostrar;
	}

	public String getDominioDirectorio() {
		return dominioDirectorio;
	}

	public void setDominioDirectorio(String dominioDirectorio) {
		this.dominioDirectorio = dominioDirectorio;
	}

	public List<Archivo> getArchivos() {
		return archivos;
	}

	/**
	 * Agrega de a un archivo a la lista de archivos (List<Archivo> archivos),
	 * la diferencia con el setter es que este metodo permite agregar una
	 * cantidad x de archivos (agregando de a un solor archivo), cada vez que se
	 * llama a este metodo dentro del mismo directorio, agrega de a un archivo
	 * en esa lista.
	 * 
	 * @param idArchivo
	 * @param usuarioArchivo
	 * @param claveArchivo
	 * @param permisoArchivo
	 * @param nombreArchivo
	 * @throws MensajeErrorException
	 */
	public void agregarArchivo(String idArchivo, String usuarioArchivo, String claveArchivo, String permisoArchivo, String usuarioDominioArchivo,
			String nombreArchivo, String dominioArchivo) throws MensajeErrorException {
		String pathArchivo = "";

		if (!pathDirectorio.equals("")) {
			pathArchivo = pathDirectorio + "/" + nombreArchivo;
		}
		if (pathArchivo.endsWith("\\") || pathArchivo.endsWith("/")) {
			pathArchivo = pathArchivo.substring(0, pathArchivo.length() - 1);
		}

		archivos.add(new Archivo(idArchivo, usuarioArchivo, claveArchivo, permisoArchivo, pathArchivo, nombreArchivo, usuarioDominioArchivo, dominioArchivo));

	}

	public String getIdDirectorio() {
		return idDirectorio;
	}

	public void setIdDirectorio(String idDirectorio) {
		this.idDirectorio = idDirectorio;
	}

	public String getPathDirectorio() {
		return pathDirectorio;
	}

	public void setPathDirectorio(String pathDirectorio) {
		this.pathDirectorio = pathDirectorio;
	}

	public String getUsuarioDirectorio() {
		return usuarioDirectorio;
	}

	public void setUsuarioDirectorio(String usuarioDirectorio) {
		this.usuarioDirectorio = usuarioDirectorio;
	}

	public String getClaveDirectorio() {
		return claveDirectorio;
	}

	public void setClaveDirectorio(String claveDirectorio) {
		this.claveDirectorio = claveDirectorio;
	}

	public String getPermisoDirectorio() {
		return permisoDirectorio;
	}

	public void setPermisoDirectorio(String permisoDirectorio) {
		this.permisoDirectorio = permisoDirectorio;
	}

	public void setArchivos(List<Archivo> archivos) {
		this.archivos = archivos;
	}

	public String getUsuarioDominioDirectorio() {
		return usuarioDominioDirectorio;
	}

	public void setUsuarioDominioDirectorio(String usuarioDominioDirectorio) {
		this.usuarioDominioDirectorio = usuarioDominioDirectorio;
	}
}

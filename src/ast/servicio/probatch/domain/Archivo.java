package ast.servicio.probatch.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.message.MensajeValidacion;

/**
 * Clase que para guardar de a un archivo leido en la trama xml de validar. Para
 * usar en la clase Directorio y en la clase MensajeValidacion
 * 
 * @author rodrigo.guillet
 * 
 */
public class Archivo {
	public static Logger logger = LoggerFactory.getLogger(MensajeValidacion.class);
	String idArchivo;
	String usuarioDominioArchivo;
	String usuarioArchivo;
	String claveArchivo;
	String permisoArchivo;
	String pathArchivo;
	String nombreArchivo;
	String dominioArchivo;

	public String getId() {
		return idArchivo;
	}

	public void setId(String id) {
		this.idArchivo = id;
	}

	public String getUsuario() {
		return usuarioArchivo;
	}

	public void setUsuario(String usuario) {
		this.usuarioArchivo = usuario;
	}

	public String getClave() {
		return claveArchivo;
	}

	public void setClave(String clave) {
		this.claveArchivo = clave;
	}

	public String getPermiso() {
		return permisoArchivo;
	}

	public void setPermiso(String permiso) {
		this.permisoArchivo = permiso;
	}

	public String getDominioArchivo() {
		return dominioArchivo;
	}

	public void setDominioArchivo(String dominioArchivo) {
		this.dominioArchivo = dominioArchivo;
	}

	public String getPath() {
		return pathArchivo;
	}

	public void setPath(String path) {
		this.pathArchivo = path;
	}

	public String getNombre() {
		return nombreArchivo;
	}

	public void setNombre(String nombre) {
		this.nombreArchivo = nombre;
	}

	public String getUsuarioDominioArchivo() {
		return usuarioDominioArchivo;
	}

	public void setUsuarioDominioArchivo(String usuarioDominioArchivo) {
		this.usuarioDominioArchivo = usuarioDominioArchivo;
	}

	public Archivo(String id, String usuario, String clave, String permiso, String path, String nombre, String usuarioDominioArchivo, String dominio) {
		
		super();
		this.idArchivo = id;
		this.usuarioArchivo = usuario;
		this.claveArchivo = clave;
		this.permisoArchivo = permiso;
		this.pathArchivo = path;
		this.nombreArchivo = nombre;
		this.usuarioDominioArchivo = usuarioDominioArchivo;
		this.dominioArchivo = dominio;
	}

}
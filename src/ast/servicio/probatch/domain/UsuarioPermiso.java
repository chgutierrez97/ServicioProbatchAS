package ast.servicio.probatch.domain;

public class UsuarioPermiso {
	// ultimo
	String usuario;
	String permisos;
	String listaAS;
	String dominio;

	public String getDominio() {
		return dominio;
	}

	public void setDominio(String dominio) {
		this.dominio = dominio;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getPermisos() {
		return permisos;
	}

	public void setPermisos(String permisos) {
		this.permisos = permisos;
	}

	public String getListaAS() {
		return listaAS;
	}

	public void setListaAS(String listaAS) {
		this.listaAS = listaAS;
	}

	public UsuarioPermiso() {

	}

	public UsuarioPermiso(String usuario, String permisos, String dominio) {
		this.usuario = usuario;
		this.permisos = permisos;
		this.dominio = dominio;
	}
	
	public UsuarioPermiso(String usuario, String dominio) {
		this.usuario = usuario;
		this.dominio = dominio;
	}

}

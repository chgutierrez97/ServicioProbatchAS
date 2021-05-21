package ast.servicio.probatch.domain;

import ast.servicio.probatch.util.Utils;

public class Atributo {

	private String tipo;
	private String nombre;
	private String valor;
	private String valorMostrar;
	private String controlar_todos;
	private String leer;
	private String adjuntar_resultado;

	public Atributo() {
		tipo = "";
		nombre = "";
		valor = "";
		valorMostrar = "";
		controlar_todos = "";
		leer = "";
		adjuntar_resultado = "";
	}

	public Atributo(String name, String value) {
		tipo = "";
		nombre = name;
		valor = value;
		valorMostrar = value;
		controlar_todos = "";
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		valor = valor.replace("%d", Utils.seccionaFecha("dd"));
		valor = valor.replace("%m", Utils.seccionaFecha("MM"));
		valor = valor.replace("%y", Utils.seccionaFecha("yy"));
		valor = valor.replace("%Y", Utils.seccionaFecha("yyyy"));
		this.valor = valor;
	}

	public String getValorMostrar() {
		return valorMostrar;
	}

	public void setValorMostrar(String valorMostrar) {
		this.valorMostrar = valorMostrar;
	}

	public String getControlar_todos() {
		return controlar_todos;
	}

	public void setControlar_todos(String controlarTodos) {
		controlar_todos = controlarTodos;
	}

	public String getLeer() {
		return leer;
	}

	public void setLeer(String leer) {
		this.leer = leer;
	}

	public String getAdjuntar_resultado() {
		return adjuntar_resultado;
	}

	public void setAdjuntar_resultado(String adjuntarResultado) {
		adjuntar_resultado = adjuntarResultado;
	}

}

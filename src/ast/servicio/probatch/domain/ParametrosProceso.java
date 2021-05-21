package ast.servicio.probatch.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ast.servicio.probatch.os.service.OsServiceFactory;

public class ParametrosProceso {

	public static final String OCULTO = "oculto";
	public static final String FATAL = "fatal";
	public static final String IGNORAR = "ignorar";
	public static final String ENTRADA = "entrada";
	public static final String SALIDA = "salida";

	private long ts;

	private String id; // numerico

	private String nombre; // tiene que existir

	private String categoria;

	private String clase;

	private Atributo usuario;

	private String chdir; // existir validar si existe????

	private String comando; // existir

	private Atributo resultado;

	private int pid;

	private Collection<Atributo> argumentos;

	private Collection<Atributo> entorno;

	private Collection<Atributo> interfaces;

	private Collection<Atributo> patrones;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public Atributo getUsuario() {
		return usuario;
	}

	public void setUsuario(Atributo usuario) {
		this.usuario = usuario;
	}

	public String getChdir() {
		return chdir;
	}

	public void setChdir(String chdir) {
		this.chdir = chdir;
	}

	public Collection<Atributo> getPatrones() {
		return patrones;
	}

	public void setPatrones(Collection<Atributo> patrones) {
		this.patrones = patrones;
	}

	public Collection<Atributo> getArgumentos() {
		return argumentos;
	}

	public String getArgumentosString() {
		String argumentos = "";
		if (getArgumentos() != null || !getArgumentos().isEmpty()) {
			for (Iterator<Atributo> iterator = getArgumentos().iterator(); iterator.hasNext();) {
				Atributo argumento = (Atributo) iterator.next();
				argumentos = argumentos + argumento.getValor() + " - ";

			}

		}

		return argumentos;
	}

	public String getArgumentosAS400String() {
		String argumentos = "";
		if (getArgumentos() != null || !getArgumentos().isEmpty()) {
			for (Iterator<Atributo> iterator = getArgumentos().iterator(); iterator.hasNext();) {
				Atributo argumento = (Atributo) iterator.next();
				argumentos = argumentos + argumento.getValor() + " ";

			}

		}

		return argumentos;
	}
	
	public void setArgumentos(Collection<Atributo> argumentos) {
		this.argumentos = argumentos;
	}

	public String getComando() {
		return comando;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public Atributo getResultado() {
		return resultado;
	}

	public void setResultado(Atributo resultado) {
		resultado.setValor(resultado.getValor());
		resultado.setValor(resultado.getValor());
		resultado.setValor(resultado.getValor());
		resultado.setValor(resultado.getValor());
		if (resultado.getValor().contains("*")) {
			if (!OsServiceFactory.getOsService().reemplazaExpRegArchivo(resultado.getValor()).equals(resultado.getValor())) {
				resultado.setValor(OsServiceFactory.getOsService().reemplazaExpRegArchivo(resultado.getValor()));
			} else {
				resultado.setValor("");
			}
		}
		this.resultado = resultado;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public void setComando(String comando) {
		this.comando = comando;
	}

	public Collection<Atributo> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Collection<Atributo> interfaces) {
		this.interfaces = interfaces;
	}

	public Collection<Atributo> getEntorno() {
		return entorno;
	}

	public String[] getVarEntornoArray() {
		String[] varEntorno = null;
		if (getEntorno() != null || !getEntorno().isEmpty()) {
			varEntorno = new String[getEntorno().size()];
			ArrayList<Atributo> listaEntorno = (ArrayList<Atributo>) getEntorno();
			for (int i = 0; i < listaEntorno.size(); i++) {
				Atributo var = listaEntorno.get(i);
				String stringVar = var.getNombre() + "=" + var.getValor();
				varEntorno[i] = stringVar;
			}
		}
		return varEntorno;
	}

	public String getVarEntornoString() {
		String variableEntorno = "";

		if (getEntorno() != null && !getEntorno().isEmpty()) {

			for (Iterator<Atributo> iterator = getEntorno().iterator(); iterator.hasNext();) {
				Atributo varEntorno = (Atributo) iterator.next();
				variableEntorno = variableEntorno + varEntorno.getNombre() + "=" + varEntorno.getValorMostrar() + " ";
			}
			variableEntorno = variableEntorno.substring(0, variableEntorno.length() - 1);
		}
		return variableEntorno;
	}

	public void setEntorno(Collection<Atributo> entorno) {
		this.entorno = entorno;
	}

	@Override
	public String toString() {

		String retCadena = "";

		String[] cmd = new String[] { "" };
		try {
			cmd = (OsServiceFactory.getOsService().getExecuteCommand(this));
		} catch (Exception e) {

		}
		for (int i = 0; i < cmd.length; i++) {
			if (i == 3 && System.getProperty("os.name").toLowerCase().contains("win")) {
				retCadena = retCadena + "******** ";
			} else {
				retCadena = retCadena + cmd[i] + " ";
			}

		}

		retCadena = retCadena.substring(0, retCadena.length() - 1);
		return retCadena = retCadena + "; " + (!getVarEntornoString().equals("") ? "export " + getVarEntornoString() + "; " : "") + "exec " + getComando()
				+ "'";

	}

}
package ast.servicio.probatch.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.util.Utils;

public class estadoGuardado {
	String xmlFileName = "";
	String cadenaOriginal = leerTxt();

	public estadoGuardado(String xmlFileName) {

		this.xmlFileName = xmlFileName;
	}

	private List<String> listaEMTOString(List<EstadoProceso> listaEM) {
		List<String> listaResultado = new ArrayList<String>();

		for (EstadoProceso em : listaEM) {
			listaResultado.add(estadoMensajeToString(em));
		}
		
		return listaResultado;
	}

	private String leerTxt() {
		String resultado = "";
		try {

			if (Utils.validarExistenciaArchivo(xmlFileName)) {

				FileReader fr = new FileReader(xmlFileName);
				BufferedReader bf = new BufferedReader(fr);

				String sCadena;

				while ((sCadena = bf.readLine()) != null) {
					if (sCadena != null)
						resultado = resultado + sCadena;
				}
			}
			if (resultado != "")
				resultado = resultado.substring(17, resultado.length() - 18);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultado;
	}

	public void escribir() {
		try {
			FileWriter fw = new FileWriter(xmlFileName);
			fw.write(textoConTag());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String textoConTag() {
		String cadenaFinalizada = "<estado-guardado>";
		cadenaFinalizada = cadenaFinalizada + cadenaOriginal + "</estado-guardado>";
		return cadenaFinalizada;
	}

	public String textoSinTag() {
		return cadenaOriginal;
	}

	public void agregarEstado(EstadoProceso em) {
		String cadenaAAgregar = estadoMensajeToString(em);
		List<String> original = armarListaTransiciones(cadenaOriginal);
		if (existeId(original, obtenerId(cadenaAAgregar))) {
			System.out.println("Existe ID");
			cadenaOriginal = eliminar(obtenerId(cadenaAAgregar));
		}

		cadenaOriginal = cadenaOriginal + cadenaAAgregar;
	}
	
	
	public void agregarListaEstado (List <EstadoProceso> listaEstadoMensaje)
	{
		for (EstadoProceso em: listaEstadoMensaje){
			agregarEstado(em);
		}
	}

	private String estadoMensajeToString(EstadoProceso em) {

		String resultado = "<transicion id=";
		String id = em.getId();
		String nombre = em.getNombre();
		long ts = em.getTs();
		Integer estado = em.getEstado();
		int valor = em.getPid();

		resultado = resultado + "\"" + id + "\"" + " " + "nombre=" + "\"" + nombre + "\"" + " " + "ts=" + "\"" + ts + "\"><fin estado=\"" + estado
				+ "\"" + " " + "valor=" + "\"" + valor + "\"/></transicion>";

		return resultado;
	}

	private List<String> armarListaTransiciones(String resultado) {

		resultado = resultado.replaceAll("</transicion>", "</transicion>;");

		return Utils.obtenerCadenas(resultado, ";");
	}

	private String obtenerId(String cad) {
		String conseguirId = cad.substring(cad.indexOf("id"));
		conseguirId = conseguirId.substring(conseguirId.indexOf('"') + 1, conseguirId.indexOf('"', conseguirId.indexOf('"') + 1));
		return conseguirId;
	}

	private boolean existeId(List<String> lista, String idBuscar) {
		boolean resultado = false;
		String idObtenido;
		for (String string : lista) {
			idObtenido = obtenerId(string);
			if (idBuscar.equals(idObtenido)) {
				resultado = true;
				break;
			}
		}

		return resultado;
	}

	public void eliminarTransicion(String id) {
		cadenaOriginal = eliminar(id);
	}

	private String eliminar(String id) {
		String resultado = "";
		List<String> lista = armarListaTransiciones(cadenaOriginal);

		for (String string : lista) {
			if (!obtenerId(string).equals(id)) {
				resultado = resultado + string;
			}
		}

		return resultado;
	}

	public void mostrarEstado(String id) {
		System.out.println(obtenerEstado(id, cadenaOriginal) != null ? obtenerEstado(id, cadenaOriginal) : "No existe ID");
	}

	private String obtenerEstado(String id, String cad) {
		String estadoObtenido = null;
		List<String> lista = armarListaTransiciones(cad);
		if (existeId(lista, id)) {
			for (String string : lista) {
				if (obtenerId(string).equals(id)) {
					estadoObtenido = string;
					break;
				}

			}

		}

		return estadoObtenido;
	}

	public void mostrarLista() {
		System.out.println("COMIENZA LISTA");
		for (String string : armarListaTransiciones(cadenaOriginal)) {
			System.out.println(string);
		}
	}

}

package ast.servicio.probatch.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.util.Utils;

public class UtilsEstadoGuardado {
	public static String leerTxt(String xmlFileName) throws IOException {
		String resultado = "";
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
		return resultado;
	}

	
	
	public static void escribir(String fileName, String cadenaGuardar) throws IOException {
		String cadenaGuardarFinalizada = "<estado-guardado>";
		cadenaGuardarFinalizada = cadenaGuardarFinalizada + cadenaGuardar + "</estado-guardado>";
		FileWriter fw = new FileWriter(fileName);
		fw.write(cadenaGuardarFinalizada);
		fw.close();

	}

	public static String agregarCadena(String cadenaAAgregar, String cadenaOriginal) {
		String resultado = cadenaOriginal;
		List<String> original = armarListaTransiciones(cadenaOriginal);
		if (existeId(original, obtenerId(cadenaAAgregar))) {
			System.out.println("Existe ID");
			resultado = eliminar(obtenerId(cadenaAAgregar), resultado);
		}
			
			resultado = resultado + cadenaAAgregar;
		

		return resultado;

	}
	
	public static void mostrarEstado(String id,String cad){
		System.out.println(obtenerEstado(id,cad)!= null ? obtenerEstado(id,cad) : "No existe ID");
	}
	
	
	private static String obtenerEstado(String id,String cad){
		String estadoObtenido = null;
		List<String> lista = armarListaTransiciones(cad);
		if(existeId(lista, id)){
			for (String string : lista) {
				if(obtenerId(string).equals(id)){
					estadoObtenido = string;
					break;
				}
				
			}
			
		}
				
		
		return estadoObtenido;
	}

	
	
	public static String estadoMensajeToString (EstadoProceso em){
		
		String resultado = "<transicion id=";
		String id = em.getId();
		String nombre = em.getNombre();
		long ts = em.getTs();
		Integer estado = em.getEstado();
		int valor = em.getPid();
		
		resultado = resultado + "\"" + id + "\"" + " " + "nombre=" + "\"" + nombre + "\"" + " " + "ts=" + "\"" + ts + "\"><fin estado=\""+ (estado != null? estado : "") + "\"" + " "  + "valor=" + "\"" + valor + "\"/></transicion>";
		
		
		return resultado;
	}
	
	
	public static String eliminar(String id, String cadena) {
		String resultado = "";
		List<String> lista = armarListaTransiciones(cadena);

		for (String string : lista) {
			if (!obtenerId(string).equals(id)) {
				resultado = resultado + string;
			}
		}

		return resultado;
	}

	public static void leer() {

		try {
			Document doc;
			String xml;
			String xmlFileName = "C:/Users/rodrigo.guillet/Desktop/xmlprueba.xml";
			if (Utils.validarExistenciaArchivo(xmlFileName)) {
				File fXmlFile = new File(xmlFileName);
				System.out.println("EXISTE ARCHIVO");
				// File fXmlFile = new
				// File("/home/ubuntu/Desktop/validar3.xml");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				FileInputStream fis = new FileInputStream(fXmlFile);
				byte[] contenido = new byte[fis.available()];
				fis.read(contenido);
				String contenido2 = new String(contenido);
				contenido2 = contenido2.substring(62, contenido2.length() - 19);
				doc = dBuilder.parse(fXmlFile);
				System.out.println(contenido2);

				doc.getDocumentElement().normalize();
				String raiz = doc.getDocumentElement().getNodeName();

				if (raiz.equals("estado-guardado")) {
					xml = parseValidar(doc);
					System.out.println("la cadena seria:");
					System.out.println(xml);
				} else {
					System.out.println("error_estado-guardado");
					System.out.println("En el xml no se encuentra el tag <estado-guardado>");
				}
			} else
				System.out.println("NO EXISTE ARCHIVO");
		} catch (Exception e) {

		}
	}

	private static String parseValidar(Document doc) {

		String resultado;

		Element raiz = doc.getDocumentElement();

		resultado = raiz.getTextContent();
		return resultado;
	}

	public static Document parsearMensaje(String xmlMessage) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document tramaXml = builder.parse(new InputSource(new StringReader(xmlMessage)));
			return tramaXml;
		} catch (Exception e) {
			return null;
		}

	}

	public static List<String> armarListaTransiciones(String resultado) {

		resultado = resultado.replaceAll("</transicion>", "</transicion>;");

		return Utils.obtenerCadenas(resultado, ";");
	}

	public void mostrarLista(List<String> lista) {
		System.out.println("COMIENZA LISTA");
		for (String string : lista) {
			System.out.println(string);
		}
	}

	public static boolean existeId(List<String> lista, String idBuscar) {
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



	public static String obtenerId(String cad) {
		String conseguirId = cad.substring(cad.indexOf("id"));
		conseguirId = conseguirId.substring(conseguirId.indexOf('"') + 1, conseguirId.indexOf('"', conseguirId.indexOf('"') + 1));
		return conseguirId;
	}

	

}

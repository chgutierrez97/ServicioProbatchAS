package ast.servicio.probatch.message;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import ast.servicio.probatch.domain.Archivo;
import ast.servicio.probatch.domain.Directorio;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.util.Utils;

/**
 * Se valida si un determinado usuario tiene ciertos permisos en un
 * archivo/directorio determinado.
 * 
 * @author rodrigo.guillet
 * 
 */
public class MensajeValidacion extends Mensaje {
	List<Directorio> listaDirectorios = new ArrayList<Directorio>();

	public static Logger logger = LoggerFactory.getLogger(MensajeValidacion.class);
	private static final String OS_NAME = "os.name";
	private static String sistemaOperativo = System.getProperty(OS_NAME);

	public MensajeValidacion(String mensajeEntrada) {
		super(mensajeEntrada);
	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {

		armarListaDirectorios(this.getTramaXml());
		Mensaje respuesta = MessageFactory.crearMensajeRespuesta(validarPermisos());
		return respuesta;
	}

	/**
	 * Llena la listaDirectorios (List<Directorio>) declarada anteriormente, con
	 * los directorios y archivos que va obteniendo de una determinada tramaXML
	 * 
	 * @param doc
	 * @throws MensajeErrorException
	 */
	private void armarListaDirectorios(Document doc) throws MensajeErrorException {
		doc.getDocumentElement().normalize();
		Directorio directorioAgregar;
		String idDirectorio = null;
		String pathDirectorio = null;
		String pathDirMostrar = null;
		String permisoDirectorio = null;
		String usuarioDominioDirectorio = "";
		String usuarioDirectorio = null;
		String dominioDirectorio = "";
		String claveDirectorio = "";
		String claveArchivo = "";
		String nombreArchivo = null;
		String idArchivo = null;
		String usuarioDominioArchivo = "";
		String usuarioArchivo = null;
		String dominioArchivo = "";
		String permisosArchivo = null;
		String nodoA = null;

		// Obtenemos la etiqueta raiz (validar)
		Element raiz = doc.getDocumentElement();
		// Empiezo a recorrer los tag directorio.

		NodeList nodosHijos = raiz.getChildNodes();
		try {
			for (int i = 0; i < nodosHijos.getLength(); i++) {
				String nodo = nodosHijos.item(i).getNodeName().trim();
				if (nodo.equals("directorio")) {
					NamedNodeMap atributos = nodosHijos.item(i).getAttributes();
					// Asigno el valor de los posibles atributos del tag
					// proceso.
					idDirectorio = atributos.getNamedItem("id").getTextContent().trim();
					usuarioDirectorio = atributos.getNamedItem("usuario").getTextContent().trim();
					usuarioDominioArchivo = atributos.getNamedItem("usuario").getTextContent().trim();
					if (usuarioDirectorio.contains("\\")) {
						dominioDirectorio = Utils.obtenerCadIzquierda(usuarioDirectorio, "\\");
						usuarioDirectorio = Utils.obtenerCadDerecha(usuarioDirectorio, "\\");
					}
					if (usuarioDirectorio.contains("@")) {
						dominioDirectorio = Utils.obtenerCadDerecha(usuarioDirectorio, "@");
						dominioDirectorio = Utils.obtenerCadIzquierda(dominioDirectorio, ".");
						usuarioDirectorio = Utils.obtenerCadIzquierda(usuarioDirectorio, "@");
					}
					permisoDirectorio = atributos.getNamedItem("permiso").getTextContent().trim();
					permisoDirectorio = permisoDirectorio.replaceAll(" ", "");
					// claveDirectorio =
					// atributos.getNamedItem("clave").getTextContent().trim();
					pathDirectorio = atributos.getNamedItem("path").getTextContent().trim();
					pathDirMostrar = pathDirectorio;

					directorioAgregar = new Directorio(idDirectorio, StringEscapeUtils.escapeXml(pathDirectorio), pathDirMostrar, usuarioDominioArchivo, usuarioDirectorio,
							permisoDirectorio, claveDirectorio, dominioDirectorio);

					// Obtengo los hijos del nodo directorio para recorrer
					// los archivos
					NodeList nodosArchivo = nodosHijos.item(i).getChildNodes();

					for (int i1 = 0; i1 < nodosArchivo.getLength(); i1++) {
						nodoA = nodosArchivo.item(i1).getNodeName().trim();
						nombreArchivo = nodosArchivo.item(i1).getTextContent().trim();
						if (nodoA.equals("archivo")) {
							if (nodosArchivo.item(i1).hasAttributes()) {
								atributos = nodosArchivo.item(i1).getAttributes();
								idArchivo = atributos.getNamedItem("id").getTextContent().trim();
								usuarioArchivo = atributos.getNamedItem("usuario").getTextContent().trim();

								usuarioDominioArchivo = atributos.getNamedItem("usuario").getTextContent().trim();
								if (usuarioArchivo.contains("\\")) {
									dominioArchivo = Utils.obtenerCadIzquierda(usuarioArchivo, "\\");
									usuarioArchivo = Utils.obtenerCadDerecha(usuarioArchivo, "\\");
								}
								if (usuarioArchivo.contains("@")) {
									dominioArchivo = Utils.obtenerCadDerecha(usuarioArchivo, "@");
									dominioArchivo = Utils.obtenerCadIzquierda(dominioArchivo, ".");
									usuarioArchivo = Utils.obtenerCadIzquierda(usuarioArchivo, "@");
								}
								permisosArchivo = atributos.getNamedItem("permiso").getTextContent().trim();
								permisosArchivo = permisosArchivo.replaceAll(" ", "");
								directorioAgregar.agregarArchivo(idArchivo, usuarioArchivo, claveArchivo, permisosArchivo, usuarioDominioArchivo,
										StringEscapeUtils.escapeXml(nombreArchivo), dominioArchivo);

							}
						}

					}
					listaDirectorios.add(directorioAgregar);
				}

			}
		} catch (MensajeErrorException e) {
			throw e;
			// TODO: handle exception
		} catch (Exception e) {
			logger.trace(e.getMessage());
			throw new MensajeErrorException("Error en la sintaxis del mensaje");
		}
	}

	/**
	 * Valida el estado de un directorio, es decir, valida si un determinado
	 * usuario tiene determinados permisos en un determinado archivo/directorio.
	 * Los resultados pueden ser: ok (ese usuario tiene permisos en el
	 * archivo/directorio), error_permiso (ese usuario no tiene permisos en el
	 * archivo/directorio), error_archivo (no existe el archivo) y
	 * error_directorio (no existe el directorio).
	 * 
	 * @param usuario
	 * @param permisos
	 * @param path
	 * @param esArchivo
	 * @return
	 * @throws MensajeErrorException
	 */
	private String validarEstado(String usuario, String permisos, String path, String dominio, boolean esArchivo) throws MensajeErrorException {
		boolean existeArchivo = false;

		if (sistemaOperativo.equals("OS/400")) {
			existeArchivo = Utils.validarExistenciaArchivoAS400(path);
		} else {
			existeArchivo = Utils.validarExistenciaArchivo(path);
		}

		if (existeArchivo) {
			if (OsServiceFactory.getOsService().buscarUsuarioPermisos(usuario, permisos, path, dominio)) {

				return "ok";
			} else {
				return "error_permiso";
			}
		} else {
			if (esArchivo) {
				return "error_archivo";
			} else {
				return "error_directorio";
			}

		}

	}

	/**
	 * Valida los permisos de los directorios y archivos y devuelve el mensaje
	 * de respuesta en string.
	 * 
	 * @return
	 * @throws MensajeErrorException
	 */
	public String validarPermisos() throws MensajeErrorException {

		String estadoDirectorio;
		String estadoArchivo;
		String respuesta = "<validar>";
		for (Directorio directorio : listaDirectorios) {
			if (directorio.getUsuarioDirectorio().equals("") || directorio.getPermisoDirectorio().equals("") || directorio.getIdDirectorio().equals("")
					|| directorio.getPathDirectorio().equals("")) {
				estadoDirectorio = "error_permiso";
			} else {
				estadoDirectorio = validarEstado(directorio.getUsuarioDirectorio(), directorio.getPermisoDirectorio(), directorio.getPathDirectorio(),
						directorio.getDominioDirectorio(), false);
			}
			respuesta = respuesta + "<directorio ";
			respuesta = respuesta + "id=" + "\"" + directorio.getIdDirectorio() + "\"" + " " + "path=" + "\"" + directorio.getPathDirMostrar() + "\"" + " " + "usuario=" + "\""
					+ directorio.getUsuarioDominioDirectorio() + "\"" + " ";
			if (estadoDirectorio.equals("error_permiso")) {
				respuesta = respuesta + "permiso=" + "\"\" " + "estado=" + "\"" + estadoDirectorio + "\"" + " ";

			} else {
				respuesta = respuesta + "permiso=" + "\"" + directorio.getPermisoDirectorio() + "\"" + " " + "estado=" + "\"" + estadoDirectorio + "\"" + " ";

			}

			if (directorio.getArchivos() != null) {
				respuesta = respuesta + ">";
				for (Archivo archivo : directorio.getArchivos()) {
					if (archivo.getUsuario().equals("") || archivo.getPermiso().equals("") || archivo.getId().equals("") || archivo.getPath().equals("")
							|| archivo.getNombre().equals("")) {
						estadoArchivo = "error_permiso";
					} else {
						estadoArchivo = validarEstado(archivo.getUsuario(), archivo.getPermiso(), archivo.getPath(), archivo.getDominioArchivo(), true);
					}
					respuesta = respuesta + " <archivo ";
					respuesta = respuesta + "id=" + "\"" + archivo.getId() + "\"" + " " + "usuario=" + "\"" + archivo.getUsuarioDominioArchivo() + "\"" + " ";
					if (estadoArchivo.equals("error_permiso")) {
						respuesta = respuesta + "permiso=" + "\"\" " + "estado=" + "\"" + estadoArchivo + "\"" + ">" + archivo.getNombre();

					} else {
						respuesta = respuesta + "permiso=" + "\"" + archivo.getPermiso() + "\"" + " " + "estado=" + "\"" + estadoArchivo + "\"" + ">" + archivo.getNombre();

					}
					respuesta = respuesta + "</archivo>";

				}
				respuesta = respuesta + "</directorio>";
			}

		}

		return respuesta + "</validar>";
	}
}
package ast.servicio.probatch.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class TestExecProceso {

	ParametrosProceso parametroP;

	public static void main(String[] args) {

		String trama = "<proceso>PROCESO</proceso>";

		execute(trama);
	}

	public static void execute(String trama) {
		ParametrosProceso parametrosProceso;
		try {
			parametrosProceso = XmlToObject(Utils.parsearMensaje(trama));
			validaciones(parametrosProceso);
			ejecutarProceso(parametrosProceso);
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MensajeErrorException e) {
			System.out.println("Exception en Validaciones " + e.getRespuestaError().toString());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TestExecProceso(ParametrosProceso parametroP) {
		this.parametroP = parametroP;
	}

	private static void ejecutarProceso(ParametrosProceso parametroP) throws Exception {

		String[] cmd = OsServiceFactory.getOsService().getExecuteCommand(parametroP);
		String currentDir = new File(".").getAbsolutePath();
		currentDir.replaceAll("\\.", "");
		String strDirCategoria = currentDir + parametroP.getCategoria();

		String strDirChdir = parametroP.getChdir();
		File dirChdir;
		File dirCategoria;

		if (!Utils.validarExistenciaArchivo(strDirChdir)) {
			Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre()
					+ "\" ts=\"" + parametroP.getTs() + "\">La carpeta chdir no existe: " + strDirChdir + "</error>");
			// ServicioAgente.colaDeMensajes.encolarMensaje(mensaje);
			System.out.println("Mensaje: " + mensaje.getTramaString());
			return;
		}

		if (Utils.validarExistenciaArchivo(strDirCategoria)) {
			Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre()
					+ "\" ts=\"" + parametroP.getTs() + "\">La carpeta categoria no existe: " + strDirCategoria + "</error>");
			// ServicioAgente.colaDeMensajes.encolarMensaje(mensaje);
			System.out.println("Mensaje: " + mensaje.getTramaString());
			return;
		}

		dirChdir = new File(strDirChdir);
		dirCategoria = new File(strDirCategoria);

		if (dirCategoria.canWrite()) {
			Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"" + parametroP.getId() + "\" nombre=\"" + parametroP.getNombre()
					+ "\" ts=\"" + parametroP.getTs() + "\">La carpeta categoria no tiene permiso de escritura: " + strDirCategoria + "</error>");
			// ServicioAgente.colaDeMensajes.encolarMensaje(mensaje);
			System.out.println("Mensaje: " + mensaje.getTramaString());
			return;
		}

		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd, null, dirChdir);
		} catch (IOException e) {
			Mensaje mensaje = MessageFactory.crearMensajeError("<error id=\"\" nombre=\"\" ts=\"\">No se puede lanzar el comando: " + cmd
					+ "</error>");
			// ServicioAgente.colaDeMensajes.encolarMensaje(mensaje);
			System.out.println("Mensaje: " + mensaje.getTramaString());
			e.printStackTrace();
			return;
		}

		try {

			if (!"DESCONECTADA".equals(parametroP.getClase())) {
				BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null) {

					System.out.println(line);
					Mensaje mensaje = MessageFactory.crearMensajeRespuesta("<mensaje id=\"" + parametroP.getId() + "\" nombre=\""
							+ parametroP.getNombre() + "\" ts=\"" + parametroP.getTs() + "\">" + line + "</mensaje>");
					System.out.println("Mensaje: " + mensaje.getTramaString());
					// ServicioAgente.colaDeMensajes.encolarMensaje(mensaje);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ParametrosProceso XmlToObject(Document input) {

		int i;
		ParametrosProceso pp = new ParametrosProceso();

		try {
			Element raiz = input.getDocumentElement();
			// Empiezo a recorrer los tag directorio.

			NodeList nodosHijos = raiz.getChildNodes();

			if (Utils.esNumerico(raiz.getAttribute("id"))) {
				pp.setId(raiz.getAttribute("id"));
			} else {
				// throw exception e;
			}

			String key = ServicioAgente.cfg.getKey();

			Atributo aux = new Atributo("id", "");

			pp.setTs(new Date().getTime() / 1000);

			Collection<Atributo> argumentos = new ArrayList<Atributo>();

			Collection<Atributo> entorno = new ArrayList<Atributo>();

			Collection<Atributo> interfaces = new ArrayList<Atributo>();

			Collection<Atributo> patrones = new ArrayList<Atributo>();

			pp.setNombre(raiz.getAttribute("nombre"));

			pp.setCategoria(raiz.getAttribute("categoria"));

			pp.setClase(raiz.getAttribute("clase"));

			for (i = 0; i < nodosHijos.getLength(); i++) {
				String nodo = nodosHijos.item(i).getNodeName().trim();
				if (nodo.equals("chdir")) {
					pp.setChdir(nodosHijos.item(i).getTextContent());

				} else if (nodo.equals("comando")) {

					pp.setComando(nodosHijos.item(i).getTextContent());

				} else if (nodo.equals("arg")) {
					aux = new Atributo("", nodosHijos.item(i).getTextContent());
					if (nodosHijos.item(i).getAttributes().getNamedItem("tipo") != null) {
						aux.setTipo(nodosHijos.item(i).getAttributes().getNamedItem("tipo").getTextContent());
						if (aux.getTipo().equals("oculto"))
							aux.setValor(new String(Utils.xorstr(key, pp.getId(), nodosHijos.item(i).getTextContent())));
					}
					argumentos.add(aux);

				} else if (nodo.equals("usuario")) {

					aux = new Atributo(nodosHijos.item(i).getTextContent(), new String(Utils.xorstr(key, pp.getId(), nodosHijos.item(i)
							.getAttributes().getNamedItem("clave").getTextContent())));

					pp.setUsuario(aux);

				} else if (nodo.equals("entorno")) {
					NodeList subHijos = nodosHijos.item(i).getChildNodes();
					for (int a = 0; a < subHijos.getLength(); a++) {
						if (subHijos.item(a).getNodeName().trim().equals("var")) {
							aux = new Atributo(subHijos.item(a).getAttributes().getNamedItem("nombre").getTextContent(), subHijos.item(a)
									.getTextContent());
							if (subHijos.item(a).getAttributes().getNamedItem("tipo") != null) {
								aux.setTipo(subHijos.item(a).getAttributes().getNamedItem("tipo").getTextContent());
								if (aux.getTipo().equals("oculto"))
									aux.setValor(new String(Utils.xorstr(key, pp.getId(), subHijos.item(a).getTextContent())));
							}
							entorno.add(aux);
						}
					}
				} else if (nodo.equals("patron")) {
					NodeList subHijos = nodosHijos.item(i).getChildNodes();
					for (int a = 0; a < subHijos.getLength(); a++) {
						aux = new Atributo(subHijos.item(a).getNodeName().trim(), subHijos.item(a).getTextContent());

						if (subHijos.item(a).getAttributes().getNamedItem("tipo") != null) {
							aux.setTipo(subHijos.item(a).getAttributes().getNamedItem("tipo").getTextContent());
							if (aux.getTipo().equals("oculto")) {
								aux.setValor(new String(Utils.xorstr(key, pp.getId(), nodosHijos.item(i).getTextContent())));
							}
						} else {
							aux.setTipo("glob");
						}
						patrones.add(aux);
					}

				} else if (nodo.equals("interfaces")) {
					NodeList subHijos = nodosHijos.item(i).getChildNodes();
					for (int a = 0; a < subHijos.getLength(); a++) {
						aux = new Atributo(subHijos.item(a).getNodeName().trim(), subHijos.item(a).getTextContent());

						if (subHijos.item(a).getAttributes().getNamedItem("controlar_todos") != null) {
							aux.setControlar_todos(subHijos.item(a).getAttributes().getNamedItem("controlar_todos").getTextContent());
						}
						interfaces.add(aux);
					}
				} else {
					System.out.println("No se reconoce el elemento " + nodo);
				}

			}
			pp.setEntorno(entorno);
			pp.setInterfaces(interfaces);
			pp.setPatrones(patrones);
			pp.setArgumentos(argumentos);

			// Asigno el valor de los posibles atributos del tag proceso.
			// test=atributos.getNamedItem("chdir").getTextContent().trim();
			// aux.setValor(test );
			// usuarioDirectorio =
			// atributos.getNamedItem("usuario").getTextContent().trim();
			// permisoDirectorio =
			// atributos.getNamedItem("permiso").getTextContent().trim();
			// claveDirectorio =
			// atributos.getNamedItem("clave").getTextContent().trim();
			// pathDirectorio =
			// atributos.getNamedItem("path").getTextContent().trim();

			// p.agregarDirectorio(idDirectorio, pathDirectorio,
			// usuarioDirectorio, claveDirectorio, permisoDirectorio);
		} catch (Exception e) {
			System.out.println("Excp: " + e.getMessage());

		}
		return pp;
	}

	private static void validaciones(ParametrosProceso parametrosProceso) throws MensajeErrorException {

		boolean existeUsuario = false;
		ArrayList<String> lista = ServicioAgente.cfg.getUsuarios();
		Collection<Atributo> collectionInterfaces = parametrosProceso.getInterfaces();
		String interfaces = null;
		String controlarTodos = null;
		String noEcontrados;
		for (Iterator<Atributo> iterator = collectionInterfaces.iterator(); iterator.hasNext();) {
			Atributo atributo = iterator.next();
			if (ParametrosProceso.ENTRADA.equals(atributo.getNombre())) {

				interfaces = atributo.getValor();
				controlarTodos = atributo.getControlar_todos();

			}
		}

		Atributo usuario = parametrosProceso.getUsuario();
		for (Iterator<String> iterator = lista.iterator(); iterator.hasNext();) {
			String usuarioConfig = iterator.next();
			if (usuario.getNombre().equalsIgnoreCase(usuarioConfig))
				existeUsuario = true;
		}

		if (!existeUsuario) {
			throw new MensajeErrorException("Usuario invalido: " + parametrosProceso.getUsuario().getNombre());
		}

		if (interfaces != null) {

			noEcontrados = Utils.archivosNoEncontrados(interfaces);

			if (noEcontrados != null || controlarTodos == "1") {

				throw new MensajeErrorException("Interfaces: " + interfaces + ">interfaces de entrada no encontradas");

			}
		}
	}

	public boolean matar(int pid) {
		try {
			Runtime.getRuntime().exec(OsServiceFactory.getOsService().getKillCommand(pid));
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}

}
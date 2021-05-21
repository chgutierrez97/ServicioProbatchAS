package ast.servicio.probatch.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.exception.MensajeErrorException;

public class prueba {

	/**
	 * @param args
	 * @throws MensajeErrorException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MensajeErrorException, IOException {
		String xmlFileName = "C:/Users/rodrigo.guillet/Desktop/test.txt";
//		String xmlFileName = "/home/ubuntu/Desktop/test.txt";
////		ServicioAtencion.cfg.getDump_file();
//		EstadoMensaje em = new EstadoMensaje("1234","LIHUERODRIGO",1328559477,0,"falla","1");
//
//		System.out.println("ULTIMO METODO");
//		System.out.println(UtilsEstadoGuardado.estadoMensajeToString(em));
//		
//		String agregar = "<transicion id= \"1234\" nombre=\"LIHUE\" ts=\"1328559477\"><fin estado=\"falla\" valor=\"1\" /></transicion>";
//		String agregar2 = "<transicion id= \"1234\" nombre=\"LIHUERODRIGO\" ts=\"1328559477\"><fin estado=\"GROSO\" valor=\"1\" /></transicion>";
//		
//		
//		System.out.println("ID: " + UtilsEstadoGuardado.obtenerId(agregar));
//		
//		
//			
//		String original = UtilsEstadoGuardado.leerTxt(xmlFileName);
//		String resultado = UtilsEstadoGuardado.agregarCadena(agregar, original);
//		System.out.println(resultado);
//		resultado = UtilsEstadoGuardado.agregarCadena(agregar2, resultado);
//		System.out.println(resultado);
//		
//		UtilsEstadoGuardado.escribir(xmlFileName, resultado);
//		
//		System.out.println("Estado de id 12345");
//		UtilsEstadoGuardado.mostrarEstado("12345", resultado);
		
		
		estadoGuardado eg = new estadoGuardado(xmlFileName);
		
		
		EstadoProceso em = new EstadoProceso("1234","LIHUERODRIGO",1328559477,0,1);
		EstadoProceso em2 = new EstadoProceso("1234","LIHUE",1328559477,0,2);
		EstadoProceso em3 = new EstadoProceso("12345","LIHUERODRIGOGUILLET",1328559477,0,3);
		
		
		List<EstadoProceso> listaEstadosMensajes = new ArrayList<EstadoProceso>();
		listaEstadosMensajes.add(em);
		listaEstadosMensajes.add(em2);
		listaEstadosMensajes.add(em3);
		
		
		
//		eg.agregarEstado(em);
//		eg.agregarEstado(em2);
//		eg.agregarEstado(em3);
		
		eg.agregarListaEstado(listaEstadosMensajes);
		
		
		eg.mostrarEstado("1234");
		System.out.println(eg.textoConTag());
		System.out.println(eg.textoSinTag());
		eg.eliminarTransicion("1");
		System.out.println("EL ESTADO 12345");
		eg.mostrarEstado("12345");
		eg.escribir();
	}
}
package ast.servicio.probatch.test;

import java.io.UnsupportedEncodingException;

import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class pruebaValidacion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			ServicioAgente probatch = new ServicioAgente("mantis.conf");

			String key = "MPBKeyED";
			String encriptado = "%4A%D9%79%BD%68%DA%D9%EF%ED%3E%90";
			
			
			
			byte[] original = Utils.xorstr(key, "3398733", encriptado);
			
			System.out.println("encriptado:"+Utils.byte2hex(Utils.xorstr(key, "3398733", Utils.byte2hex(original))));
			
			
			String keyResultado = new String(original, "UTF-8");
			System.out.println("Clave a comparar: UTF8:" + keyResultado);

			keyResultado = new String(original);
			System.out.println("Clave a comparar: NADA:" + keyResultado);

			keyResultado = new String(original, "CP850");

			System.out.println("Clave a comparar: CP850:" + keyResultado);

			keyResultado = new String(original, "ISO-8859-1");
			System.out.println("Clave a comparar: iso-8859-1:" + keyResultado);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

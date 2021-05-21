package ast.servicio.probatch.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Map;

import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.Utils;

public class Testing {

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException {
//		// ArrayList<String> lista = new ArrayList<String>();
//		// lista.add("Hola");
//		// lista.add("/usr/bin/ls");
//		// lista.add("c:/Windos/System32/cmd.exe");
//		// lista.add("Bye");
//		//		
//		// System.out.println(new
//		// String("c:/Windos/System32/cmd.exe").contains("cmd"));
//		//		
//		// System.out.println("Contains: " + lista.contains("cmd.exe"));
//
//		// Pattern pattern = Pattern.compile("(X|W|R)");
//
//		// Matcher matcher = pattern.matcher("XWRA");
//
//		// System.out.println("Find: " + matcher.find());
//		
//		System.out.println(""+(char)13+(char)10);
//
		 String enc = Utils.byte2hex(xorstr("MPBKeyED", "3426042",
		 Utils.byte2hex("Mayo2013".getBytes())));
		 System.out.println("Enc " + enc);
		
//		 System.out.println(Boolean.parseBoolean("true"));
				
		 byte[] enc2 = xorstr("MPBKeyED", "3412679", enc);
		 System.out.println("Enc " + new String(enc2));
//		
//		 String prueba = Utils.seccionaFecha("ddMMyy");
//		 System.out.println(prueba);
//
//		Process process = OsServiceFactory.getOsService().executeCommand("cmd.exe /c dir");
//
//		BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//		String line = null;
//		while ((line = in.readLine()) != null) {
//			System.out.println(line);
//		}
//
//		System.out.println(process.exitValue());
//		
//		String dir = "dir/dir/d<i>r";
//		System.out.println(StringEscapeUtils.escapeXml(dir));
//
//		StringTokenizer stk = new StringTokenizer("Javier        Alejandro          ", " ");
//		while (stk.hasMoreTokens()) {
//			System.out.println(stk.nextToken().trim());
//		}
//
//		String mensaje = "Path=C:\\Perl\\site\bin;C:\\Perl\\bin;%SystemRoot%\\system32;%SystemRoot%;%SystemRoot%\\System32\\Wbem;%SYSTEMROOT%\\System32\\WindowsPowerShell\\v1.0\\;C:\\OpenSSL\\bin;C:\\Program Files\\TortoiseSVN\\bin;C:\\apache-maven-2.2.1\bin;%JAVA_HOME%\\bin";
//		System.out.println(mensaje);
//		System.out.println(mensaje.substring(mensaje.indexOf('%') + 1).indexOf('%') + 1);
//		String mensajeC = mensaje;
//
//		while (mensajeC.contains("%")) {
//			String sisVar = "";
//			sisVar = mensajeC.substring(mensajeC.indexOf('%') + 1, mensajeC.substring(mensajeC.indexOf('%') + 1).indexOf('%') + mensajeC.indexOf('%') + 1);
//			if (!sisVar.equals("")) {
//				String sisVarValue = "";
//				sisVarValue = System.getenv(sisVar);
//				if (!sisVarValue.equals("")) {
//					mensaje = mensaje.replace("%" + sisVar + "%", sisVarValue);
//				}
//			}
//			mensajeC = mensajeC.substring(mensajeC.substring(mensajeC.indexOf('%') + 1).indexOf('%') + mensajeC.indexOf('%') + 2);
//		}
//		System.out.println(mensaje);
//
//		System.out.println(System.getenv("SystemDrive"));
	}

	/**
	 * Xor para desencriptar o encriptar clave
	 * 
	 * @param key
	 *            - Clave en arch de config
	 * @param id
	 *            - id a concatenar
	 * @param arg
	 *            - Texto encriptado o a encriptar
	 * @return
	 */
	public static byte[] xorstr(String key, String id, String arg) {

		byte[] resultado = null;
		byte[] decryptArg = Utils.hex2Byte(arg);
		byte messageDigest[];
		byte[] s;

		String key_id = key + id;

		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(key_id.getBytes());

			messageDigest = algorithm.digest();

			int la = decryptArg.length;
			int lk = messageDigest.length;

			int tamS = (messageDigest.length * (la / lk)) + (la % lk);
			s = new byte[tamS];

			int base = 0;
			for (int i = 0; i < (la / lk); i++) {
				base = messageDigest.length * i;
				for (int j = 0; j < messageDigest.length; j++)
					s[base + j] = messageDigest[j];

			}

			base = tamS - (la % lk);
			for (int j = 0; j < (la % lk); j++)
				s[base + j] = messageDigest[j];

			int minimo = Math.min(decryptArg.length, s.length);

			resultado = new byte[minimo];

			for (int i = 0; i < minimo; i++) {
				resultado[i] = (byte) (decryptArg[i] ^ s[i]);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(5);
		}

		return resultado;
	}

	public static void main4(String[] args) throws IOException {

		String archCfg = null;

		if (args.length > 0)

			archCfg = args[0];

		else

			archCfg = "mantis.conf";

		System.out.println("Archivo de Configuracion: " + archCfg);

		ServicioAgente probatch = new ServicioAgente(archCfg);
		probatch.ejecutarServicio();

	}

	public static void main3(String[] args) {

		// Formato del mensaje:
		// <log><channel>x</channel><type>xxxxx</type><message>xxxxxxxxxx</message></log>

		// Obtengo los campos del mensaje

		String cadena = "<log><channel>x</channel><type>xxxxx</type><message>xxxxxxxxxx</message></log>";

		// Al recibir y enviar mensajes XML, el parser reconoce el mensaje e
		// interpreta el mensaje como parte del XML.

		String[] aux = cadena.split("<message>");

		String[] aux2 = aux[1].split("</message>");

		System.out.println("Pos channel: " + cadena.indexOf("<channel>") + "Last pos: " + cadena.indexOf("</channel>"));

		System.out.println("Channel: " + cadena.substring(cadena.indexOf("<channel>") + "<channel>".length(), cadena.indexOf("</channel>")));

		System.out.println("type: " + cadena.substring(cadena.indexOf("<type>") + "<type>".length(), cadena.indexOf("</type>")));

		System.out.println("Channel: " + cadena.substring(cadena.indexOf("<message>") + "<message>".length(), cadena.indexOf("</message>")));

		for (String string : aux) {

			System.out.println("Aux " + string);

		}

		for (String string : aux2) {

			System.out.println("Aux2 " + string);

		}

	}

	public static void main2(String[] args) throws Exception {

		ProcessBuilder builder = new ProcessBuilder("cmd.exe");

		Map<String, String> environment = builder.environment();

		environment.put("path", ";"); // Clearing the path variable;

		// environment.put("path", "D:\\Java\\Java6.0\\bin;");

		Process javap = builder.start();

		writeProcessOutput(javap);

	}

	static void writeProcessOutput(Process process) throws Exception {

		InputStreamReader tempReader = new InputStreamReader(

		new BufferedInputStream(process.getInputStream()));

		BufferedReader reader = new BufferedReader(tempReader);

		while (true) {

			String line = reader.readLine();

			if (line == null)

				break;

			System.out.println(line);

		}

	}

}

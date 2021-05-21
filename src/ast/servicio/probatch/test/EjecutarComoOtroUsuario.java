package ast.servicio.probatch.test;

import java.io.IOException;

import ast.servicio.probatch.os.service.OsServiceFactory;

public class EjecutarComoOtroUsuario {

	/**
	 * @param args
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	public static void main(String[] args) throws IOException, SecurityException, IllegalArgumentException, NoSuchFieldException,
			IllegalAccessException {

		Process proceso = OsServiceFactory.getOsService().executeCommand("D:\\Proyectos\\ProBatch\\CPAU.EXE -u matias.brino -p SaintSeiya14 -ex notepad.exe");

		int pid = OsServiceFactory.getOsService().getPid(proceso);

		System.out.println("pid: " + pid);

		
	}

}

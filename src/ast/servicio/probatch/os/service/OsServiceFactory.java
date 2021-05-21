package ast.servicio.probatch.os.service;

import ast.servicio.probatch.os.service.impl.AS400Service;
import ast.servicio.probatch.os.service.impl.LinuxService;
import ast.servicio.probatch.os.service.impl.UnixService;
import ast.servicio.probatch.os.service.impl.WindowsService;

public class OsServiceFactory {

	private static final String UNIX = "aix";
	private static final String lINUX = "nux";
	private static final String WINDOWS = "win";
	private static final String AS400 = "os/400";
	
	private static final String OS_NAME = "os.name";
	
	private static OsService oSService;

	public static OsService getOsService() {
		if (oSService != null){
			return oSService;
		}
		
		String sistemaOperativo = System.getProperty(OS_NAME);
		
		if (sistemaOperativo.toLowerCase().contains(WINDOWS)) {
			oSService = new WindowsService();
		} else if (sistemaOperativo.toLowerCase().contains(lINUX)) {
			oSService = new LinuxService();
		} else if (sistemaOperativo.toLowerCase().contains(UNIX)) {
			oSService = new UnixService();
		}else if (sistemaOperativo.toLowerCase().contains(AS400)) {
			oSService = new AS400Service();
		}else{
			//TODO: Main Frame
		}
		return oSService;
	}
	
	public static boolean isWindowSO(){
		return System.getProperty(OS_NAME).toLowerCase().equals(WINDOWS);
	}
	
	public static boolean isLinuxSO(){
		return System.getProperty(OS_NAME).toLowerCase().equals(lINUX);
	}
	
	public static boolean isUnixSO(){
		return System.getProperty(OS_NAME).toLowerCase().equals(UNIX);
	}
	
	public static boolean isAS400(){
		return System.getProperty(OS_NAME).toLowerCase().equals(AS400);
	}

}

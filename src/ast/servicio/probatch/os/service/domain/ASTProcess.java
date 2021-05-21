package ast.servicio.probatch.os.service.domain;

import java.io.InputStream;

import com.ibm.as400.access.CommandCall;

import ast.servicio.probatch.os.service.OsServiceFactory;


public  class ASTProcess implements IProcess{

	Process procesoWLU = null;
	
	public ASTProcess(Process procesoWLU) {
		this.procesoWLU = procesoWLU;
	}

	public int getPid() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return OsServiceFactory.getOsService().getPid(this.procesoWLU);
	}

	public InputStream getInputStream() {
		return procesoWLU.getInputStream();
	}

	public InputStream getErrorStream() {
		return procesoWLU.getErrorStream();
	}

	public int waitFor() throws InterruptedException {
		return procesoWLU.waitFor();
	}

	public int exitValue() {
		return procesoWLU.exitValue();
	}
	
	public byte[] getKey(){
		
		return null;
	}

	public CommandCall getCommandCall() {
	
		return null;
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}

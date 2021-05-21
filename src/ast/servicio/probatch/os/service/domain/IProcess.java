package ast.servicio.probatch.os.service.domain;

import java.io.InputStream;

import com.ibm.as400.access.CommandCall;

public interface IProcess extends Runnable {
	
	public int getPid() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException; 
	
	public InputStream getInputStream();
	
	public CommandCall getCommandCall();
	
	public InputStream getErrorStream();
	
	public int waitFor() throws InterruptedException;
	
	public int exitValue();
	
	public byte[] getKey();
	
	
}

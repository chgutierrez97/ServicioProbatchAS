package ast.servicio.probatch.os.service.domain;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.message.MensajeError;
import ast.servicio.probatch.service.ServicioAgente;

import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.Job;

public class ASTRunCommand implements IProcess {

	public static Logger logger = LoggerFactory.getLogger(ServicioAgente.class);

	CommandCall proceso = null;
	// RunCommand comando = null;
	boolean ejecucionResultado = false;

	public ASTRunCommand(CommandCall proceso) {
		this.proceso = proceso;
		// this.comando = proceso;
	}

	public CommandCall getCommandCall() {

		return this.proceso;
	}

	public int getPid() {
		try {
			Job trabajo = proceso.getServerJob();
			return Integer.parseInt(trabajo.getNumber());

		} catch (Exception e) {

			logger.error(new MensajeError(e.getMessage()).getTramaString());
			return 0;
		}
	}

	public InputStream getInputStream() {

		return null;
	}

	public InputStream getErrorStream() {
		// TODO Auto-generated method stub
		return null;
	}

	public int waitFor() throws InterruptedException {

		if (ejecucionResultado) {

			return 0;
		} else {

			return 1;
		}
	}

	public int exitValue() {

		if (ejecucionResultado) {

			return 0;
		} else {

			return 1;
		}
	}

	public byte[] getKey() {

		return null;
	}

	public boolean sucessfulExecution() {

		return ejecucionResultado;
	}

	public void run() {

		try {

			// logger.debug("Antes de ejecutar el comando");

			ejecucionResultado = proceso.run();

			// logger.debug("Se ejecuto el comando");

		} catch (Exception e) {
			String errorMsgHeader = this.getClass() + "::run:";
			logger.error(errorMsgHeader + "Error al ejecutar el comando: " + proceso.getCommand());
			logger.debug(errorMsgHeader + e.getMessage());
		}
	}

}

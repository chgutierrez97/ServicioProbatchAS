package ast.servicio.probatch.threads;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.os.service.domain.IProcess;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.LoggerUtils;
import ast.servicio.probatch.util.Utils;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.JobLog;
import com.ibm.as400.access.QueuedMessage;

public class ListenerProcesoAS400 extends Thread {

	public static Logger loggerProceso;
	public static Logger logger = LoggerFactory.getLogger(ServicioAgente.class);

	private IProcess process;
	private ParametrosProceso parametroP;
	private OutputStream osSalida;
	private String errorFatal;

	public ListenerProcesoAS400(IProcess process, OutputStream osSalidaSocket, ParametrosProceso parametroP) {
		super("Listener Proceso (" + parametroP.getId() + ")" + " Estandar");
		this.process = process;
		this.parametroP = parametroP;
		this.osSalida = osSalidaSocket;
		loggerProceso = LoggerUtils.getLoggerProceso(parametroP);
	}

	public CommandCall getCommandCall() {
		return process.getCommandCall();
	}

	public String getErrorFatal() {
		return errorFatal;
	}

	public void setErrorFatal(String errorFatal) {
		this.errorFatal = errorFatal;
	}

	public void run() {
		String debugMsgHeader = this.getClass().getName() + "::run:";

		String typeMessage = "mensaje";
		String mensajeAux = null;
		CommandCall cmd = process.getCommandCall();
		AS400 conexion = cmd.getSystem();
		JobLog logTrabajoMon = null;
		byte[] last = null;

		try {

			conexion.connectService(AS400.COMMAND);
			AS400 conexionAux = new AS400(conexion);
			conexionAux.connectService(AS400.COMMAND);

			Job trabajo = cmd.getServerJob();
			byte[] internalJobIdentifier = (byte[]) trabajo.getValue(Job.INTERNAL_JOB_IDENTIFIER);
			Job trabajoMon = new Job(conexionAux, internalJobIdentifier);

			logTrabajoMon = trabajoMon.getJobLog();
			logTrabajoMon.setStartingMessageKey(last);

			Enumeration<?> listaMensaje = logTrabajoMon.getMessages();

			if (last != null)
				listaMensaje.nextElement();

			while (listaMensaje.hasMoreElements()) {
				QueuedMessage message = (QueuedMessage) listaMensaje.nextElement();
				mensajeAux = message.getFromProgram() + ":" + message.getAlertOption() + ":" + message.getSeverity() + ":" + message.getText();
				if (validaPatron(parametroP.getPatrones(), mensajeAux) == null) {
					Mensaje mensaje = MessageFactory.crearMensajeRespuesta("<" + typeMessage + " id=\"" + parametroP.getId() + "\" nombre=\""
							+ parametroP.getNombre() + "\" ts=\"" + parametroP.getTs() + "\">" + StringEscapeUtils.escapeXml(mensajeAux) + "</" + typeMessage
							+ ">");
					loggerProceso.info(StringEscapeUtils.escapeXml(mensajeAux));
					logger.debug("CLI <-- " + mensaje.getTramaString());

					osSalida.write(mensaje.getTramaString().getBytes());
					last = message.getKey();
				} else {

				}
				Thread.sleep(500);
			}

		} catch (MensajeErrorException e) {

			setErrorFatal(e.getRespuestaError().toString());
			Utils.matar(parametroP.getPid());
			logger.info(e.getRespuestaError().toString());
			logger.trace(e.getMessage());

		} catch (IOException e) {

			System.err.println("Error al enviar el mensaje");
			loggerProceso.error("Error al enviar el mensaje");
			logger.error("Error al enviar el mensaje");
			logger.trace(e.getMessage());
			ServicioAgente.connectionStatus = false;
			EjecutarProceso.procesoTerminado = true;
		} catch (Exception e) {

			loggerProceso.error("Error al ejecutar el proceso");
			logger.error("Error al ejecutar el proceso: " + e.getMessage());
			logger.trace(e.getMessage());
			ServicioAgente.connectionStatus = false;
		}

	}

	public String validaPatron(Collection<Atributo> patrones, String salidaProceso) throws MensajeErrorException {

		if (!patrones.isEmpty()) {

			for (Iterator<Atributo> iterator = patrones.iterator(); iterator.hasNext();) {

				Atributo atributo = (Atributo) iterator.next();

				if (ParametrosProceso.FATAL.equals(atributo.getNombre()) && Utils.validaEsxpresionesRegulrares(salidaProceso, atributo.getValor())) {

					throw new MensajeErrorException("fatal", parametroP.getId(), parametroP.getNombre(), salidaProceso);

				}
			}

			for (Iterator<Atributo> iterator = patrones.iterator(); iterator.hasNext();) {

				Atributo atributo = (Atributo) iterator.next();

				if (ParametrosProceso.IGNORAR.equals(atributo.getNombre()) && atributo.getTipo().equals("glob")
						&& Utils.validaEsxpresionesRegulrares(salidaProceso, atributo.getValor())) {
					return salidaProceso;
				} else if (ParametrosProceso.IGNORAR.equals(atributo.getNombre()) && atributo.getTipo().equals("re")) {
					if ((Utils.validaEsxpresionesRegulrares(salidaProceso, atributo.getValor()) || Utils.validaEsxpresionesRegulrares(salidaProceso,
							ServicioAgente.cfg.getIgnore_re()))) {
						return salidaProceso;
					}
				}

			}

		}
		return null;
	}
}

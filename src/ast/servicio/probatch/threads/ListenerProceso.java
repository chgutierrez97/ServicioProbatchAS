package ast.servicio.probatch.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;
import ast.servicio.probatch.message.MensajeProceso;
import ast.servicio.probatch.os.service.OsServiceFactory;
import ast.servicio.probatch.os.service.domain.IProcess;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.LoggerUtils;
import ast.servicio.probatch.util.Utils;

public class ListenerProceso extends Thread {

	public static Logger loggerProceso;
	public static Logger logger = LoggerFactory.getLogger(ServicioAgente.class);

	public final static String TYPE_ERROR = "TYPE_ERROR";
	public final static String TYPE_INPUT = "TYPE_INPUT";
	public final static String WARNING_UNIX = "stty: tcgetattr: Not a typewriter";

	private OutputStream osSalida;
	private ParametrosProceso parametroP;
	private IProcess process;
	private String type;
	private String errorFatal;

	public ListenerProceso(String type, IProcess process, OutputStream osSalidaSocket, ParametrosProceso parametroP) {
		super("Listener Proceso (" + parametroP.getId() + ")" + (TYPE_INPUT.equalsIgnoreCase(type) ? " Estandar" : " Error"));
		this.type = type;
		this.process = process;
		this.osSalida = osSalidaSocket;
		this.parametroP = parametroP;
		loggerProceso = LoggerUtils.getLoggerProceso(parametroP);
	}

	public String getErrorFatal() {
		return errorFatal;
	}

	public void setErrorFatal(String errorFatal) {
		this.errorFatal = errorFatal;
	}

	@Override
	public void run() {

		String typeMessage = "mensaje";
		String line = null;
		try {
			String headerType;
			BufferedReader in;
			// Verifico si es el thread de errorstream o el thread de
			// inputstream
			if (type.equals(TYPE_INPUT)) {
				in = new BufferedReader(new InputStreamReader(process.getInputStream()));
				headerType = "mensaje";
			} else {
				in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				headerType = "error";
			}

			boolean firstTime = true;

			while ((line = in.readLine()) != null && !MensajeProceso.terminarThreadsLocal) {
				if (validaPatron(parametroP.getPatrones(), line) == null) {
					if ("error".equals(headerType)) {
						loggerProceso.error(StringEscapeUtils.escapeXml(line));
						//Este if se agrega debido a que el msj de SQR envia un error que para nosotros no seria
						if(!WARNING_UNIX.equals(line)){
							if (firstTime) {
								typeMessage = "fatal";
								firstTime = false;
							} else {
								typeMessage = "mensaje";
							}
						}
					} else {
						loggerProceso.info(StringEscapeUtils.escapeXml(line));
					}

					line = OsServiceFactory.getOsService().resuelveVariablesDeSistema(line);
					Mensaje mensaje = MessageFactory.crearMensajeRespuesta("<" + typeMessage + " id=\"" + parametroP.getId() + "\" nombre=\""
							+ parametroP.getNombre() + "\" ts=\"" + parametroP.getTs() + "\">" + StringEscapeUtils.escapeXml(line) + "</" + typeMessage + ">");

					logger.debug("CLI <-- " + mensaje.getTramaString());
					osSalida.write(mensaje.getTramaString().getBytes());
				}

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
package ast.servicio.probatch.message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.exception.MensajeErrorException;
import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.util.DateUtils;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.JobList;
import com.ibm.as400.access.ObjectDoesNotExistException;

public class ConsultaProcesoAS400 extends Mensaje {

	public static Logger logger = LoggerFactory.getLogger(MensajeProceso.class);

	public ConsultaProcesoAS400(String mensajeEntrada) {
		super(mensajeEntrada);

	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {
		logger.debug("procesarMensaje:consultando procesos de as400...");

		String nombreSubProc = "";
		String nombreUsuarioEje = "";

		StringBuffer mensajeString = new StringBuffer("<procesos-as400>");

		try {

			nombreSubProc = this.getTramaXml().getDocumentElement().getAttribute("nombre");
			nombreUsuarioEje = this.getTramaXml().getDocumentElement().getAttribute("usuario");
			AS400 sys = new AS400(ServicioAgente.cfg.getaS400Server());
			// Create the job list object.
			JobList jobList = new JobList(sys);

			if (!nombreSubProc.equals("")) {
				jobList.addJobSelectionCriteria(JobList.SELECTION_JOB_NAME, nombreSubProc);
			} else if (!nombreUsuarioEje.equals("")) {
				jobList.addJobSelectionCriteria(JobList.SELECTION_USER_NAME, nombreUsuarioEje);
			} else {
				mensajeString.append("Se debe introducir el nombre del proceso o el usuario que de ejecucion");
				mensajeString.append("</procesos-as400>");
				return MessageFactory.crearMensajeRespuesta(mensajeString.toString());
			}
			Enumeration<?> list = jobList.getJobs();

			while (list.hasMoreElements()) {

				mensajeString.append("<proceso");
				Job j = (Job) list.nextElement();
				try {
					
					String status = j.getStatus();

					if (status.equals(Job.JOB_STATUS_ACTIVE)) {
						printJobData(j);
						status = status + "/" + j.getValue(Job.ACTIVE_JOB_STATUS);
					} else if (status.equals(Job.JOB_STATUS_OUTQ)) {

						String estadoFinal = new String();
						estadoFinal = j.getCompletionStatus();

						if (estadoFinal.equals(Job.COMPLETION_STATUS_NOT_COMPLETED)) {
							// status = status + "/" +
							// "COMPLETION_STATUS_NOT_COMPLETED";
							status = status + "/" + "NOT_COMPLETED";
						} else if (estadoFinal.equals(Job.COMPLETION_STATUS_COMPLETED_ABNORMALLY)) {
							// status = status + "/" +
							// "COMPLETION_STATUS_COMPLETED_ABNORMALLY";
							status = status + "/" + "COMPLETED_ABNORMALLY";
						} else if (estadoFinal.equals(Job.COMPLETION_STATUS_COMPLETED_NORMALLY)) {
							// status = status + "/" +
							// "COMPLETION_STATUS_COMPLETED_NORMALLY";
							status = status + "/" + "COMPLETED_NORMALLY";
						}
					}

					String j_name = j.getName().trim();
					String j_user = j.getUser().trim();
					String j_number = j.getNumber().trim();
					String j_status = status.trim();

					mensajeString.append(" nombre=\"" + j_name + "\"");
					mensajeString.append(" usuario=\"" + j_user + "\"");
					mensajeString.append(" numero=\"" + j_number + "\"");
					mensajeString.append(" estado=\"" + j_status + "\"");

					Date jobActiveDate = j.getJobActiveDate();

					// if (jobActiveDate == null) {
					// logger.error("procesarMensaje:jobActiveDate es null!");
					// }

					// String s_horaInicio =
					// DateUtils.formatISO8601Date(jobActiveDate);
					String s_horaInicio = DateUtils.formatISO8601Date(jobActiveDate).trim();
					mensajeString.append(" hora-inicio=\"" + s_horaInicio + "\"/>");

					// logger.debug("j.getQueue()" + j.getQueue());
					// logger.debug("j.getStatus()=" + j.getStatus());
					// logger.debug("j.getPurge()=" + j.getPurge());

				} catch (Exception e) {

					logger.debug(e.getMessage());
				}
			}// while (list.hasMoreElements()) {

			mensajeString.append("</procesos-as400>");
		}

		catch (Exception e) {
			logger.debug(e.getMessage());
			logger.info(new MensajeError("Error al procesar la lista de trabajos").getTramaString());
		}
		return MessageFactory.crearMensajeRespuesta(mensajeString.toString());
	}

	private void printJobData(Job j) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException {
		if(j==null){
			return;
		}
		
//		final String jobName = j.getName();
//		final String jobNumber = j.getNumber();
//		
//		Map<String, String> m = new HashMap<String, String>();
//		m.put("jobName", jobName);
//		m.put("jobNumber", jobNumber);
		
		logger.debug("Datos de job activo="+j.toString()+";subsystem="+j.getSubsystem());
	}//printJobData
}

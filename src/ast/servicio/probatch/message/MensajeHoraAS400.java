package ast.servicio.probatch.message;

import java.io.OutputStream;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.SystemValue;

import ast.servicio.probatch.exception.MensajeErrorException;

public class MensajeHoraAS400 extends Mensaje {

	public MensajeHoraAS400(String mensajeEntrada) {
		super(mensajeEntrada);
		// TODO Auto-generated constructor stub
	}

	public MensajeHoraAS400() {
		super(null);
		StringBuffer msjHoraAS = new StringBuffer();
		msjHoraAS.append("<hora-as400 hora= \"");
		msjHoraAS.append(horaSistema());
		msjHoraAS.append("\"><fin estado=\"exito\" valor=\"0\"/>");
		this.setTramaString(msjHoraAS.toString());
	}

	private String horaSistema() {

		AS400 sys;
		StringBuffer fechaHora = new StringBuffer();

		try {
			sys = new AS400();

			SystemValue sysASFecha = new SystemValue(sys, "QDATE");
			SystemValue sysASHora = new SystemValue(sys, "QTIME");
			fechaHora.append(sysASFecha.getValue().toString() + "T" + sysASHora.getValue().toString());

		} catch (Exception e) {
			fechaHora.append(e.getMessage());
		}

		return fechaHora.toString();
	}

	@Override
	public Mensaje procesarMensaje(OutputStream osSalida) throws MensajeErrorException {
		// TODO Auto-generated method stub
		return this;
	}

}

package ast.servicio.probatch.os.service.impl;

import java.util.Iterator;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.ParametrosProceso;

public class UnixService extends LinuxService {

	public static final String IMPERSONALIZACION_USER = "/bin/su";

	public UnixService() {
		super();
	}

	public String[] getExecuteCommand(ParametrosProceso parametroP) {
		int indx = parametroP.getArgumentos().size();
		String[] retorno = new String[indx + 5];
		int i = 0;

		retorno[i] = IMPERSONALIZACION_USER;
		i++;
		retorno[i] = "-";
		i++;
		retorno[i] = parametroP.getUsuario().getNombre();
		i++;
		retorno[i] = "-c cd " + parametroP.getChdir() + "; exec";
		i++;
		retorno[i] = parametroP.getComando();
		i++;
		if (parametroP.getArgumentos() != null) {
			for (Iterator<Atributo> iterator = parametroP.getArgumentos().iterator(); iterator.hasNext();) {
				Atributo atributo = iterator.next();
				retorno[i] = atributo.getValor();
				i++;
			}
		}

		return retorno;
	}
}

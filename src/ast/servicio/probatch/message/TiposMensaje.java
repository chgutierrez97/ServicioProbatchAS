package ast.servicio.probatch.message;

public final class TiposMensaje {
	
	public static enum tipoMensaje {
		AUTENTICACION {
			public String toString() {
				return "autenticacion";
			}
		},
		PROCESO {
			public String toString() {
				return "proceso";
			}
		},
		LIMPIAR {
			public String toString() {
				return "limpiar";
			}
		},
		ESTADO {
			public String toString() {
				return "estado";
			}
		},
		VALIDAR {
			public String toString() {
				return "validar";
			}
		},
		MATAR {
			public String toString() {
				return "matar";
			}
		},
		BUSCAR_LOG {
			public String toString() {
				return "buscar-logs";
			}
		},
		HORA_AS400{
			public String toString() {
				return "hora-as400";
			}
		},
		PROCESO_AS400{
			public String toString() {
				return "procesos-as400";
			}
		}
	}
	
}

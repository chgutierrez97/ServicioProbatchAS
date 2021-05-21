package ast.servicio.probatch.domain;

import ast.servicio.probatch.factory.MessageFactory;
import ast.servicio.probatch.message.Mensaje;

public class EstadoProceso {

	private String id;
	private String nombre;
	private long ts;
	private int pid;
	private Integer estado;
	private boolean dump = false;

	public EstadoProceso(String id, String nombre, long ts, int pid, Integer estado) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.ts = ts;
		this.pid = pid;
		this.estado = estado;
	}

	public boolean isDump() {
		return dump;
	}

	public void setDump(boolean dump) {
		this.dump = dump;
	}

	public EstadoProceso() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	public Mensaje getMensajeTransicionEstado() {
		String estado;
		if (this.estado == null)
			estado = "<inicio/>";
		else if (this.estado == 0)
			estado = "<fin estado=\"exito\"/>";
		else if (this.estado == -9999)
			estado = "<fin estado=\"muerte\"/>";
		else
			estado = "<fin estado=\"falla\"/>";
		return MessageFactory.crearMensajeRespuesta("transicion", null, id, nombre, estado, getTs(), false);
	}
}

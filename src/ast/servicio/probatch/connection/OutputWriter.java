package ast.servicio.probatch.connection;

import java.io.IOException;
import java.io.OutputStream;

public class OutputWriter extends OutputStream {

	private OutputStream writer;
	private int maxOutputSize = 0;

	public OutputWriter(OutputStream writer, int maxOutputSize) {
		this.writer = writer;
		this.maxOutputSize = maxOutputSize;
	}

	@Override
	public void write(byte[] mensaje) throws IOException {
//		this.writer.flush();
//		String mensajeString = new String(mensaje);
//		if (mensajeString.length() > maxOutputSize) {
//			this.writer.write((mensajeString.substring(0, maxOutputSize) + "\n").getBytes());
//		}else{
			this.writer.write((new String(mensaje) + "\n").getBytes());
//		}
	}

	@Override
	public void write(int arg0) throws IOException {
		this.writer.write(arg0);
	}

}

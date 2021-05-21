package ast.servicio.probatch.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	/**
	 * Fecha con formato standard Cobis yyyy-MM-dd HH:mm:ss
	 */
	public static String standardDate = "yyyy-MM-dd HH:mm:ss";
	
	public static String formatStandardDate(Date date) {
		return format(date, standardDate);
	}
	
	/**
	 * Fecha con formato ISO 08601 yyyy-MM-ddTHH:mm:ss
	 */
	public static String formatISO8601Date(Date date){
		String fecha = format(date, standardDate);
		fecha = fecha.replace(" ", "T");
		return fecha;
	}
	
	public static String format(Date date, String format) {
		String result = "";
		if (date != null) {
			try {
				SimpleDateFormat shortDateFormat = new SimpleDateFormat(format);
				result = shortDateFormat.format(date);
			} catch (Exception e) {
			}
		}
		return result;
	}
	
	
}

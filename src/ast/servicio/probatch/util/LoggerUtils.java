package ast.servicio.probatch.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.service.ServicioAgente;
import ast.servicio.probatch.threads.EjecutarProceso;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

public class LoggerUtils {

	public static Logger createLoggerProceso(ParametrosProceso paramProc) {
		LoggerFactory.getLogger(LoggerUtils.class).debug("paramProc.getCategoria = "+paramProc.getCategoria());
		return createLogger(paramProc.getCategoria(), paramProc.getNombre() + "-" + paramProc.getId() + "-" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(paramProc.getTs()*1000)));
	}
	
	public static Logger getLoggerProceso(ParametrosProceso paramProc) {
		return LoggerFactory.getLogger(paramProc.getNombre() + "-" + paramProc.getId() + "-" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(paramProc.getTs()*1000)));
	}

	
	
	private static Logger createLogger(String logDir, String name) {
		ch.qos.logback.classic.Logger templateLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(EjecutarProceso.class);
		LoggerContext context = templateLogger.getLoggerContext();

		if(logDir == null || logDir.length() < 1)
			logDir = ServicioAgente.cfg.getWrkdir() + "/";
		else{
			logDir = ServicioAgente.cfg.getWrkdir() + "/" + logDir + "/";
		}

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern(context.getProperty("DEFAULT_PATTERN"));
		encoder.setContext(context);

		DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> timeBasedTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent>();
		timeBasedTriggeringPolicy.setContext(context);

		TimeBasedRollingPolicy<ILoggingEvent> timeBasedRollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
		timeBasedRollingPolicy.setContext(context);
		timeBasedRollingPolicy.setFileNamePattern(logDir + name + "." + context.getProperty("ROLLING_TEMPLATE") + ".log");
		timeBasedRollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(timeBasedTriggeringPolicy);
		timeBasedTriggeringPolicy.setTimeBasedRollingPolicy(timeBasedRollingPolicy);

		RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
		rollingFileAppender.setAppend(true);
		rollingFileAppender.setContext(context);
		rollingFileAppender.setEncoder(encoder);
		rollingFileAppender.setFile(logDir + name + ".log");
		rollingFileAppender.setName(name + "Appender");
		rollingFileAppender.setPrudent(false);
		rollingFileAppender.setRollingPolicy(timeBasedRollingPolicy);
		rollingFileAppender.setTriggeringPolicy(timeBasedTriggeringPolicy);

		timeBasedRollingPolicy.setParent(rollingFileAppender);

		encoder.start();
		timeBasedRollingPolicy.start();

		rollingFileAppender.stop();
		rollingFileAppender.start();

		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name);
		logbackLogger.setLevel(templateLogger.getLevel());
		logbackLogger.setAdditive(false);
		logbackLogger.addAppender(rollingFileAppender);

		return logbackLogger;
	}
	
	public static Logger removeLoggerAppender(String nombre) {
		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(nombre);
//		logbackLogger.getAppender("Proceso"+idProceso+ "Appender").stop();
//		logbackLogger.detachAppender(logbackLogger.getAppender("Proceso"+idProceso+ "Appender"));
		logbackLogger.detachAndStopAllAppenders();
		return logbackLogger;
	}

}

package ast.servicio.probatch.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class ReadEnv {
	public static Properties getEnvVars() throws Throwable {
		Process p = null;
		Properties envVars = new Properties();
		Runtime r = Runtime.getRuntime();
		String OS = System.getProperty("os.name").toLowerCase();
		System.out.println(OS);
		if (OS.indexOf("windows 9") > -1  ) {
			p = r.exec("command.com /c set");
		} else if ((OS.indexOf("nt") > -1) || (OS.indexOf("windows") > -1) ) {
			// thanks to JuanFran for the xp fix!
			p = r.exec("cmd.exe /c set");
		} else {
			// our last hope, we assume Unix (thanks to H. Ware for the fix)
			p = r.exec("env");
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = br.readLine()) != null) {
			int idx = line.indexOf('=');
			String key = line.substring(0, idx);
			String value = line.substring(idx + 1);
			envVars.setProperty(key, value);
			// System.out.println( key + " = " + value );
		}
		return envVars;
	}

	public static void main(String args[]) {
		try {
			Properties p = ReadEnv.getEnvVars();
			System.out.println("the current value of TEMP is : " + p.getProperty("TEMP"));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
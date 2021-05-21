package ast.servicio.probatch.test.exe;

import java.io.IOException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;

public class As400CmdRunTest {
	public void run(String[] args) throws InterruptedException, AS400SecurityException, ErrorCompletingRequestException, IOException {
		AS400 as400 = new AS400("172.28.194.101", "cabot", "accusys");
		String s_cmd = "SBMJOB CMD(CALL PGM(PRUEBA) PARM(\"1\")) JOB(PR_JOB_t1)";

		CommandCall cmd = new CommandCall(as400);
		try {
			// Run the command "CRTLIB FRED"
			if (cmd.run(s_cmd) != true) {
				// Note that there was an error
				log("program failed!");
			}
			// Show the messages (returned whether or not there was an error)
			AS400Message[] messagelist = cmd.getMessageList();
			for (int i = 0; i < messagelist.length; i++) {
				// show each message
				log(messagelist[i].getText());
			}
		} catch (Exception e) {
			log("Command " + cmd.getCommand() + " did not run!");
		}

		Thread systemThread = cmd.getSystemThread();
		log("waiting...");
		if (systemThread != null) {
			log("joining...");
			systemThread.join();
		}

		long maxWaitTime = 1000 * 60 * 10;
		long millis = 1000 * 30;
		long wait = 0;
		while (true) {
			Job job = cmd.getServerJob();
			if (job == null) {
				break;
			}

			String number = job.getNumber();
			String jobName = job.getName();

			if (number == null || jobName == null) {
				break;
			} else {
				log("job number=" + number+" , job name="+jobName);
			}

			Thread.sleep(millis);
			wait += millis;
			if (wait >= maxWaitTime) {
				break;
			}
		}

		// done with the system
		as400.disconnectAllServices();

		log("done!");
	}

	private static void log(String s) {
		System.out.println("As400CmdRunTest::" + s);
	}
}

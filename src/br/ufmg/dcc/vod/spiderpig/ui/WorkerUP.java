package br.ufmg.dcc.vod.spiderpig.ui;

import java.lang.reflect.Constructor;
import java.net.InetAddress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.spiderpig.common.LoggerInitiator;
import br.ufmg.dcc.vod.spiderpig.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.distributed.worker.WorkerActor;
import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;

public class WorkerUP extends Command {

	private static final String PORT = "p";
	private static final String LOG_FILE = "l";
	private static final String EXECUTOR_CLASS = "e";
	private static final String SLEEP_TIME = "t";

	@Override
	@SuppressWarnings("static-access")
	public Options getOptions() {
		Options opts = new Options();
		Option portOpt = OptionBuilder
				.withArgName("port")
				.hasArg()
				.isRequired()
				.withDescription("Port to Bind")
				.create(PORT);
		
		Option logFileOpt = OptionBuilder
				.withArgName("file")
				.hasArg()
				.isRequired()
				.withDescription("Log File")
				.create(LOG_FILE);
		
		Option executorOpt = OptionBuilder
				.withArgName("executor")
				.hasArg()
				.isRequired()
				.withDescription("Executor class to use")
				.create(EXECUTOR_CLASS);
		
		Option sleepTimeOpt = OptionBuilder
				.withArgName("sleep-time")
				.hasArg()
				.isRequired()
				.withDescription("Sleep time (seconds)")
				.create(SLEEP_TIME);
		
		opts.addOption(portOpt);
		opts.addOption(logFileOpt);
		opts.addOption(executorOpt);
		opts.addOption(sleepTimeOpt);
		
		return opts;
	}

	@Override
	public void exec(CommandLine cli) throws Exception {
		
		long sleepTime = Long.parseLong(cli.getOptionValue(SLEEP_TIME)) * 1000;
		int port = Integer.parseInt(cli.getOptionValue(PORT));
		LoggerInitiator.initiateLog(cli.getOptionValue(LOG_FILE));
		String cls = cli.getOptionValue(EXECUTOR_CLASS);
		
		String host = InetAddress.getLocalHost().getHostName();
		
		Constructor<?> constructor = Class.forName(cls)
				.getConstructor(long.class);
		JobExecutor executor = (JobExecutor) constructor.newInstance(sleepTime);
		
		WorkerActor actor = new WorkerActor(executor, 
				new RemoteMessageSender());
		QueueService service = new QueueService(host, port);
		actor.withSimpleQueue(service).startProcessors(1);
	}
}
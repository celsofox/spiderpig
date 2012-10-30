package br.ufmg.dcc.vod.ncrawler.distributed.worker;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.CrawlRequest;
import br.ufmg.dcc.vod.ncrawler.queue.Actor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.serializer.MessageLiteSerializer;

public class WorkerActor extends Actor<CrawlRequest> 
		implements QueueProcessor<CrawlRequest> {

	public static final String HANDLE = "Worker";
	private final JobExecutor executor;
	private final RemoteMessageSender sender;

	public WorkerActor(JobExecutor executor, RemoteMessageSender sender) {
		super(HANDLE);
		this.executor = executor;
		this.sender = sender;
	}
	
	@Override
	public QueueProcessor<CrawlRequest> getQueueProcessor() {
		return this;
	}

	@Override
	public MessageLiteSerializer<CrawlRequest> newMsgSerializer() {
		return new MessageLiteSerializer<>(CrawlRequest.newBuilder());
	}

	@Override
	public void process(CrawlRequest msg) {
		this.executor.crawl(msg.getId(),
				new InterestedProxy(msg.getCallBackID(), sender), 
				new FileSaverProxy(msg.getFileSaverID(), sender));
	}
}
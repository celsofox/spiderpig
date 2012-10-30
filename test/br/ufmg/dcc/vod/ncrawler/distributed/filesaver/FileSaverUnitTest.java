package br.ufmg.dcc.vod.ncrawler.distributed.filesaver;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import junit.framework.Assert;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaverActor;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaverImpl;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileWrapper;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;

import com.google.common.io.Files;

public class FileSaverUnitTest {

	private class ActorX extends FileSaverActor {

		private final SynchronousQueue<Object> queue;

		public ActorX(FileSaver saver, 
				SynchronousQueue<Object> queue) {
			super(saver);
			this.queue = queue;
		}

		@Override
		public void process(UploadMessage msg) {
			super.process(msg);
			try {
				queue.put(new Object());
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
		}
		
	}
	
	@Test
	public void testAll() throws Exception {
		File tmp = Files.createTempDir();
		FileSaverImpl impl = new FileSaverImpl(tmp.getAbsolutePath());
		
		SynchronousQueue<Object> waitQueue = new SynchronousQueue<>();
		
		QueueService service = new QueueService("localhost", 7676); 
		ActorX actor = new ActorX(impl, waitQueue);

		actor.withSimpleQueue(service).startProcessors(5);
		
		FileWrapper w1 = new FileWrapper("bah", "oi\nquer\ntc1".getBytes());
		FileWrapper w2 = new FileWrapper("buh", "oi\nquer\ntc2".getBytes());
		
		RemoteMessageSender sender = new RemoteMessageSender();
		ServiceID.Builder builder = ServiceID.newBuilder();
		builder.setHostname("localhost");
		builder.setPort(7676);
		builder.setHandle(actor.getHandle());
		
		ServiceID sid = builder.build();
		sender.send(sid, w1.toProtocolBuffer());
		sender.send(sid, w2.toProtocolBuffer());
		
		waitQueue.take();
		waitQueue.take();
		
		List<String> fileAsList = FileUtil.readFileToList(new File(tmp, "bah"));

		Assert.assertEquals(3, fileAsList.size());
		Assert.assertEquals(Arrays.asList("oi", "quer", "tc1"), fileAsList);

		fileAsList = FileUtil.readFileToList(new File(tmp, "buh"));

		Assert.assertEquals(3, fileAsList.size());
		Assert.assertEquals(Arrays.asList("oi", "quer", "tc2"), fileAsList);
		
		service.waitUntilWorkIsDoneAndStop(1);
	}
}

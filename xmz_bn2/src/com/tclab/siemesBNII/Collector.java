package com.tclab.siemesBNII;

import java.util.concurrent.Executor;

//消息的生成和分发
public class Collector implements IBuilder {
	IReactor dh = new DataHandler();
	StringBuilder buffer = new StringBuilder();
	Executor pool = CommonUtil.getThreadPool();

	String str_temp;
	String lastSend;
	ITransmitter transmitter;

	int maxCount = 6;
	int timeOut = 3 * 1000;
	int interval = 10; // 轮询间隔
	boolean isResponed = true; // 默认仪器已经回复过
	Thread thread = null; // 轮询线程

	@Override
	public void msgController(final String str, ITransmitter transmitter) {
		if (str == null || str.equals("") || str.length() == 0) {
			return;
		}
		log("recevier:" + str);
		if (transmitter != null) {
			this.transmitter = transmitter;
		}
		str_temp = str;
		String begin = str_temp.substring(0, 1);
		if (begin.equals(Constant.is_ack)) {
			msgDispatcher(str_temp);
			return;
		}
		if (begin.equals(Constant.is_nak)) {
			msgDispatcher(str_temp);
			return;
		}
		String end = str_temp.substring(str_temp.length() - 1,
				str_temp.length());
		boolean isEnd = end.equals(Constant.is_etx);
		if (isEnd) {
			buffer.append(str_temp);
			msgDispatcher(buffer.toString());
		} else {
			buffer.append(str_temp);
		}
	}

	private void msgDispatcher(String msg) {
		buffer.delete(0, msg.length());
		if (msg.equals(Constant.is_ack)) {
			isResponed = true;
			terminatePoll(thread);
			write2File("received:ACK");
			return;
		}
		if (msg.equals(Constant.is_nak)) {
			isResponed = true;
			terminatePoll(thread);
			write2File("received:NAK");
			transmitter.send(lastSend);
			isResponed = false; // 寻求回应
			thread = new Thread(timerTask);
			thread.start();
			write2File("send:" + lastSend);
			return;
		}
		// 进行校验，如果不正确，发送nak
		if (!CommonUtil.Checksum(msg)) {
			write2File("operator.check:checkSum error!");
			transmitter.send(Constant.is_nak);
			return;
		}

		System.out.println("msg保存到txt:" + msg);
		write2File("received:" + msg); // 保存到resdata

		String msgType = msg.substring(1, 2);
		// Result Record
		if (msgType.equals("D")) {
			transmitter.send(Constant.is_ack);
			write2File("D send:ACK");
			parseResult(msg);
		} else if (msgType.equals("J")) { // Job-list Request Record
			String communicate = dh.packageMsg(msg) + Constant.is_ack;
			transmitter.send(communicate); // 如果返回空，就发ack
			lastSend = communicate;
			System.out.println(communicate + "==");
			if (!communicate.equals(Constant.is_ack)) {
				isResponed = false;
				thread = new Thread(timerTask);
				thread.start();
			}
			write2File("J send:" + communicate);
		} else if (msgType.equals("R")) {// Control Result Record 控制
			transmitter.send(Constant.is_ack);
			write2File("R send:ACK");
			parseResult(msg);
		}
	}

	// 日志
	private void log(final String text) {
		pool.execute(new Runnable() {
			@Override
			public void run() {
				CommonUtil.log(text, true);
			}
		});
	}

	private void terminatePoll(Thread thread) {
		try {
			if (thread != null && thread.isAlive()) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void parseResult(String msg) {
		final String text = msg;
		pool.execute(new Runnable() {
			@Override
			public void run() {
				dh.parseMsg(text);
			}
		});
	}

	// 记录原始数据 resdata_ad
	private void write2File(String msg) {
		final String text = msg;
		pool.execute(new Runnable() {
			@Override
			public void run() {
				CommonUtil.write2File(text, Constant.resdata_ad, true, true);
			}
		});
	}

	Runnable timerTask = new Runnable() {
		long beginTime = System.currentTimeMillis();
		int count = 0;
		long checkTime;

		@Override
		public void run() {
			write2File("timertask time:" + beginTime + "count:" + count);
			// System.out.println(beginTime);
			while (true) {
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				checkTime = System.currentTimeMillis();
				// System.out.println(checkTime);
				if (!isResponed) {
					if (count <= maxCount) {
						if (checkTime - beginTime >= timeOut) {
							transmitter.send(lastSend);
							beginTime = System.currentTimeMillis();
							count++;
							log("repeat send: " + lastSend + "##" + count);
						}
					} else { // 虽然还没有应答， 已经超过重发次数，放弃这条消息
						transmitter.send(Constant.is_eot); // 结束本次传输
						count = 0;
						write2File("abort the respone!");
						break;
					}
				} else { // 已收到回复用，取消任务；
					count = 0;
					break;
				}
			}
		}
	};

}

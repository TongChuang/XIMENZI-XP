package com.tclab.siemesBNII;

import java.util.concurrent.Executor;

//消息的生成和分发
public class Collector_1 implements IBuilder {
	IReactor dh = new DataHandler();
	StringBuilder buffer = new StringBuilder();
	Executor pool = CommonUtil.getThreadPool();

	String str_temp;
	String lastSend;
	ITransmitter transmitter;


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
			write2File("received:ACK");
			return;
		}
		if (msg.equals(Constant.is_nak)) {
			write2File("received:NAK");
			transmitter.send(lastSend);
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
}

package com.tclab.i7600;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import oracle_7600.NewDB;
import oracle_7600.ResultInfo;
import ui.ToTrayIcon;

//消息的分解和组装
public class DataHandler implements IReactor {

	// ------------------------------------------------------------------
	SimpleDateFormat meaFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	SimpleDateFormat ybbhFormat = new SimpleDateFormat("yyyyMMdd");

	public String getDate() {
		String date = ToTrayIcon.dateButton.getText().split(" ")[0].replace(
				"-", "").trim();
		if (date == null) {
			date = ybbhFormat.format(new Date());
		}
		return date;
	}

	public String StringFilter(String str) throws PatternSyntaxException {
		return Pattern.compile("[\\s\t\r\n,%:!;?~` @#$&*{}|\\[\\]^]")
				.matcher(str).replaceAll("").trim();
	}

	// --------------------------------------------------------------------

	@Override
	public String parseMsg(String text) {
		if (text.contains("Error")) {
			return null;
		}
		if (text.toUpperCase().contains("MISSING")) {
			return null;
		}
		String item = null;
		String ybbh = null;

		String jyxm = null;
		String value = null;

		ResultInfo baseInfo = null;
		ArrayList<ResultInfo> list = new ArrayList<ResultInfo>();
		Date measureTime = new Date();

		String order = Constant.is_car + "O|";
		int pos_order = text.indexOf(order);
		if (pos_order > 0) {
			int begin = text.indexOf("|", pos_order + order.length() + 1);
			int end = text.indexOf("|", begin + 1);
			ybbh = text.substring(begin + 1, end).trim();
			if (ybbh.contains("^")) {
				String[] split = ybbh.split("\\^");
				ybbh = split[1].trim();
			}
			ybbh = StringFilter(ybbh);
			if (ybbh.length() == 4) {                               // 质控
                
			} else {
				baseInfo = NewDB.getSampleBaseInfo(ybbh, getDate()); // 基本信息
				System.out.println("-----------baseInfo.getSampleNo()= "+baseInfo.getSampleNo());
			}

		}
		String[] results = text.split("R\\|");
		String[] split = null;
		String[] split2 = null;
		int b = 0;
		String[] split3 = results[0].split("\\|");
		
		if (baseInfo != null) {
			baseInfo.setMeasuretime(measureTime); // 检验时间
		}

		for (int i = 1; i < results.length; i++) { // 从1开始
			b = results[i].trim().indexOf("|");
			if (b > 0) {
				split2 = results[i].split("\\|");
				split = split2[1].split("\\^");
				jyxm = split[3].trim().split("/")[0];
				if (jyxm == null) {
					continue;
				}
				value = split2[2].trim();
				if (value == null) {
					continue;
				}
				item = ybbh + "," + jyxm + "," + value + ",1";
				ToTrayIcon.getTray().jt_data.setText(item);
				if (baseInfo!=null) {
					baseInfo.setResult(value);
					baseInfo.setChannel(jyxm);
					try {
						list.add(baseInfo.clone());
					} catch (CloneNotSupportedException e){
						e.printStackTrace();
					}
				}
				CommonUtil.write2File(item, Constant.data_ad, true, false);
			}
		}
		if (!list.isEmpty()) {
			System.out.println("------开始保存------");
			NewDB.batchSave(list);
			System.out.println("------结束------");
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String queryData(String text) {
		StringBuilder sb = new StringBuilder();
		// 一般 Q|1|^A12000005176||ALL||||||||O<CR>
		String items = "";
		String ybbh = "";
		String s = Constant.is_car + "Q|";
		int index = text.indexOf(s);
		int indexOf = text.indexOf("|", index + s.length());
		String sub = text.substring(indexOf, text.indexOf("|", indexOf + 1))
				.trim();
		sub = sub.replace("/", "^").substring(0, sub.length());
		String[] split2 = sub.split("\\^");
		if (sub == null || sub.length() == 0) {
			return null;
		}
		String sampleId = split2[3].trim();
		if (sampleId.contains("A")) {
			String item_s = getItems(sampleId, 'O');
			if (item_s != null) {
				String[] sp = item_s.split(",");
				items = sp[0];
				ybbh = sp[1].trim();
				if (ybbh.length() == 0) {
					ybbh = "C1";
				}
			}
		}
		String sub1 = split2[2] + "^" + split2[3] + "^" + split2[4] + "^"
				+ split2[5] + "^" + split2[6];
		CommonUtil.log("sampleId=" + sampleId, true);

		String dataHeader = "H|\\^&|||host^1|||||H7600|TSDWN^REPLY|P|1"
				+ Constant.is_car;
		String patient = "P|1" + Constant.is_car;
		String dataOrder = "O|1|" + sub1 + "|R1|" + items
				+ "|R||||||N||^^||||||" + ybbh + "^^^^||||||O"
				+ Constant.is_car;
		String dataTermination = "L|1|N" + Constant.is_car;
		sb.append(dataHeader).append(patient).append(dataOrder)
				.append(dataTermination);
		int dataLength = sb.length();
		CommonUtil.log("dataLength=" + dataLength, true);
		return sb.toString();
	}

	// 查询数据库，得到所要做的项目
	public String getItems(String sampleId, char identifer) {
		// TODO query
		String query =NewDB.query(sampleId);
		//String query = new DBConn().query(sampleId);
		String[] split = query.split(",");
		int length = split.length;
		StringBuilder pairs = new StringBuilder();
		for (int i = 0; i < length - 1; i++) {
			pairs.append("^^^").append(split[i].trim()).append("/")
					.append("\\");
		}
		String sub = pairs.substring(0, pairs.length() - 1);

		return sub + "," + split[length - 1];
	}
}

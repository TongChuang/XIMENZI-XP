package com.tclab.e601;

import interf.IReactor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import oracle_7600.NewDB;
import oracle_7600.ResultInfo;
import ui.ToTrayIcon;
import util.CommonUtil;
import cons.Constant;

//消息的分解和组装
public class DataHandler implements IReactor {
	static HashMap<String, String> qcMap;

	static {
		qcMap = readFromFile(Constant.qc);
	}

	// ------------------------------------------------------------------
	SimpleDateFormat sdFormat = new SimpleDateFormat("ddMMyyHHmmss");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public String getDate() {
		String date = ToTrayIcon.dateButton.getText().split(" ")[0].replace(
				"-", "").trim();
		if (date == null) {
			date = sdf.format(new Date());
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
		String order = Constant.is_car + "O|";
		int pos_order = text.indexOf(order);

		// H|\^&|||cobas6000^1|||||host|RSUPL^REAL|P|1
		// P|1|||||||U||||||^
		// O|1|
		// A12000139608|0^5054^1^^S1^SC|^^^49^\^^^50^\^^^53^\^^^54^\^^^67^|R||||||N||||1|||||||20160715084931|||F
		// C|1|I|1 ^^^^|G
		// R|1|^^^49//not|1.13|ng/mL||N||F||1
		// |20160715085047||E11C|1|I|0|IR|2|^^^50//not|1.10|ng/mL||N||F||1
		// |20160715085108||E12C|1|I|0|IR|3|^^^53//not|16.90|U/mL||N||F||1
		// |20160715085129||E11C|1|I|0|IR|4|^^^54//not|23.47|U/mL||N||F||1
		// |20160715085150||E12C|1|I|0|IR|5|^^^67//not|71.34|ng/mL||N||F||1
		// |20160715085211||E11C|1|I|0|I
		// L|1|N24

		// System.out.println("pos_order" + pos_order);
		ResultInfo baseInfo = null;
		Date measuretime = new Date();
		ArrayList<ResultInfo> list = new ArrayList<ResultInfo>();

		if (pos_order > 0) {
			int begin = text.indexOf("|", pos_order + order.length() + 1);
			int end = text.indexOf("|", begin + 1);
			ybbh = text.substring(begin + 1, end).trim();
			if (ybbh.contains("PC") || ybbh.length() == 4) { // 质控 "7771"
				if (qcMap != null && !qcMap.isEmpty()) {
					String str = qcMap.get(ybbh.split(" ")[1].trim());
					if (str != null) {
						ybbh = str;
					}
				}
			} else { // 普通样本
				baseInfo = NewDB.getSampleBaseInfo(ybbh, getDate());
				if (baseInfo != null) {
					baseInfo.setMeasuretime(measuretime); // 检验时间
				}
			}
		}

		String[] results = text.split("R\\|");
		String[] split = null;
		String[] split2 = null;
		int b = 0;
		for (int i = 1; i < results.length; i++) { // 从1开始
			b = results[i].trim().indexOf("|");
			if (b > 0) {
				split2 = results[i].split("\\|");
				// System.out.println("split2.length:" + split2.length);
				split = split2[1].split("\\^");
				// System.out.println(split2[1]);
				jyxm = split[3].trim().split("/")[0];
				// System.out.println("jyxm=" + jyxm);
				if (jyxm == null) {
					continue;
				}
				value = split2[2].trim();
				if (value.contains("^")) {
					String[] split3 = value.split("\\^");
					value = split3[1];
				}
				String flag = split2[5].trim();
				if (flag.equals("LL")) {
					value = "<" + value;
				} else if (flag.equals("HH")) {
					value = ">" + value;
				}
				// System.out.println("value=" + value);
				if (value == null) {
					continue;
				}
				item = ybbh + "," + jyxm + "," + value + ",1";
				CommonUtil.write2File(item, Constant.data_ad, true, false);
				ToTrayIcon.getTray().jt_data.setText(item);

				if (baseInfo != null) {
					baseInfo.setResult(value);
					baseInfo.setChannel(jyxm);
					try {
						list.add(baseInfo.clone());
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// 持久化
		if (list.size() > 0) {
			NewDB.batchSave(list);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String queryData(String text) {
		StringBuilder sb = new StringBuilder();
		// H|\^&|||cobas6000^1|||||host|TSREQ^REAL|P|1Q|1|^^
		// A12000151474^0^5069^5^^S1^SC^R1|
		// |ALL||||||||OL|1|N0B
		String ybbh = "";
		String items = "";
		String s = Constant.is_car + "Q|";
		int index = text.indexOf(s);
		int indexOf = text.indexOf("|", index + s.length());
		String sub = text.substring(indexOf, text.indexOf("|", indexOf + 1))
				.trim();
		// sub = sub.replace("/", "^").substring(0, sub.length());
		String[] split2 = sub.split("\\^");
		// System.out.println(sub);
		if (sub == null || sub.length() == 0) {
			return null;
		}
		String sampleId = split2[2].trim();
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
		// System.out.println("sampleId=" + sampleId);

		String sub1 = split2[3] + "^" + split2[4] + "^" + split2[5] + "^"
				+ split2[6] + "^" + split2[7] + "^" + split2[8];
		// System.out.println("sub1="+sub1);
		CommonUtil.log("sampleId=" + sampleId, true);
		String dataHeader = "H|\\^&|||cobas6000^1|||||host|TSDWN^REPLY"
				+ Constant.is_car;
		String patient = "P|1" + Constant.is_car;
		// 1H|\^&|||cobas6000^1|||||host|TSDWN^REPLY|1
		// P|1
		// O|1|A12000151474|0^5069^5^^S1^SC|^^^50^1\^^^49^1\^^^51^1\^^^53^1\^^^54^1\^^^67^1|R||||||A||^^||1||||^^^^||||||O
		// C|1|L|30^C2^C3^C4^C5|G
		// L|1|N
		// 24
		String dataOrder = "O|1|" + sampleId + "|" + sub1 + "|" + items
				+ "|R||||||A||^^||1||||^^^^||||||O" + Constant.is_car;
		String comment = "C|1|L|" + ybbh + "^C2^C3^C4^C5|G" + Constant.is_car;
		String dataTermination = "L|1|N" + Constant.is_car;
		sb.append(dataHeader).append(patient).append(dataOrder).append(comment)
				.append(dataTermination);
		// System.out.println(sb.toString());
		int dataLength = sb.length();
		// System.out.println("dataLength=" + dataLength);
		CommonUtil.log("dataLength=" + dataLength, true);
		return sb.toString();
	}

	// 查询数据库，得到所要做的项目
	public String getItems(String sampleId, char identifer) {
		// TODO query
		String query = NewDB.query(sampleId); // 新库查询方法
		// String query = new DBConn().query(sampleId);
		if (query == null) {
			return null;
		}
		// System.out.println("query"+query);
		String[] split = query.split(",");
		int length = split.length;
		StringBuilder pairs = new StringBuilder();
		for (int i = 0; i < length - 1; i++) {
			pairs.append("^^^").append(split[i].trim()).append("^").append("1")
					.append("\\");
		}
		String sub = pairs.substring(0, pairs.length() - 1);
		return sub + "," + split[length - 1];
	}

	public static HashMap<String, String> readFromFile(String path) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		String tempstr = null;
		FileInputStream fis = null;
		BufferedReader br = null;
		try {
			File file = new File(path);
			File parentFile = file.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis));
			while ((tempstr = br.readLine()) != null) {
				if (tempstr.contains("=")) {
					String[] split = tempstr.split("=");
					hashMap.put(split[0].trim(), split[1].trim());
				}
			}

		} catch (IOException ex) {
			System.out.println(ex.getStackTrace());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return hashMap;
	}
}

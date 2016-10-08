package com.tclab.actop700;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import oracle_7600.NewDB;
import oracle_7600.ResultInfo;
import ui.ToTrayIcon;
import util.CommonUtil;

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
		// -----
		Date measureTime = new Date();
		ResultInfo baseInfo = null;
		ArrayList<ResultInfo> list = new ArrayList<ResultInfo>();

		String order = Constant.is_car + "O|";
		int pos_order = text.indexOf(order);
		System.out.println("pos_order" + pos_order);
		if (pos_order >= 0) {
			int begin = text.indexOf("|", pos_order + order.length() + 1);
			int end = text.indexOf("|", begin + 1);
			ybbh = text.substring(begin + 1, end).trim();
			if (ybbh.contains("^")) {
				String[] split = ybbh.split("\\^");
				ybbh = split[1].trim(); //
			}
			if (ybbh.length() == 4) {

			} else {
				baseInfo = NewDB.getSampleBaseInfo(ybbh, getDate());
				// 检验时间
				baseInfo.setMeasuretime(measureTime);
			}
		}
		String[] results = text.split(Constant.is_car + "R\\|");
		String[] split = null;
		String[] split2 = null;

		int b = 0;
		for (int i = 1; i < results.length; i++) { // 从1开始
			b = results[i].trim().indexOf("|");
			if (b > 0) {
				split2 = results[i].split("\\|");
				split = split2[1].split("\\^");
				jyxm = split[3].trim();
				String unit = split2[3].trim();
				if (jyxm.equals("131") && unit.equals("INR")) {
					jyxm = "132";
					value = split2[2].trim();
				} else if (jyxm.equals("2244") && unit.equals("ng/mL")) {
					jyxm = "2501";
					value = split2[2].trim();
				} else if (jyxm.equals("2241") && unit.equals("ng/mL")) {
					jyxm = "2501";
					value = split2[2].trim();
				} else if (jyxm.equals("2504") && unit.equals("ng/mL")) {
					jyxm = "2501";
					value = split2[2].trim();
				} else if (jyxm.equals("2041") && unit.equals("g/L")) {
					jyxm = "2011";
					value = split2[2].trim();
				} else if (jyxm.equals("2051") && unit.equals("g/L")) {
					jyxm = "2011";
					value = split2[2].trim();
				} else if (jyxm.equals("131") && unit.equals("s")) {
					value = split2[2].trim();
				} else if (jyxm.equals("921") && unit.equals("s")) {
					value = split2[2].trim();
				} else if (jyxm.equals("1201") && unit.equals("s")) {
					value = split2[2].trim();
				} else if (jyxm.equals("2011") && unit.equals("g/L")) {
					value = split2[2].trim();
				} else if (jyxm.equals("2501") && unit.equals("ng/mL")) {
					value = split2[2].trim();
				} else if (jyxm.equals("1202") && unit.equals("s")) {
					jyxm = "1201";
					value = split2[2].trim();
				}
				if (value != null && !value.equals("FAILED")) {
					item = ybbh + "," + jyxm + "," + value + ",1";
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
					CommonUtil.write2File(item, Constant.data_ad, true, false);
				}
				value = null;
			}
		}
		if (baseInfo != null) {
			baseInfo.setResult("31.0");
			baseInfo.setChannel("1600");
			try {
				list.add(baseInfo.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			baseInfo.setResult("10.6");
			baseInfo.setChannel("1500");
			try {
				list.add(baseInfo.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}

		if (!list.isEmpty()) {
			NewDB.batchSave(list);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String queryData(String text) {
		StringBuilder sb = new StringBuilder();
		String s = Constant.is_car + "Q|";
		String[] split = text.split(Constant.is_car);
		// H|@^\|<1470680644_19838><1470680644_19839>||14070778|||||1||P|1394-97|20160808142404
		String sampleId = "";
		String[] re_header = split[0].split("\\|");
		String dataHeader = re_header[0] + "|" + re_header[1] + "|"
				+ re_header[2] + "|" + re_header[3] + "|" + re_header[9]
				+ "|||||" + re_header[4] + "||P|1394-97|"
				+ CommonUtil.getDateTime(new Date()) + Constant.is_car;
		sb.append(dataHeader);

		// Q|1|^75||||||||||O@N
		String items = "";
		String orderSeq = "";
		String patientSeq = "";
		String patient = "P|1||||^|||U|||||^" + Constant.is_car;
		String idByTop = "";
		String dataOrder = "";

		int index = text.indexOf(s);
		int indexOf = text.indexOf("|", index + s.length());
		String sub = text.substring(indexOf, text.indexOf("|", indexOf + 1))
				.trim();
		if (sub.contains("@")) {
			String[] split2 = sub.split("@");
			for (int i = 0; i < split2.length; i++) {
				String[] split3 = split2[i].split("\\^");
				sampleId = split3[1].trim();
				if (sampleId.contains("A")) {
					String item_s = getItems(sampleId, 'O');
					if (item_s != null) {
						String[] sp = item_s.split(",");
						items = sp[0];
						orderSeq = String.valueOf(1); // 设为1
						patientSeq = String.valueOf(i + 1);
						patient = "P|" + patientSeq + "||||^|||U|||||^"
								+ Constant.is_car;
						dataOrder = "O|" + orderSeq + "|" + sampleId + "|"
								+ idByTop + "|" + items + "|R|"
								+ CommonUtil.getDateTime(new Date())
								+ "|||||A||||P||||||||||Q" + Constant.is_car;
						sb.append(patient).append(dataOrder);
					}
				} else {
					patientSeq = String.valueOf(i + 1);
					patient = "P|" + patientSeq + "||||^|||U|||||^"
							+ Constant.is_car;
					orderSeq = String.valueOf(1); // 设为1
					dataOrder = "O|" + orderSeq + "|" + sampleId + "|"
							+ idByTop + "|" + items + "|R|"
							+ CommonUtil.getDateTime(new Date())
							+ "|||||A||||P||||||||||Q" + Constant.is_car;
					sb.append(patient).append(dataOrder);
				}
			}
		} else {
			String[] split2 = sub.split("\\^");
			if (sub == null || sub.length() == 0) {
				return null;
			}
			sampleId = split2[1].trim();
			if (sampleId.contains("A")) {
				String item_s = getItems(sampleId, 'O');
				if (item_s != null) {
					String[] sp = item_s.split(",");
					items = sp[0];
					patientSeq = "1";
					patient = "P|" + patientSeq + "||||^|||U|||||^"
							+ Constant.is_car;
					dataOrder = "O|" + "1" + "|" + sampleId + "|" + idByTop
							+ "|" + items + "|R|"
							+ CommonUtil.getDateTime(new Date())
							+ "|||||A||||P||||||||||Q" + Constant.is_car;
					sb.append(patient).append(dataOrder);
				}
			} else {
				orderSeq = String.valueOf(1); // 设为1
				dataOrder = "O|" + orderSeq + "|" + sampleId + "|" + idByTop
						+ "|" + items + "|R|"
						+ CommonUtil.getDateTime(new Date())
						+ "|||||A||||P||||||||||Q" + Constant.is_car;
				sb.append(patient).append(dataOrder);
			}
		}

		// O | 1 | 8201 | | ^ ^ ^ 900 | S | 20000715143243 | | | | | N | | | | P
		// | | | | | | | | | | O <CR>
		String dataTermination = "L|1|N" + Constant.is_car;
		sb.append(dataTermination);
		return sb.toString();
	}

	DBConn dbConn = new DBConn();

	// 查询数据库，得到所要做的项目
	public String getItems(String sampleId, char identifer) {
		// TODO query
		String query = NewDB.query(sampleId);
		// String query = dbConn.query(sampleId);
		// System.out.println("query:"+query);
		String[] split = query.split(",");
		int length = split.length;
		StringBuilder pairs = new StringBuilder();
		for (int i = 0; i < length - 1; i++) {
			pairs.append("^^^").append(split[i].trim()).append("@");
		}
		String sub = pairs.substring(0, pairs.length() - 1);
		return sub + "," + split[length - 1];
	}
}

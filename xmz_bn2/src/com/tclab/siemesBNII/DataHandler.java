package com.tclab.siemesBNII;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import oracle_7600.NewDB;
import oracle_7600.ResultInfo;

import ui.ToTrayIcon;

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
		ArrayList<ResultInfo> list = new ArrayList<ResultInfo>();
		ResultInfo baseiInfo = null;
		Date measureTime = null;
		String ybbh = "";
		String type = text.substring(1, 2);
		ybbh = text.substring(2, 31).trim(); //
		if (type.equals("R")) { // 质控
			if (qcMap != null && !qcMap.isEmpty()) {
				if (qcMap.get(ybbh) != null) {
					ybbh = qcMap.get(ybbh);
				}
			}
		} else {
			baseiInfo = NewDB.getSampleBaseInfo(ybbh, getDate());
		}

		String yqxmdh = text.substring(31, 33).trim();
		String flag = text.substring(33, 37).trim();
		if (flag.equals("1") || flag.equals("5") || flag.equals("9")
				|| flag.equals("13")) {
			flag = ">";
		} else if (flag.equals("2") || flag.equals("6") || flag.equals("10")
				|| flag.equals("14")) {
			flag = "<";
		} else {
			flag = "";
		}
		try {
			measureTime = sdFormat.parse(text.substring(73, 85).trim());
		} catch (ParseException e) {
			measureTime = new Date();
			e.printStackTrace();
		}

		if (baseiInfo != null) {
			baseiInfo.setMeasuretime(measureTime);
		}

		// 取结果
		String result = text.substring(37, 52).trim();
		String result_1 = result.split("E")[0];
		String multiple = "E" + result.split("E")[1];

		float factor = 0;
		if (multiple.equals("E+00")) {
			factor = 1;
		} else if (multiple.equals("E+01")) {
			factor = 10;
		} else if (multiple.equals("E+02")) {
			factor = 100;
		} else if (multiple.equals("E+03")) {
			factor = 1000;
		} else if (multiple.equals("E+04")) {
			factor = 10000;
		} else if (multiple.equals("E+05")) {
			factor = 100000;
		} else if (multiple.equals("E-01")) {
			factor = (float) 0.1;
		} else if (multiple.equals("E-02")) {
			factor = (float) 0.01;
		} else if (multiple.equals("E-03")) {
			factor = (float) 0.001;
		} else if (multiple.equals("E-04")) {
			factor = (float) 0.0001;
		} else if (multiple.equals("E-05")) {
			factor = (float) 0.00001;
		}
		double result_2 = (Double.valueOf(result_1) * factor);
		DecimalFormat format = new DecimalFormat("#.###");
		String result_3 = format.format(result_2);

		String item = ybbh + "," + yqxmdh + "," + flag + result_3 + ",1";
		CommonUtil.write2File(item, Constant.data_ad, true, false);
		ToTrayIcon.getTray().jt_data.setText(item);
		if (baseiInfo != null) {
			baseiInfo.setResult(flag + result_3);
			baseiInfo.setChannel(yqxmdh);
			try {
				list.add(baseiInfo.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		if (!list.isEmpty()) {
			NewDB.batchSave(list);
		}
		return item;
	}

	@Override
	public String packageMsg(String text) {
		int car = text.indexOf(String.valueOf(Constant.car));
		System.out.println(car);
		String sampleId = text.substring(2, car - 1); // 29位
		System.out.println("sampleId=" + sampleId);
		String data = getData(sampleId, 'J'); // id包含空格
		return Constant.is_stx + data;
	}

	private String formatStr(String str) {
		if (str.length() < 2) {
			str = "0" + str;
		}
		return str;
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

	// 查询数据库，得到所要做的项目
	public String getData(String sampleId, char identifer) {
		StringBuilder data = new StringBuilder(identifer + ""); // 包括标识
		String birthday = "31121955";
		String sex = "M";
		String state = "0";
		String remark = "00"; // 一般传输00
		String optField = "0";

		String dilution = "0";
		// TODO query
		String query = NewDB.query(sampleId);
		// String query = new DBConn().query(sampleId);
		String[] split = query.split(",");
		StringBuilder pairs = new StringBuilder(); // dilution pairs
		for (int i = 0; i < split.length; i++) {
			String testNo = formatStr(split[i]);
			pairs.append(testNo).append(dilution);
		}
		data.append(sampleId).append(birthday).append(sex).append(state)
				.append(remark).append(optField).append(pairs);
		int bcc = CommonUtil.getBCC(data.toString());
		data.append((char) bcc).append(Constant.car).append(Constant.is_etx);
		System.out.println(data.toString());
		return data.toString();
	}
}

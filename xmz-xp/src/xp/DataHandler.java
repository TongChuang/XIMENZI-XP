package xp;

import interf.IReactor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import oracle_7600.NewDB;
import oracle_7600.ResultInfo;
import ui.ToTrayIcon;
import util.CommonUtil;
import cons.Constant;

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
		ArrayList<ResultInfo> list = new ArrayList<ResultInfo>();
		ResultInfo baseInfo = null;
		Date measureTimeDate = new Date();

		String ybbh = null;
		String jyxm = null;
		String value = null;
		String order = Constant.is_car + "O|";
		int pos_order = text.indexOf(order);
		if (pos_order > 0) {
			int begin = text.indexOf("|", pos_order + order.length() + 1);
			int end = text.indexOf("|", begin + 1);
			ybbh = text.substring(begin + 1, end).trim();
			if (ybbh.contains("^")) {
				String[] split = ybbh.split("\\^");
				ybbh = split[0].trim();
			}
			if (ybbh.length() == 4) {

			} else {
				baseInfo = NewDB.getSampleBaseInfo(ybbh, getDate());
				baseInfo.setMeasuretime(measureTimeDate);
			}
		}
		String[] results = text.split("R\\|");
		String sub = null;
		String[] split = null;
		int b, c, d = 0;
		for (int i = 0; i < results.length; i++) {
			b = results[i].trim().indexOf("|");
			if (b > 0) {
				c = results[i].trim().indexOf("|", b + 1);
				sub = results[i].trim().substring(b + 1, c).trim();
				if (!sub.contains("DOSE")) {
					continue;
				}
				split = sub.split("\\^");
				jyxm = split[3].trim();
				if (jyxm == null) {
					continue;
				}
				d = results[i].trim().indexOf("|", c + 1);
				value = results[i].trim().substring(c + 1, d).trim();
				if (value == null) {
					continue;
				}
				item = ybbh + "," + jyxm + "," + value + ",1";

				if (baseInfo != null) {
					baseInfo.setResult(value);
					baseInfo.setChannel(jyxm);
					try {
						list.add(baseInfo.clone());
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
				}
				ToTrayIcon.getTray().jt_data.setText(item);
				CommonUtil.write2File(item, Constant.data_ad, true, false);
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
		ArrayList<String> list = new ArrayList<String>();
		// 一般 Q|1|^A12000005176||ALL||||||||O<CR>
		String s = Constant.is_car + "Q|";
		int index = text.indexOf(s);
		int indexOf = text.indexOf("|", index + s.length());
		String sub = text.substring(indexOf, text.indexOf("|", indexOf + 1))
				.trim();
		if (sub == null || sub.length() == 0) {
			return null;
		}
		String[] split = sub.split("\\^");
		String sampleId = split[1].trim();
		CommonUtil.log("sampleId=" + sampleId, true);
		System.out.println("sampleId=" + sampleId);
		// H|\^&|<CR>
		// L|1<CR>
		// P|1|20|||J^A^B||1956|M|||||J^A^B|||||||||||G_d
		String dataHeader = "H|\\^&|||LIS_ID|||||NG_LIS||P|1" + Constant.is_car;
		String patient = "P|1|||||||U" + Constant.is_car;
		String dataOrder = getOrder(sampleId, 'O');
		String dataTermination = "L|1|F" + Constant.is_car; // 假设只一帧
		sb.append(dataHeader).append(patient).append(dataOrder)
				.append(dataTermination);
		return sb.toString();
	}

	// 查询数据库，得到所要做的项目
	public String getOrder(String sampleId, char identifer) {
		// O|1|18653||^^^T4\^^^HCG\^^^P1234|R| | | | | |Q| | | | | | | | | | | |
		// | |O\Q<CR>
		// O|1|18653^0037^B||^^^T4\^^^HCG\^^^P1234|S| | | | | | | | | | | | | |
		// | | | | | |F<CR>
		StringBuilder data = new StringBuilder("O|1|").append(sampleId).append(
				"||"); // 包括标识
		// TODO query
		String query = NewDB.query(sampleId);
		String[] split = query.split(",");
		StringBuilder pairs = new StringBuilder();
		for (int i = 0; i < split.length; i++) {
			pairs.append("^^^").append(split[i].trim()).append("\\");
		}
		String sub = pairs.substring(0, pairs.length() - 1);
		data.append(sub).append("|R|").append("|||||||||||||||||||")
				.append("O\\Q").append(Constant.is_car);
		System.out.println(data.toString());
		return data.toString();
	}
}

package cons;

import java.util.Date;

import util.CommonUtil;

public class Constant {
	public static String is_ack = String.valueOf((char)06);
	public static String is_enq = String.valueOf((char)05);
	public static String is_eot = String.valueOf((char)04);
	public static String is_etx = String.valueOf((char)03);
	public static String is_stx = String.valueOf((char)02);
	public static String is_nak = String.valueOf((char)21);
	public static String is_etb = String.valueOf((char)23);
	public static String is_ret=String.valueOf((char)10);  //    LF
	public static String is_car=String.valueOf((char)13);  //    CR   \r    回车
	
	public static String is_vl = String.valueOf((char)124); //  垂线 ｜
	public static String is_bs = String.valueOf((char)92); //   反斜杠  \
	public static String is_caret = String.valueOf((char)94); // 脱字符 ^
	public static String is_amp = String.valueOf((char)38); // 和号 &
	public static char ret = (char)10;  //    \n
	public static char car = (char)13;  //    \r
	
	public static String qc="c://lis/liscomm/QC.ini";
	public static String log_ad = "c://lis/liscomm/templog.txt";
	public static String resdata_ad = "c://lis/liscomm/out/"+CommonUtil.getPastDate(new Date(), 0)+".txt";
	public static String data_ad = "c://lis/liscomm/comm1.db";
}

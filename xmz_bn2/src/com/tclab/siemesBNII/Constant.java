package com.tclab.siemesBNII;

import java.util.Date;

public class Constant {
	public static String is_ack = String.valueOf((char)06);
	public static String is_enq = String.valueOf((char)05);
	public static String is_eot = String.valueOf((char)04);
	public static String is_etx = String.valueOf((char)03);
	public static String is_stx = String.valueOf((char)02);
	public static String is_nak = String.valueOf((char)21);
	public static char ret = (char)10;  //\r
	public static char car = (char)13;  //\n
	
	public static String log_ad = "c://lis/liscomm/templog.txt";
	public static String resdata_ad = "c://lis/liscomm/out/"+CommonUtil.getPastDate(new Date(), 0)+".txt";
	public static String data_ad = "c://lis/liscomm/comm1.db";
	public static String qc="C://lis/liscomm/QC.ini";
}

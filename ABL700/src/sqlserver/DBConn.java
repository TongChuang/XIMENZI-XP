package sqlserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import abl700.CommonUtil;

public class DBConn {

	static Connection conn = null;
	public static Connection update=null;
	static Connection monitor=null;
	static boolean isOpen = false;
	static StringBuilder dbUrl = new StringBuilder();
	static StringBuilder csdbUrl = new StringBuilder();
	static PreparedStatement ps_1 = null, ps_2 = null, ps_3 = null;
	static ResultSet rs_1 = null, rs_2 = null, rs_3 = null;
	
	public static  boolean isOpen() {
		try {
			if (conn==null||monitor==null||update==null) {
				return false;
			}
			if (conn.isClosed()||monitor.isClosed()||update.isClosed()) {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static void open() {
		String driverName = DBConfig.driverName;
		dbUrl.append(DBConfig.jdbc).append(DBConfig.host)
				.append(DBConfig.dbName);
		//------------------------------------
		csdbUrl.append(DBConfig.jdbc).append(DBConfig.host)
		.append(DBConfig.csDbName);
		try {
			Class.forName(driverName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			conn = DriverManager.getConnection(dbUrl.toString(),
					DBConfig.userName, DBConfig.userPwd);
			CommonUtil.log("DBConn.connect:database connect success", true);
			monitor = DriverManager.getConnection(dbUrl.toString(),
					DBConfig.userName, DBConfig.userPwd);
			update = DriverManager.getConnection(csdbUrl.toString(),
					DBConfig.userName, DBConfig.userPwd);
			CommonUtil.log("DBConn.connect:monitor----database connect success", true);
			System.out.println("conntect success!==");

		} catch (SQLException e) {
			e.printStackTrace();
			CommonUtil.log("DBConn.open:db connect failed! please check the network", true);
		}
	}

	public String query(String sampleId) { // 'A12000129836'
		StringBuilder yqxmdhs = new StringBuilder();
		String yqdh = null, cdrq = null, ybbh = null, xmdh = null;
		String sql_1 = Sqls.sql_1;
		try {
			ps_1 = conn.prepareStatement(sql_1);
			ps_1.setString(1, sampleId.trim());
			rs_1 = ps_1.executeQuery();
			while (rs_1.next()) {
				yqdh = rs_1.getString("yqdh");
				cdrq = rs_1.getString("cdrq").substring(0, 10);
				ybbh = rs_1.getString("ybbh");
			//	System.out.println(yqdh + "," + cdrq + "," + ybbh);
			}
			rs_1.close();
			ps_1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String sql_2 = Sqls.sql_2;
		try {
			ps_2 = conn.prepareStatement(sql_2);
			ps_2.setString(1, yqdh);
			ps_2.setString(2, cdrq);
			ps_2.setString(3, ybbh);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				xmdh = rs_2.getString("xmdh");
				//System.out.println(xmdh);
				String sql_3 = Sqls.sql_3;
				ps_3 = conn.prepareStatement(sql_3);
				ps_3.setString(1, yqdh);
				ps_3.setString(2, xmdh);
				rs_3 = ps_3.executeQuery();
				while (rs_3.next()) {
					//System.out.println(rs_3.getString("yqxmdh"));
					yqxmdhs.append(rs_3.getString("yqxmdh") + ",");
				}
			}
			yqxmdhs.append(ybbh);
			rs_2.close();
			ps_2.close();
			rs_3.close();
			ps_3.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String sub = yqxmdhs.toString();
		return sub;
	}

	public static void close() {
		if (isOpen) {
			try {
				conn.close();
				monitor.close();
				update.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (ps_1 != null) {
			try {
				ps_1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (ps_2 != null) {
			try {
				ps_2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (ps_3 != null) {
			try {
				ps_3.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (rs_1 != null) {
			try {
				rs_1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (rs_2 != null) {
			try {
				rs_2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (rs_3 != null) {
			try {
				rs_3.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String monitorNewSamp() {
		String query = Sqls.monitor;
		StringBuilder sBuilder=new StringBuilder();
		String string = "";
		try {
			PreparedStatement ps_1 = monitor.prepareStatement(query);
			ps_1.setString(1, "XN-9000");
			ps_1.setString(2, getDate(new Date()));
			ps_1.setString(3, "j");
			ResultSet rs_1 = ps_1.executeQuery();
			while (rs_1.next()) {
				sBuilder.append(rs_1.getString("ybid")).append(",").append(rs_1.getString("brbq")).append("|");
			}
			rs_1.close();
			ps_1.close();
			string=sBuilder.toString();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return string;
	}

	private String getDate(Date date){
		SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd");
		return df.format(date);
	}
}

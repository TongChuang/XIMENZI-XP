package com.tclab.e601;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.CommonUtil;

//加上样本编号
public class DBConn {

	static Connection conn = null;
	static boolean isOpen = false;
	static StringBuilder dbUrl = new StringBuilder();
	static PreparedStatement ps_1 = null, ps_2 = null, ps_3 = null;
	static ResultSet rs_1 = null, rs_2 = null, rs_3 = null;

	public static boolean isOpen() {
		return isOpen;
	}

	public static Connection open() {
		String driverName = DBConfig.driverName;
		dbUrl.append(DBConfig.jdbc).append(DBConfig.host)
				.append(DBConfig.dbName);
		try {
			Class.forName(driverName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			conn = DriverManager.getConnection(dbUrl.toString(),
					DBConfig.userName, DBConfig.userPwd);
			CommonUtil.log("DBConn.connect:database connect success", true);
			isOpen = true;
			System.out.println("conntect success!==");

		} catch (SQLException e) {
			e.printStackTrace();
			CommonUtil.log("DBConn.open:db connect failed! please check the network", true);
		}
		return conn;
	}

	public String query(String sampleId) { // 'A12000129836'
		StringBuilder yqxmdhs = new StringBuilder();
		String yqdh = null, cdrq = null, ybbh = null, xmdh = null;
		String sql_1 = "select yqdh,cdrq,ybbh from lis_ybxx where ybid=?";
		try {
			ps_1 = conn.prepareStatement(sql_1);
			ps_1.setString(1, sampleId.trim());
			rs_1 = ps_1.executeQuery();
			if (rs_1==null) {
				return null;
			}
			while (rs_1.next()) {
				yqdh = rs_1.getString("yqdh");
				cdrq = rs_1.getString("cdrq").substring(0, 10);
				ybbh = rs_1.getString("ybbh");
				System.out.println(yqdh + "," + cdrq + "," + ybbh);
			}
			rs_1.close();
			ps_1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String sql_2 = "select xmdh from lis_xmcdz where yqdh=? and cdrq=? and ybbh=?";
		try {
			ps_2 = conn.prepareStatement(sql_2);
			ps_2.setString(1, yqdh);
			ps_2.setString(2, cdrq);
			ps_2.setString(3, ybbh);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				xmdh = rs_2.getString("xmdh");
				System.out.println(xmdh);
				String sql_3 = "select yqxmdh from xt_yqxmdh where yqdh=? and xmdh=?";
				ps_3 = conn.prepareStatement(sql_3);
				ps_3.setString(1, yqdh);
				ps_3.setString(2, xmdh);
				rs_3 = ps_3.executeQuery();
				while (rs_3.next()) {
					System.out.println(rs_3.getString("yqxmdh"));
					yqxmdhs.append(rs_3.getString("yqxmdh") + ",");
				}
			}
			//------ybbh----------
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
}

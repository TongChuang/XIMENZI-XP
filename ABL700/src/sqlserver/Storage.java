package sqlserver;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Storage {
    public void saveResults(ArrayList<String> list){
    	try {
			if (DBConn.update == null) {
				return;
			}
			DBConn.update.setAutoCommit(false);
			// 9911,eE2,100.96,1
			for (int i = 0; i < list.size(); i++) {
				String[] split = list.get(i).split(",");
				ResBean resBean = new ResBean(split[0], split[1], split[2]);
				if (resBean.ybbh_ybid.length()==12) {
					PreparedStatement ps = DBConn.update
							.prepareStatement(Sqls.updateById);
					ps.setString(1, resBean.cdjg);
					ps.setString(2, resBean.cdjg);
					ps.setString(3, resBean.ybbh_ybid);
					ps.setString(4, resBean.yqxmdh);
					int affCount = ps.executeUpdate();
					ps.close();
					if (affCount==0) {
						//TODO 插入
					}
					
				} else {
					PreparedStatement ps = DBConn.update
							.prepareStatement(Sqls.updateByBh);
					ps.setString(1, resBean.cdjg);
					ps.setString(2, resBean.cdjg);
					ps.setString(3, resBean.ybbh_ybid);
					ps.setString(4, getDate(new Date()));//"2016-07-10"
					ps.setString(5, resBean.yqxmdh);
					int affCount = ps.executeUpdate();
					ps.close();
					if (affCount==0) {
						         
					}	
				}
			}
			System.out.println("++++update ok++++");
			DBConn.update.commit();
			DBConn.update.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    }
    
    private String getDate(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd");
		return df.format(date);
	}
}

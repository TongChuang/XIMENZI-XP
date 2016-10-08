package sqlserver;

public interface DBConfig {
	String jdbc="jdbc:sqlserver://";
	String host="10.31.96.37:1433;";         //10.31.96.37:1433;
	String userName = "sa";
	String userPwd = "Abc123";
	String dbName="DatabaseName=Elisdb";
	String csDbName="DatabaseName=ElisdB";
	String driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
}

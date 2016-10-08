package sqlserver;

public interface Sqls {
	String sql_1 = "select yqdh,cdrq,ybbh from lis_ybxx where ybid=?";
	String sql_2 = "select xmdh from lis_xmcdz where yqdh=? and cdrq=? and ybbh=?";
	String sql_3 = "select yqxmdh from xt_yqxmdh where yqdh=? and xmdh=?";
	String monitor = "select ybid,brbq from lis_ybxx where yqdh=? and cdrq=? and ybzt=?";
	String updateById = "update c set c.xmcdz=?,c.yscdz=? from lis_ybxx a,xt_yqxmdh b,lis_xmcdz c"
			+ "where a.ybid=? and a.cdrq=c.cdrq and a.ybbh=c.ybbh and b.yqxmdh=? and b.yqdh='RUNDA' and b.xmdh=c.xmdh";
	String updateByBh = "update c set c.xmcdz=?,c.yscdz=? from lis_ybxx a,xt_yqxmdh b,lis_xmcdz c "
			+ "where a.ybbh=?  and a.cdrq=? and a.cdrq=c.cdrq and a.ybbh=c.ybbh and b.yqxmdh=? and b.yqdh='RUNDA' and b.xmdh=c.xmdh";
	String check_ByBh="select c.yscdz from lis_ybxx a,xt_yqxmdh b,lis_xmcdz c where ";
	
	
	//String insertById = "insert into lis_xmcdz(ybbh,xmdh,xmcdz,yscdz) values (?,?,?)";
    String insertByBh = "insert into lis_xmcdz(ybbh,xmdh,xmcdz,yscdz) values (?,?,?)";
//------------------------------------------------------------
    String update_ByBh="declare @yqdh varchar(10) set @yqdh='?';declare @yqxmdh varchar(10) set @yqxmdh='?';" +
    		"declare @xmcdz varchar(24) set @xmcdz='?';declare @yscdz varchar(500) set @yscdz='?';" +
    		"declare @ybbh varchar(20) set @ybbh='?';declare @cdrq datetime set @cdrq='?';" +
    		"declare @temp varchar(20) " +
    		
    		"set @temp = (select yscdz from lis_ybxx a,xt_yqxmdh b,lis_xmcdz c where a.ybbh=@ybbh and " +
    		"a.cdrq=@cdrq and c.cdrq=@cdrq and c.ybbh=@ybbh and b.yqxmdh=@yqxmdh and b.yqdh=@yqdh " +
    		"and b.xmdh=c.xmdh );   declare @last varchar(20) set @last=(case when CHARINDEX('|',@temp)>0 then " +
    		"(select reverse(substring(reverse(@temp),1,charindex('|',reverse(@temp)) - 1)))else @temp end);" +
    		
    		"update c set c.yscdz=(case when @last='' or @last=null then @yscdz  " +
    		"when @yscdz=@last then @temp else @temp+'|'+@yscdz end),c.xmcdz=(case when @yscdz!=@last then @yscdz else xmcdz end) " +
    		"from lis_ybxx a,xt_yqxmdh b,lis_xmcdz c where a.ybbh=@ybbh and a.cdrq=@cdrq and c.cdrq=@cdrq and c.ybbh=@ybbh and b.yqxmdh=@yqxmdh and b.yqdh=@yqdh and b.xmdh=c.xmdh";
}

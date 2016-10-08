package oracle;

public interface NewSqls {
	//初始化  '%JD-AX4030%'OR instrument like '%XSMK-UF100%'
	String lab_index_d="select instrument,index_id,method,name,sample_from,unit,isprint from lab_index where " +
			"instrument like '%XN-9000%' OR instrument like '%XN-1000%'";
	String l_device_d = "select * from l_device where id in ('XN-9000','XN-1000')";
	String l_ylxhdescribe_d = "select ylxh,profiletest from l_ylxhdescribe where ksdm in ('210100')";
	String lab_channel_d = "select testid,channel,sampletype from lab_channel where deviceid in ('XN-9000','XN-1000')";
	
	
	 
	//-------------------------------------------------
	String l_device = "select * from l_device where id=?";
	String l_ylxhdescribe = "select ylxh,profiletest from l_ylxhdescribe where ksdm=?";
	String lab_channel = "select testid,channel,sampletype from lab_channel where deviceid=?";
	String lab_index="select instrument,index_id,method,name,sample_from,unit,isprint from lab_index where instrument like ?";
	String lab_reference = "select * from lab_testreference where testid in (?) order by testid, orderno asc";
    
	//查询
	String getSampleBy_code = "select age,sex, sampleno,cycle, ylxh,symstatus from l_sample where barcode=? and rownum<=1";
	String getsampleBy_No = "select age, sex, sampleno, ylxh, cycle, symstatus from l_sample where sampleno=? and rownum<=1";
	
	//保存
	String get_checker="select tester from l_tester_set where deviceid=? and segment=?";
	String update_sample="update l_sample set auditstatus=?,samplestatus=? where sampleno=?";
	String has_l_sample_update="select auditstatus,samplestatus from l_sample where sampleno=?";
	String hasTestResult_sql = "select * from l_testresult where sampleno=? and testid=? and rownum<=1";
	String insert_TestResult = "insert into l_testresult (sampleno,testid,deviceid,measuretime,operator,sampletype," +
			"testresult,teststatus,unit,testname,method,correctflag,refhi,reflo,resultflag,isprint) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    String update_TestResult = "update l_testresult set testresult=?,deviceid=?, measuretime=?,operator=? where sampleno=? and testid=?";

    String new_update_result="update l_testresult set testresult=?,measuretime=? ,resultflag=? where sampleno=? and testid=?";
    String getSampleByCode="select age, sex, sampleno, cycle,ageunit,sampletype from L_SAMPLE where barcode=? and rownum<=1";
    String getSampleByNO="select age, sex, sampleno, cycle,ageunit,sampletype from L_SAMPLE where sampleno=? and rownum<=1";
    String insert_TestResult_log="insert into l_testresult_log (sampleno,testid,correctflag,deviceid,measuretime,operator,refhi,reflo,resultflag,sampletype," +
			"testresult,teststatus,unit,editmark,isprint,cloudmark,testname,method,id,logip,logger,logoperate) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,ID.Nextval,?,?,?)";
    
    //监视
    String monitor_sql="select barcode,patientblh,patientname from l_sample where samplestatus=3 and (sampleno like '%XCG%' or sampleno like '%XCH%')";
    
}

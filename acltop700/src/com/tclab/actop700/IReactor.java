package com.tclab.actop700;

public interface IReactor {
   String parseMsg(String str);
   <T>T queryData(String str);
}

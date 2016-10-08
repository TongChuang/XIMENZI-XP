package com.tclab.i7600;

public interface IReactor {
   String parseMsg(String str);
   <T>T queryData(String str);
}

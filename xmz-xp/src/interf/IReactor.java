package interf;

public interface IReactor {
   String parseMsg(String str);
   <T>T queryData(String str);
}

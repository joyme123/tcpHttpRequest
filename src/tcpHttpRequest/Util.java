package tcpHttpRequest;

public class Util {
	/**
	 * 构造随机字符串
	 * @param length 字符串长度
	 * @return
	 */
	public static String generateRandomCode(int length){
		String srcCode = "qwertyuiopasdfghjklzxcvbnm7894561230";
		int srcLength = srcCode.length();
		StringBuilder dstCode = new StringBuilder();
		for(int i = 0; i < length; i++){
			int pos = (int) (Math.random()*(srcLength-1));
			dstCode.append(srcCode.charAt(pos));
		}
		return dstCode.toString();
	}
}

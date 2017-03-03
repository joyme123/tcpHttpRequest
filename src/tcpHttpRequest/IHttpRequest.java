package tcpHttpRequest;

import java.io.File;
import java.util.HashMap;

public interface IHttpRequest {
	/**
	 * 设置请求的方法
	 * @param method 请求的方法
	 */
	public void setMethod(String method);	
	
	/**
	 * 设置请求参数
	 * @param paramsMap 带有请求参数的Map
	 */
	public void setParams(HashMap<String, String> paramsMap);		
	
	/**
	 * 设置http请求的版本
	 * @param version http协议版本号
	 */
	public void setHttpVersion(String version);
	
	/**
	 * 设置请求的url地址
	 * @param url url地址
	 */
	public void setUrl(String url);
	
	/**
	 * 设置请求携带的cookie
	 * @param cookie 携带的cookie
	 */
	public void setCookie(String cookie);
	
	/**
	 * 设置接受的消息格式
	 */
	public void setAccept(String accept);
	
	/**
	 * 设置接受的编码
	 * @param encoding 编码格式
	 */
	public void setAcceptEncoding(String encoding);
	
	/**
	 * 设置UA
	 * @param userAgent
	 */
	public void setUserAgent(String userAgent);
	
	/**
	 * 上传文件
	 * @param file
	 */
	public void addFile(String key,File file);
	/**
	 * 发起请求
	 * @return int 返回的回应
	 */
	public String request();
}

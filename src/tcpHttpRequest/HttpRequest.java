package tcpHttpRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exception.UrlNotRightException;

public class HttpRequest implements IHttpRequest{
	//请求方法
	private String method;
	//请求参数
	private HashMap<String, String> paramsMap = new HashMap<String,String>();
	//http协议版本
	private String version;
	//请求的url地址
	private String url;
	//请求携带的cookie
	private String cookie;
	//接受的格式
	private String accept;
	//接受的编码
	private String acceptEncoding;
	//消息头中的host
	private String host;
	//消息头中的请求路径
	private String path;
	private String userAgent;
	//消息头
	private String header;
	//消息体,初始化为空，是为了在GET的时候直接为空
	private String body = "";	
	//boundary
	private String boundary;
	
	//是否含有文件
	private boolean hasFile;
	private HashMap<String,File> files = null;
	private final String postContentType = "application/x-www-form-urlencoded; charset=UTF-8";
	private final String postWithFileContentType = "multipart/form-data; boundary=";
	private final static String CARRIAGERETURN = "\r\n";
	private final static String SPACE = " ";
	
	public final static String GET_METHOD = "GET";
	public final static String POST_METHOD = "POST";
	public final static String HTTP1_1 = "HTTP/1.1";
	public final static String HTTP1_0 = "HTTP/1.0";
	
	public HttpRequest(){
		this.method = "GET";	//默认是GET
		this.version = "HTTP/1.1";	//版本默认是1.0
		this.cookie = "";		//cookie默认为空
		this.accept = "*/*";
		this.acceptEncoding = "deflate, br";	//默认为空
		this.userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
		this.hasFile = false;		//默认没有文件
		this.boundary = "----"+Util.generateRandomCode(35);
	}
	
	@Override
	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public void setParams(HashMap<String, String> paramsMap) {
		this.paramsMap = paramsMap;
	}
	
	@Override
	public void setHttpVersion(String version) {
		this.version = version;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
		Pattern pattern = Pattern.compile("http://(.*?)(/.*)");
		Matcher m = pattern.matcher(url);
		if(m.find()){
			this.host = m.group(1);
			this.path = m.group(2);
		}else{
			throw new UrlNotRightException("url not right!!!");
		}
	}

	@Override
	public void setCookie(String cookie) {
		this.cookie = cookie;
		
	}

	@Override
	public void setAccept(String accept) {
		this.accept = accept;
	}

	@Override
	public void setAcceptEncoding(String encoding) {
		this.acceptEncoding = encoding;
	}

	@Override
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Override
	public void addFile(String key,File file) {
		if(this.files == null)
			files = new HashMap<String,File>();
		files.put(key, file);
		this.hasFile = true;
	}
	
	/**
	 * 手动添加请求参数
	 * @param key
	 * @param value
	 */
	public void appendParam(String key,String value){
		this.paramsMap.put(key, value);
	}
	
	private void constructHeader(){
		StringBuilder sb = new StringBuilder();
		String params = "";
		//构造请求的方法，路径及版本
		if(this.method.equals(HttpRequest.GET_METHOD)){
			//如果是GET请求方式，需要构造请求参数
			StringBuilder getSb = new StringBuilder();
			Set<String> keySet = this.paramsMap.keySet();
			boolean isFirst = true;
			for(String key:keySet){
				String paramStr = "";
				try {
					paramStr = URLEncoder.encode(this.paramsMap.get(key),"utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if(isFirst){
					getSb.append("?"+key+"="+paramStr);
				}else{
					getSb.append("&"+key+"="+paramStr);
				}
			}
			params = getSb.toString();
		}
		
		sb.append(this.method+HttpRequest.SPACE+this.path+params+HttpRequest.SPACE+this.version+HttpRequest.CARRIAGERETURN);
		//构造host
		sb.append("HOST:"+HttpRequest.SPACE+this.host+HttpRequest.CARRIAGERETURN);
		//构造Connection
		if(this.version.equals(HttpRequest.HTTP1_1))
			sb.append("Connection:"+HttpRequest.SPACE+"keep-alive"+HttpRequest.CARRIAGERETURN);
		//如果是POST才构造消息体的长度
		if(this.method.equals(POST_METHOD)){
			//这一步构造消息体的长度
			sb.append("Content-Length:"+HttpRequest.SPACE+String.valueOf(this.body.length())+HttpRequest.CARRIAGERETURN);
		}
		//这一步构造接收域
		sb.append("Accept:"+HttpRequest.SPACE+this.accept+HttpRequest.CARRIAGERETURN);
		//这一步构造接收的编码格式
		sb.append("Accept-Encoding:"+HttpRequest.SPACE+this.acceptEncoding+HttpRequest.CARRIAGERETURN);
		//这一步构造UA
		sb.append("User-Agent:"+HttpRequest.SPACE+this.userAgent+HttpRequest.CARRIAGERETURN);
		//如果是post要构造Content-Type
		if(this.method.equals(HttpRequest.POST_METHOD)){
			//这一步构造Content-Type
			if(this.hasFile)
				sb.append("Content-Type:"+HttpRequest.SPACE+this.postWithFileContentType+this.boundary+HttpRequest.CARRIAGERETURN);
			else
				sb.append("Content-Type:"+HttpRequest.SPACE+this.postContentType+HttpRequest.CARRIAGERETURN);
		}
		//这一步构造Cookie
		if(this.cookie != null && !this.cookie.equals("")){
			sb.append("Cookie:"+HttpRequest.SPACE+this.cookie+HttpRequest.CARRIAGERETURN);
		}
			
		sb.append(HttpRequest.CARRIAGERETURN);
		this.header = sb.toString();
	} 
	
	private void constructBody(){
		StringBuilder sb = new StringBuilder();
		//如果没有文件，则使用www-form-urlencoded
		if(!this.hasFile){
			//下面是查询参数
			boolean first = true;
			Set<Entry<String,String>> entrySet = paramsMap.entrySet();
			for(Entry<String,String> params : entrySet){
				if(!first)
					sb.append("&");
				try {
					sb.append(params.getKey()+"="+URLEncoder.encode(params.getValue(),"utf-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				first = false;
			}
			sb.append(HttpRequest.CARRIAGERETURN);
		}else{
			//如果有文件的话,则使用multipart/formdata的方法
			
			//先构造请求参数
			Set<Entry<String,String>> entrySet = paramsMap.entrySet();
			for(Entry<String,String> params : entrySet){
				sb.append("--"+this.boundary+HttpRequest.CARRIAGERETURN);
				sb.append("Content-Disposition: form-data; name=\""+params.getKey()+"\""+HttpRequest.CARRIAGERETURN);
				sb.append(HttpRequest.CARRIAGERETURN);
				sb.append(params.getValue()+HttpRequest.CARRIAGERETURN);
			}
			
			//再构造文件
			Set<Entry<String,File>> filesEntrySet = files.entrySet();
			for(Entry<String,File> fileEntry : filesEntrySet){
				
				sb.append("--"+boundary+HttpRequest.CARRIAGERETURN);
				sb.append("Content-Disposition: form-data; name=\""+fileEntry.getKey()+"\"; "
						+ "filename=\""+fileEntry.getValue().getName()+"\""+HttpRequest.CARRIAGERETURN);
				String contentType = null;
				Path path = Paths.get(fileEntry.getValue().getAbsolutePath());
				try{
					contentType = Files.probeContentType(path);
				}catch(IOException e){
					//TODO 这里要做异常处理
					e.printStackTrace();
				}
				sb.append("Content-Type: "+contentType+HttpRequest.CARRIAGERETURN);
				sb.append(HttpRequest.CARRIAGERETURN);
				//这里将文件加入到这里
				try {
					InputStreamReader reader = new InputStreamReader(new FileInputStream(fileEntry.getValue()));
					char[] c = new char[1024];
					while(reader.read(c) != -1){
						sb.append(c);
					}
					reader.close();
				} catch (FileNotFoundException e) {
					//TODO 这里要做异常处理
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sb.append(HttpRequest.CARRIAGERETURN+"--"+boundary+"--"+HttpRequest.CARRIAGERETURN);
		}
		
		this.body = sb.toString();
	}

	@Override
	public String request() {
		//如果是post则构造消息体
		if(this.method.equals(POST_METHOD)){
			this.constructBody();	//必须先构造body，以获得body的长度
		}
		this.constructHeader();	//构造消息头
		StringBuilder response = new StringBuilder();	//请求的回应
		String hostWithoutPort = null;	//host,不带port
		int port = 80;
		if(this.host.contains(":")){
			String[] hp = this.host.split(":");
			hostWithoutPort = hp[0];
			port = Integer.parseInt(hp[1]);
		}else{
			hostWithoutPort = host;
		}
		try {
			Socket socket = new Socket(hostWithoutPort,port);
			OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
			System.err.println(this.header+this.body);
			writer.write(this.header+this.body);
			writer.flush();
			
			InputStreamReader reader = new InputStreamReader(socket.getInputStream());
			char[] cbuf = new char[1000];
			while(reader.read(cbuf) != -1){
				response.append(cbuf);
			}
			//不保持长连接，所以用完即关闭
			//socket.close();
		} catch (IOException e) {
			// TODO 这里要做异常处理
			e.printStackTrace();
		}
		return response.toString();
	}

	
}

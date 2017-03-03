package client;

import java.io.File;
import java.util.HashMap;

import tcpHttpRequest.HttpRequest;

public class Client {
	public static void main(String[] args){
		HttpRequest request = new HttpRequest();
		request.setMethod(HttpRequest.POST_METHOD);
		request.setUrl("http://localhost/php/index.php");
		HashMap<String,String> paramsMap = new HashMap<String,String>();
		paramsMap.put("action", "person_action");
		paramsMap.put("sub_action", "addPhoto");
		paramsMap.put("personId", "211");
		request.setParams(paramsMap);
		request.addFile("file", new File("/home/jiang/图片/hahahahahahahahahahh.png"));
		String response = request.request();
		System.out.println(response);
	}
}

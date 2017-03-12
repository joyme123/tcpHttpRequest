package client;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashMap;

import tcpHttpRequest.HttpRequest;

public class Client {
	
	public static void get(){
		HttpRequest request = new HttpRequest();
		request.setMethod(HttpRequest.GET_METHOD);
		request.setUrl("http://localhost/index.php");
		HashMap<String,String> paramsMap = new HashMap<String,String>();
		paramsMap.put("str", "hello world");
		request.setParams(paramsMap);
		String response = request.request();
		System.out.println(response);
	}
	
	public static void post(){
		HttpRequest request = new HttpRequest();
		request.setMethod(HttpRequest.POST_METHOD);
		request.setUrl("http://localhost/index.php");
		HashMap<String,String> paramsMap = new HashMap<String,String>();
		paramsMap.put("action", "person    action");
		paramsMap.put("sub_action", "add   Photo");
		paramsMap.put("personId", "211");
		request.setParams(paramsMap);
		String response = request.request();
		System.out.println(response);
	}
	
	public static void file(){
		HttpRequest request = new HttpRequest();
		request.setMethod(HttpRequest.POST_METHOD);
		request.setUrl("http://localhost/index.php");
		HashMap<String,String> paramsMap = new HashMap<String,String>();
		paramsMap.put("action", "person_action");
		paramsMap.put("sub_action", "addPhoto");
		paramsMap.put("personId", "211");
		request.setParams(paramsMap);
		request.addFile("file", new File("/home/joyme/Pictures/Wallpapers/1489290173204.jpg"));
		String response = request.request();
		System.out.println(response);
	}
	
	public static void main(String[] args){
		//get();
		//post();
		file();
	}
}

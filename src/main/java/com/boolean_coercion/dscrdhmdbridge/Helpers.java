package com.boolean_coercion.dscrdhmdbridge;

import okhttp3.*;
import org.json.JSONObject;
import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;

public class Helpers {

	public static String HMD_TOKEN = "[NOPE]";
	public static final String TOKEN = "[NOPE]";

	public static OkHttpClient client = new OkHttpClient();

	public static JSONObject newCall(Request request) throws IOException{
		return new JSONObject(client.newCall(request).execute().body().string());
	}

	public static JSONObject newCall(String endpoint, JSONObject json) throws IOException{
		return newCall(newRequest(endpoint, json));
	}

	public static JSONObject newCall(String endpoint, String json) throws IOException{
		return newCall(newRequest(endpoint, json));
	}

	public static void newCall(String endpoint, String json, Callback callback) {
		client.newCall(newRequest(endpoint, json)).enqueue(callback);
	}

	public static void newCall(String endpoint, JSONObject json, Callback callback) {
		newCall(endpoint, json.toString(), callback);
	}

	public static Request newRequest(String endpoint, String json){
		Request out = new Request.Builder()
				.addHeader("Content-Type", "application/json")
				.url("https://www.hackmud.com/mobile/"+endpoint+".json")
				.post(RequestBody.create(MediaType.parse("json"), json))
				.build();
		return out;
	}

	public static Request newRequest(String endpoint, JSONObject json){
		return newRequest(endpoint, json.toString());
	}
}

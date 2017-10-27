package com.boolean_coercion.dscrdhmdbridge;

import okhttp3.*;
import org.json.JSONObject;
import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;

public class Helpers {

	public static final String TOKEN = "[REDACTED]";
	public static String HMD_TOKEN = "[REDACTED]";

	public static OkHttpClient client = getUnsafeOkHttpClient();

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

	private static OkHttpClient getUnsafeOkHttpClient() {
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
					}

					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new java.security.cert.X509Certificate[]{};
					}
				}
			};

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
			builder.hostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});

			OkHttpClient okHttpClient = builder.build();
			return okHttpClient;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

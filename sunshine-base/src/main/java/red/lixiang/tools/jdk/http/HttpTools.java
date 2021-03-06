package red.lixiang.tools.jdk.http;

import com.alibaba.fastjson.JSON;
import red.lixiang.tools.jdk.StringTools;
import red.lixiang.tools.jdk.io.IOTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lixiang
 * @date 2019/12/13
 **/
public class HttpTools {


    /**
     * Constructor.
     */
    private HttpTools() {

    }

    /**
     * Do get operation for the http request.
     *
     * @return the response
     */
    public static <T> HttpResponse<T> doGet(String url, Map<String, String> paramMap, final Class<T> responseType) {
        HttpRequest request = new HttpRequest(url);
        request.setParamMap(paramMap);
        return doGet(request, responseType);
    }

    public static <T> HttpResponse<T> doGet(HttpRequest httpRequest, final Class<T> responseType) {
        httpRequest.setHttpMethod(HttpRequest.METHOD_GET);
        return doInvoke(httpRequest, responseType);
    }


    public static <T> HttpResponse<T> doPost(String url, Map<String, String> bodyMap, Class<T> responseType) {

        return doPost(url, bodyMap, HttpRequest.JSON_HEADER, responseType);
    }

    public static <T> HttpResponse<T> doPost(String url, Map<String, String> bodyMap, Map<String, String> headerMap, Class<T> responseType) {
        return doPost(url, JSON.toJSONString(bodyMap), headerMap, responseType);
    }

    public static <T> HttpResponse<T> doPost(String url, String bodyContent, Map<String, String> headerMap, Class<T> responseType) {
        HttpRequest request = new HttpRequest(url);
        request.setBodyContent(bodyContent);
        request.setHeaderMap(headerMap);
        request.setHttpMethod(HttpRequest.METHOD_POST);
        return doInvoke(request, responseType);
    }

    public static <T> HttpResponse<T> doInvoke(HttpRequest request, Class<T> responseType) {
        try {
            Map<String, String> paramMap = request.getParamMap();
            String url = request.getUrl();
            // 在url后面拼接参数,这里要注意,本来url后面就带了参数,又有paraMap的存在
            if (null != paramMap) {
                StringBuilder querySB = new StringBuilder(url.contains("?")?"&":"?");
                paramMap.forEach((key, value) -> querySB.append(key).append("=").append(value).append("&"));
                //去掉最后一个 &
                String query = querySB.substring(0, querySB.length() - 1);
                url = url + query;
            }
            // 设置代理
            Proxy proxy = Proxy.NO_PROXY;
            if (request.getProxyHost() != null && request.getProxyPort() != null) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(request.getProxyHost(), request.getProxyPort()));
            }
            if(request.getCookieManager()!=null){
                request.getCookieManager().setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
                CookieHandler.setDefault(request.getCookieManager());
            }

            InputStreamReader isr = null;
            InputStreamReader esr = null;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection(proxy);
            // 请置请求方式
            conn.setRequestMethod(request.getHttpMethod());
            // 添加header
            if(request.getHeaderMap()!=null){
                for (Map.Entry<String, String> entry : request.getHeaderMap().entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 添加请求的body
            if (StringTools.isNotBlank(request.getBodyContent())) {
                try (OutputStream outputStream = conn.getOutputStream()) {
                    outputStream.write(request.getBodyContent().getBytes(StandardCharsets.UTF_8));
                }
            }

            conn.connect();


            int statusCode = conn.getResponseCode();

            String response = null;
            try {
                if(responseType == byte[].class){
                    byte[] bytes = IOTools.readByte(conn.getInputStream());
                    return new HttpResponse<>(statusCode,(T)bytes);
                }
                if(responseType == InputStream.class){
                    return new HttpResponse<>(statusCode,(T) conn.getInputStream());
                }
                isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                response = IOTools.readString(isr);
                if (responseType == String.class) {
                    return new HttpResponse<>(statusCode, (T) response);
                }
                return new HttpResponse<T>(statusCode, JSON.parseObject(response, responseType));
            } catch (Exception e) {
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    esr = new InputStreamReader(errorStream, StandardCharsets.UTF_8);
                    try {
                        response =IOTools.readString(esr);
                        return new HttpResponse(statusCode, response);
                    } catch (IOException ioe) {
                        //ignore
                    }
                }
                e.printStackTrace();
            } finally {
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
                if (esr != null) {
                    try {
                        esr.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

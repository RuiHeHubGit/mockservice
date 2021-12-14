package mockservice;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.entity.mime.MIME.DEFAULT_CHARSET;

/**
 * 上传图片工具类
 */
public class UploadUtil {
    public static void main(String[] args) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsicGFhc2Nsb3VkLWNsaWVudC1wcm9kdWN0IiwicGFhc2Nsb3VkLWNsaWVudC1kZXZpY2UtbG9jayIsInBhYXNjbG91ZC1jbGllbnQtZGV2aWNlLWNlbnRlciIsInBhYXNjbG91ZC1jbGllbnQtcHJvdmlkZXItb3RhIiwicGFhc2Nsb3VkLWNsaWVudC11c2VyLWRldmljZSIsInByb3ZpZGVyT3YyMDIwIiwicGFhc2Nsb3VkLWNsaWVudC11aWMiLCJwYWFzY2xvdWQtY2xpZW50LW9wYyJdLCJ1c2VyX25hbWUiOiI1Iiwib3BlbmlkIjo1LCJzY29wZSI6WyIqIl0sImlzcyI6ImlvdGJ1bGwuY29tIiwiZXhwIjoxNjA5Mjk0NzQxLCJqdGkiOiI3MmMyOGFkNC02ZDQwLTQ5NTktODIxNC1iNTE4ZTA3MGUzOTIiLCJjbGllbnRfaWQiOiJwYWFzY2xvdWRjbGllbnR1aWMifQ.fUGhrF9Gpf3ddKCZZeZFQ4i7vE9s4jZzmnAJI8oVaKLfedCcY5qaZb_XeflHswBxQxNH-cNuG2Y2h8b3zsLmtgYXg4PJyq0_OiqPwcIjIP8wb34SPjruRomETkwI5XxyMq0Nxzosfh1zb_GwjohlxypbX9RYqjgocTjtncY5Mzar4xj3oQMpd5n1---cMOS2mm19bPvvFjiAlFKIdhaNXGs3_86ZJDWNBmBBGeij1ah-p6jSqzTdBSrl1q48oNX6sVo1Rij3DNs2CaqJQc7swTQ6WyZYVNHC0ykSS2zhQ6dCV9KSZbMECINExFqy_xV6MXHSQ-xxouCFfvOGYSXU9A");
        Map<String, Object> files = new HashMap<>();
        files.put("files", new File("c:/Users/herui/Desktop/135.bmp"));
        Map<String, String> params = new HashMap<>();
        postForUpload("http://localhost:8004/v1/upload", headers, files, "test.png", params);
    }
    public static String postForUpload(String url, Map<String, String> headers, Map<String, Object> files, String fileName,
                                Map<String, String> params) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        if(!CollectionUtils.isEmpty(headers)) {
            for (Map.Entry<String, String> entity : headers.entrySet()) {
                post.setHeader(entity.getKey(), entity.getValue());
            }
        }
        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setCharset(StandardCharsets.UTF_8);
            if(!CollectionUtils.isEmpty(files)) {
                //上传的文件体
                for (String key : files.keySet()) {
                    if(files.get(key) instanceof File) {
                        builder.addBinaryBody(key, (File) files.get(key));
                    }
                    if(files.get(key) instanceof InputStream) {
                        builder.addBinaryBody(key, (InputStream) files.get(key), ContentType.MULTIPART_FORM_DATA, fileName);
                    }
                    if(files.get(key) instanceof byte[]) {
                        builder.addBinaryBody(key, (byte[]) files.get(key), ContentType.MULTIPART_FORM_DATA, fileName);
                    }
                }
            }

            if(!CollectionUtils.isEmpty(params)) {
                for (Map.Entry<String, String> entity : params.entrySet()) {
                    if(StringUtils.isNotBlank(entity.getValue())) {
                        builder.addTextBody(entity.getKey(), entity.getValue());
                    }
                }
            }
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);

            // 设置连接超时时间
            int timeout = 5000;
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
            post.setConfig(requestConfig);

            entity = response.getEntity();
            if(null != entity) {
                return EntityUtils.toString(entity, Charset.forName(DEFAULT_CHARSET.displayName()));
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
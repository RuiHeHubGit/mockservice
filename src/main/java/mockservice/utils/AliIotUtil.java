package mockservice.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.iot.model.v20180120.SetDevicePropertyRequest;
import com.aliyuncs.iot.model.v20180120.SetDevicePropertyResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mockservice.domain.InvokePropertyVo;
import mockservice.service.CacheService;
import mockservice.service.impl.CacheServiceImpl;

@Slf4j
@Data
public class AliIotUtil {
    private static volatile DefaultAcsClient client;
    private static volatile CacheService<Object> cacheService;

    public DefaultAcsClient defaultAcsClient() throws ClientException {
        if (client == null) {
            synchronized (AliIotUtil.class) {
                if (client == null) {
                    cacheService = new CacheServiceImpl().getCacheService("system");
                    String accessKeyId = String.valueOf(cacheService.get("AliIot:accessKeyId"));
                    String accessKeySecret = String.valueOf(cacheService.get("AliIot:accessKeySecret"));
                    DefaultProfile.addEndpoint("cn-shanghai", "Iot", "iot.cn-shanghai.aliyuncs.com");
                    IClientProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKeyId, accessKeySecret);
                    client = new DefaultAcsClient(profile);
                    Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread t, Throwable e) {
                            distory();
                        }
                    });
                }
            }
        }
        return client;
    }

    public synchronized void distory() {
        if (client != null) {
            client.shutdown();
            client = null;
        }
    }

    public SetDevicePropertyResponse setDeviceProperties(InvokePropertyVo invokePropertyVo) {
        SetDevicePropertyResponse response = null;
        String deviceName = invokePropertyVo.getDeviceName();
        String items = invokePropertyVo.getItems();
        String iotId = invokePropertyVo.getIotId();
        SetDevicePropertyRequest setDevicePropertyRequest = new SetDevicePropertyRequest();
        setDevicePropertyRequest.setDeviceName(deviceName);
        setDevicePropertyRequest.setIotId(iotId);
        setDevicePropertyRequest.setItems(items);
        setDevicePropertyRequest.setProductKey(invokePropertyVo.getProductKey());
        try {
            response = defaultAcsClient().getAcsResponse(setDevicePropertyRequest);
            if (!response.getSuccess()) {
                log.error("设备[{}] 设置物属性 [{}] 失败 requestId {} code {} message {}",
                        iotId, items, response.getRequestId(), response.getCode(), response.getErrorMessage());
            } else {
                log.info("设备[{}] 设置物属性[{}] 成功 requestId {}", iotId, items, response.getRequestId());
            }
        } catch (ClientException e) {
            log.error("设备[{}] 设置物属性 [{}] 失败 {} requestId {} code {} message {}", iotId, items, e,
                    e.getRequestId(), e.getErrCode(), e.getErrMsg());
        }
        return response;
    }
}

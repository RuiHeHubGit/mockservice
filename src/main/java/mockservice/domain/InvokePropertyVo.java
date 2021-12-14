package mockservice.domain;

import lombok.Data;

@Data
public class InvokePropertyVo {
    private String productName;
    private String productKey;
    private String deviceName;
    private String iotId;
    private String items;
}

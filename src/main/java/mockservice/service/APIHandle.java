package mockservice.service;

import mockservice.domain.API;
import mockservice.service.impl.APIHandleImpl;

import java.lang.reflect.Method;

public interface APIHandle {
    Method getHandleMethod();

    API getApi();

    void setApi(API api);
}

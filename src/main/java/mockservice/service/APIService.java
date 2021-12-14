package mockservice.service;

import mockservice.domain.API;

import java.util.List;

public interface APIService {
    List<API> getAPIs();

    API updateAPI(API api);

    API getAPI(Integer apiID);

    API deleteAPI(Integer apiID);

    List<API> deleteAPIs(Integer[] ids);

    API addAPI(API api);
}

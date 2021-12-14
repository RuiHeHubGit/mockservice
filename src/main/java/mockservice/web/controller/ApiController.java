package mockservice.web.controller;

import mockservice.MockServiceApplication;
import mockservice.domain.API;
import mockservice.service.APIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ApiController {
    private final static Logger logger = LoggerFactory.getLogger(MockServiceApplication.class);

    @Autowired
    private APIService apiService;

    @GetMapping("apis")
    public List<API> getAPIs() {
        return apiService.getAPIs();
    }

    @DeleteMapping("apis")
    public List<API> deleteAPIs(@RequestBody Integer[] ids) {
        return apiService.deleteAPIs(ids);
    }

    @PostMapping("api")
    public API addAPI(@RequestBody API api) {
        return apiService.addAPI(api);
    }

    @PutMapping("api")
    public API updateAPI(@RequestBody API api) {
        return apiService.updateAPI(api);
    }

    @GetMapping("api/{id}")
    public API getAPI(@PathVariable("id") Integer apiID) {
        return apiService.getAPI(apiID);
    }

    @DeleteMapping("api/{id}")
    public API deleteAPI(@PathVariable("id") Integer apiID) {
        return apiService.deleteAPI(apiID);
    }
}

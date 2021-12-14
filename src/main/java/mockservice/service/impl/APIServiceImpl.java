package mockservice.service.impl;

import mockservice.domain.API;
import mockservice.service.APIService;
import mockservice.validator.RequestValidator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static mockservice.exception.ExceptionRepository.*;


@Service
public class APIServiceImpl implements BeanFactoryAware, APIService {
    private final static AtomicInteger API_ID = new AtomicInteger(999);

    private List<APIHandleImpl> apiRepository;

    private BeanFactory beanFactory;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public APIServiceImpl() {
        apiRepository = new CopyOnWriteArrayList<>();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public static Integer getAPIID() {
        return API_ID.getAndIncrement();
    }


    @Override
    public List<API> getAPIs() {
        return apiRepository.stream().map(APIHandleImpl::getApi).collect(Collectors.toList());
    }

    @Override
    public API updateAPI(API api) {

        Integer apiID = api.getId();
        if (apiID == null) {
            throw API_BAD_REQUEST.getException();
        }

        RequestValidator.validAndRegularApi(api);

        APIHandleImpl handle = getAPIHandleByapiID(apiID);
        if (handle == null) {
            throw API_NOT_FOUNT.getException();
        }

        requestMappingHandlerMapping.unregisterMapping(handle.getRequestMappingInfo());
        apiRepository.remove(handle);

        addAPI(api);

        return api;
    }

    @Override
    public API getAPI(Integer apiID) {
        APIHandleImpl handle = getAPIHandleByapiID(apiID);
        if (handle == null) {
            throw API_NOT_FOUNT.getException();
        }
        return handle.getApi();
    }

    @Override
    public API deleteAPI(Integer apiID) {
        APIHandleImpl handle = getAPIHandleByapiID(apiID);
        if (handle == null) {
            throw API_NOT_FOUNT.getException();
        }

        unregisterRequestMapping(handle);

        return handle.getApi();
    }

    @Override
    public List<API> deleteAPIs(Integer[] ids) {
        List<API> apis = new ArrayList<>();
        List<Integer> failedList = new ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            Integer id = ids[i];
            if (id == null) {
                continue;
            }
            if (id < 0 || id > API_ID.get()) {
                failedList.add(id);
                continue;
            }
            try {
                apis.add(deleteAPI(ids[i]));
            } catch (Exception e) {
                failedList.add(id);
            }
        }
        if (!failedList.isEmpty()) {
            throw API_DELETES_SOME_FAILED.getException(failedList);
        }
        return apis;
    }

    @Override
    public API addAPI(API api) {
        RequestValidator validator = RequestValidator.validAndRegularApi(api);

        for (APIHandleImpl handle : apiRepository) {
            if (api.getMethod().equals(handle.getApi().getMethod()) && api.getPattern().equals(handle.getApi().getPattern())) {
                throw API_ADD_FAILED.getException();
            }
        }

        registerRequestMapping(api, validator);

        return api;
    }


    private synchronized void registerRequestMapping(API api, RequestValidator validator) {

        PatternsRequestCondition patterns = new PatternsRequestCondition(api.getPattern());
        RequestMethodsRequestCondition methods = new RequestMethodsRequestCondition(
                RequestMethod.valueOf(api.getMethod().toUpperCase()));
        ParamsRequestCondition params = new ParamsRequestCondition(api.getParams());
        HeadersRequestCondition headers = new HeadersRequestCondition(api.getHeaders());
        ConsumesRequestCondition consumes = new ConsumesRequestCondition(api.getConsumes());
        ProducesRequestCondition produces = new ProducesRequestCondition(api.getConsumes());

        RequestMappingInfo mappingInfo = new RequestMappingInfo(
                api.getName(), patterns, methods, params, headers, consumes, produces, null);

        try {
            APIHandleImpl apiHandle = beanFactory.getBean(APIHandleImpl.class);
            apiHandle.setApi(api);
            apiHandle.setRequestMappingInfo(mappingInfo);
            apiHandle.setRequestValidator(validator);
            requestMappingHandlerMapping.registerMapping(mappingInfo, apiHandle, apiHandle.getHandleMethod());
            apiRepository.add(apiHandle);
        } catch (IllegalStateException e) {
            throw API_ADD_FAILED.getException(e);
        }
    }

    private synchronized void unregisterRequestMapping(APIHandleImpl handle) {
        try {
            requestMappingHandlerMapping.unregisterMapping(handle.getRequestMappingInfo());
            apiRepository.remove(handle);
        } catch (Throwable t) {
            throw API_UNREGISTER_FAILED.getException(t);
        }
    }

    private APIHandleImpl getAPIHandleByapiID(Integer apiID) {
        APIHandleImpl handle = null;
        for (int i = 0; i < apiRepository.size(); i++) {
            if (apiRepository.get(i).getApi().getId().equals(apiID)) {
                handle = apiRepository.get(i);
                break;
            }
        }
        return handle;
    }
}

package mockservice.configuration;

import mockservice.filter.LoginFilter;
import mockservice.filter.HttpAccessLogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean httpAccessLogFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new HttpAccessLogFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("HttpAccessLogFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean loginFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new LoginFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("LoginFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}

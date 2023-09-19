package online.superh.haro.spring.boot.resilience4j.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @version: 1.0
 * @author: haro
 * @description:
 *              熔断器演示Controller
 * @date: 2023-09-19 14:00
 */
@Slf4j
@RestController
@RequestMapping(value = "/demo/cb")
public class CircuitBreakerDemoController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/get_user")
    @CircuitBreaker(name = "cbA", fallbackMethod = "getUserFallback")
    public String getUser(@RequestParam("id") Integer id) {
        log.info("[getUser][准备调用 user-service 获取用户({})详情]", id);
        return restTemplate.getForEntity("http://127.0.0.1:18080/user/get?id=" + id, String.class).getBody();
    }

    //方法的参数要和原始方法一致，最后一个为 Throwable 异常
    public String getUserFallback(Integer id, Throwable throwable) {
        log.info("[getUserFallback][id({}) exception({})]", id, throwable.getClass().getSimpleName());
        return "mock:User:" + id;
    }

}

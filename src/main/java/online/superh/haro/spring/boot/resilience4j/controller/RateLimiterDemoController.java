package online.superh.haro.spring.boot.resilience4j.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version: 1.0
 * @author: haro
 * @description:
 *          限流器演示Controller
 * @date: 2023-09-19 14:11
 */
@Slf4j
@RestController
@RequestMapping("/demo/rl")
public class RateLimiterDemoController {


    @GetMapping("/get_user")
    //通过 name 属性，设置对应的 RateLimiter 实例名
    //fallbackMethod 方法的参数要和原始方法一致
    @RateLimiter(name = "rlA", fallbackMethod = "getUserFallback")
    public String getUser(@RequestParam("id") Integer id) {
        return "User:" + id;
    }

    public String getUserFallback(Integer id, Throwable throwable) {
        log.info("[getUserFallback][id({}) exception({})]", id, throwable.getClass().getSimpleName());
        return "mock:User:" + id;
    }

}

package online.superh.haro.spring.boot.resilience4j.controller;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @version: 1.0
 * @author: haro
 * @description:
 *      Resilience4j 提供了 TimeLimiter 组件，限制任务的执行时长，在超过时抛出异常
 * @date: 2023-09-19 15:39
 */
@Slf4j
@RestController
@RequestMapping(value = "/demo/tl")
public class TimeLimiterDemoController {

    @Autowired
    private TimeLimiterService timeLimiterService;

    @GetMapping("/get_user")
    public String getUser(@RequestParam("id") Integer id) throws ExecutionException, InterruptedException {
        return timeLimiterService.getUser0(id).get();
    }

    /*
        这里创建了 TimeLimiterService 的原因是，这里我们使用 Resilience4j 是基于注解 + AOP的方式，
     如果直接 this. 方式来调用方法，实际没有走代理，导致 Resilience4j 无法使用 AOP。
     */
    @Slf4j
    @Service
    public static class TimeLimiterService {
        /*
            因为 TimeLimiter 需要搭配线程池类型的 Bulkhead，
            所以这里添加了 resilience4j.thread-pool-bulkhead 配置项，因为 TimeLimiter 是基于线程池来实现超时限制的。
         */
        @Bulkhead(name = "bhB", type = Bulkhead.Type.THREADPOOL)
        @TimeLimiter(name = "tlA", fallbackMethod = "getUserFallback")
        public CompletableFuture<String> getUser0(Integer id) throws InterruptedException {
            log.info("[getUser][id({})]", id);
            Thread.sleep(10 * 1000L); // sleep 10 秒
            return CompletableFuture.completedFuture("User:" + id);
        }

        public CompletableFuture<String> getUserFallback(Integer id, Throwable throwable) {
            log.info("[getUserFallback][id({}) exception({})]", id, throwable.getClass().getSimpleName());
            return CompletableFuture.completedFuture("mock:User:" + id);
        }

    }

}

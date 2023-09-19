package online.superh.haro.spring.boot.resilience4j.controller;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
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
 *  在 Resilience4j 中，提供了基于 Semaphore 信号量和 ThreadPool 线程池两种 Bulkhead 实现，
 *  隔离不同种类的调用，并提供流控的能力，从而避免某类调用异常时而占用所有资源，导致影响整个系统。
 * @date: 2023-09-19 14:20
 */
@Slf4j
@RestController
@RequestMapping(value = "/demo/bh")
public class BulkheadDemoController {

    /*
        Semaphore包含一组许可证，
        如果需要，每个acquire()调用都会阻塞，直到有一个可用，然后拿走一个许可证；
        每个release()添加一个许可证，这可能会释放一个正在阻塞的acquire()调用。
        然而，没有实际的许可证对象，Semaphore只是维护了一个可获得许可证的计数
     */
    @GetMapping("/get_user")
    @Bulkhead(name = "backendC", fallbackMethod = "getUserFallback", type = Bulkhead.Type.SEMAPHORE)
    public String getUser(@RequestParam("id") Integer id) throws InterruptedException {
        log.info("[getUser][id({})]", id);
        Thread.sleep(10 * 1000L); // sleep 10 秒
        return "User:" + id;
    }

    public String getUserFallback(Integer id, Throwable throwable) {
        log.info("[getUserFallback][id({}) exception({})]", id, throwable.getClass().getSimpleName());
        return "mock:User:" + id;
    }


    @Autowired
    private ThreadPoolBulkheadService threadPoolBulkheadService;

    @GetMapping("/get_user2")
    public String getUser2(@RequestParam("id") Integer id) throws ExecutionException, InterruptedException {
        //串行执行
        threadPoolBulkheadService.getUser0(id);
        return threadPoolBulkheadService.getUser0(id).get();
    }

    /*
     这里我们使用 Resilience4j 是基于注解 + AOP的方式，
     如果直接 this. 方式来调用方法，实际没有走代理，导致 Resilience4j 无法使用 AOP。
     */
    @Slf4j
    @Service
    public static class ThreadPoolBulkheadService {
        
        @Bulkhead(name = "bhB", fallbackMethod = "getUserFallback", type = Bulkhead.Type.THREADPOOL)
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

package com.example.testgrpc1;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "service-b", url = "http://service-b:8080")
public interface TestClient {
    @GetMapping("/test2")
    String test222(@RequestParam("name") String name);
}

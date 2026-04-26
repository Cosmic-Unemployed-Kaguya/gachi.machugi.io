package com.example.testgrpc2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test2")
    public String test2(@RequestParam("name") String name){
        String podName = System.getenv("HOSTNAME");
        if (podName == null) podName = "Local-PC";
        String message = "안녕!!!!!" + name +" rest" +" from:" +podName;

        return message;
    }
}

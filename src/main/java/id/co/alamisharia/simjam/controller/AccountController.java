package id.co.alamisharia.simjam.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AccountController {

    @RequestMapping("/account")
    public Mono<String> base() {
        return Mono.create(sink -> sink.success("Account context"));
    }
}

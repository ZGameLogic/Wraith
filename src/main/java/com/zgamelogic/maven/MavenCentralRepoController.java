package com.zgamelogic.maven;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("mcr")
public class MavenCentralRepoController {

    @PostMapping
    public void test(@RequestBody String payload) {
        System.out.println(payload);
    }
}

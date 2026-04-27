package com.chatbot.core.license.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ActivationController {

    @GetMapping("/activate")
    public String activatePage(@RequestParam(required = false) String token) {
        return "forward:/activate.html";
    }

    @GetMapping("/")
    public String homePage() {
        return "forward:/activate.html";
    }
}

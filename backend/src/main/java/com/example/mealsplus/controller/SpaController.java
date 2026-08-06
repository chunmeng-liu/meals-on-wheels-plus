package com.example.mealsplus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping({"/login", "/senior", "/volunteer", "/admin", "/profile"})
    public String appRoutes() {
        return "forward:/index.html";
    }
}

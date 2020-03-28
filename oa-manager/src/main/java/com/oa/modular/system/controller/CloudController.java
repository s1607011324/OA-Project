package com.oa.modular.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cloud")
public class CloudController {
    private static String PREFIX = "/modular/system/cloud";

    @RequestMapping("")
    public String index(){
        return PREFIX + "/cloud.html";
    }
}

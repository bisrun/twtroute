package kr.stteam.TwtRoute.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TwtAdminController {
    //@RequestMapping(value = "/chat", method = RequestMethod.GET)
    @GetMapping("monitor")
    public String hello(Model model){
        model.addAttribute("data", "spring!!" );
        return "monitor";
    }

    @GetMapping("websocket/sample")
    public String sample(Model model){

        return "websocket_sample";
    }
}

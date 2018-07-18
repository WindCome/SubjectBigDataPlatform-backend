package com.example.ggkgl.Controller;


import com.example.ggkgl.Repository.EngineerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EngineerController {
    @Autowired
    EngineerRepository engineerRepository;

}

package com.example.ggkgl.Controller;

import com.example.ggkgl.Entity.MetaEntityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {
    @Autowired
    JpaRepository<MetaEntityEntity,Integer> jpaRepository;
    @GetMapping(value = "/hello")
    public List<MetaEntityEntity> say()
    {
        return jpaRepository.findAll();
    }

}

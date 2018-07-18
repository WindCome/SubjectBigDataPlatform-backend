package com.example.ggkgl.Controller;


import com.example.ggkgl.Entity.MetaEntityEntity;
import com.example.ggkgl.Repository.AcRepository;
import com.example.ggkgl.Repository.MDRepository;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class AllController {
    @Autowired
    MDRepository mdRepository;
    @GetMapping(value = "/getAll")
    public List<MetaEntityEntity> getAll(@RequestParam(value ="page",defaultValue = "-1") int page
            ,@RequestParam(value = "size",defaultValue = "-1") int size)
    {
        if(page==-1||size==-1)
            return mdRepository.findAll();
        int start=(page-1)*size;
        return mdRepository.getPageable(start,size);
    }
}

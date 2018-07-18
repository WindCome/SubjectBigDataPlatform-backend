package com.example.ggkgl.Controller;

import com.example.ggkgl.AssitClass.Change;
import com.example.ggkgl.Entity.AcademicianEntity;
import com.example.ggkgl.Mapper.GreatMapper;
import com.example.ggkgl.Repository.AcRepository;
import com.example.ggkgl.Repository.MDRepository;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;

@RestController
@CrossOrigin
public class AcademicianController {
    @Autowired
    AcRepository acRepository;

    @Autowired
    MDRepository mdRepository;

    @Autowired
    private GreatMapper greatMapper;

    @GetMapping(value = "/academician/getAll")
    public List<AcademicianEntity> getAllAc()
    {
        return acRepository.findAll();
    }

    @PostMapping(value = "/academician/add")
    public AcademicianEntity addAc(HttpServletRequest request)
    {
        AcademicianEntity academicianEntity=new AcademicianEntity();
        academicianEntity.setEvaluateTime(java.sql.Date.valueOf(request.getParameter("EvaluateTime")));
        academicianEntity.setExpertCategory(request.getParameter("ExpertCategory"));
        academicianEntity.setIntroduce(request.getParameter("Introduce"));
        academicianEntity.setName(request.getParameter("Name"));
        academicianEntity.setLink(request.getParameter("Link"));
        return acRepository.save(academicianEntity);
    }

    @Transactional
    @PostMapping(value = "/add/{tableId}")
    public Boolean add(@PathVariable("tableId") int tableId,@RequestBody JSONObject jsonObject)
    {
        List<String> listKeys=new ArrayList<>();
        List<String> listValues=new ArrayList<>();
        String tableName="";
        try {
             tableName=mdRepository.getOne(tableId).getName();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        Iterator iterator=jsonObject.keys();
        while(iterator.hasNext())
        {
            String key=(String)iterator.next();
//            System.out.println(key);
            listKeys.add(key);
            listValues.add(jsonObject.getString(key));
//            System.out.println(jsonObject.getString(key));
        }
        Map<String,Object> params=new HashMap<String,Object>(2) ;
        params.put("tableName",tableName);
        params.put("keys",listKeys);
        params.put("attributes",listValues);
        try {
            greatMapper.insert(params);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @GetMapping(value ="/display/{tableId}")
    public List<HashMap> show(@PathVariable("tableId") int tableId
            ,@RequestParam(value = "page") int page,@RequestParam(value = "size") int size)
    {
        String tableName=mdRepository.getOne(tableId).getName();
        int start=(page-1)*size;
        return greatMapper.desc(tableName,start,size);
    }

   @PostMapping(value = "/update/{tableId}")
    public Boolean update(@PathVariable("tableId") int tableId,@RequestParam(value = "Id") String Id
           ,@RequestBody JSONObject jsonObject)
   {
       String tableName="";
       try {
           tableName=mdRepository.getOne(tableId).getName();
       }
       catch (Exception e)
       {
           e.printStackTrace();
           return false;
       }
       Iterator iterator=jsonObject.keys();
       List<Change> changeList=new ArrayList<>();
       while(iterator.hasNext())
       {
           Change change=new Change();
           String key=(String)iterator.next();
           String value=jsonObject.getString(key);
           change.setKey(key);
           change.setValue(value);
           changeList.add(change);
       }
       Map<String,Object> params=new HashMap<>();
       params.put("tableName",tableName);
       params.put("Id",Id);
       params.put("changeList",changeList);
       try {
           greatMapper.update(params);
       }
       catch (Exception e)
       {
           e.printStackTrace();
           return false;
       }
       return true;
   }

    @PostMapping(value = "/academician/update")
    public AcademicianEntity acUpdate(@RequestParam(value = "acid") int Id,HttpServletRequest request)
    {
        AcademicianEntity academicianEntity=acRepository.getOne(Id);
        academicianEntity.setEvaluateTime(java.sql.Date.valueOf(request.getParameter("EvaluateTime")));
        academicianEntity.setExpertCategory(request.getParameter("ExpertCategory"));
        academicianEntity.setIntroduce(request.getParameter("Introduce"));
        academicianEntity.setName(request.getParameter("Name"));
        academicianEntity.setLink(request.getParameter("Link"));
        return acRepository.save(academicianEntity);
    }
    @GetMapping(value = "/delete/{tableId}")
    public Boolean delete(@RequestParam("Id") String Id,@PathVariable("tableId") int tableId)
    {
        String tableName="";
        try {
            tableName=mdRepository.getOne(tableId).getName();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        try {
            greatMapper.delete(tableName,Id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @GetMapping(value = "/academician/delete")
    public void deleteAc(@RequestParam("acid")int Id)
    {
        acRepository.deleteById(Id);
    }

    @GetMapping(value = "/academician/search")
    public List<AcademicianEntity> searchAc(@RequestParam("name") String name)
    {
       return acRepository.findAcademicianEntitiesByNameContaining(name);
    }








}

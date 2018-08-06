package com.example.ggkgl.Controller;

import com.example.ggkgl.AssitClass.Change;
import com.example.ggkgl.Entity.AcademicianEntity;
import com.example.ggkgl.Mapper.GreatMapper;
import com.example.ggkgl.Repository.AcRepository;
import com.example.ggkgl.Repository.MDRepository;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
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

    public int getSize(int tableId)
    {
        String tableName=getTableName(tableId);
        return greatMapper.getSize(tableName);
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

    @PostMapping(value = "/add/{tableId}")
    public Boolean add(@PathVariable("tableId") int tableId,@RequestBody JSONObject jsonObject
            ,@RequestParam("flag") int flag) {
        List<String> listKeys = new ArrayList<>();
        List<String> listValues = new ArrayList<>();
        String tableName = "";
        try {
            tableName = getTableName(tableId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
//            System.out.println(key);
            listKeys.add(key);
            listValues.add(jsonObject.getString(key));
//            System.out.println(jsonObject.getString(key));
        }
        if(flag==2) {
            listKeys.add("ID");
            listValues.add(UUID.randomUUID().toString());
            listKeys.add("SEQ_NO");
            Integer seqNo=getSize(tableId)+1;
            listValues.add(seqNo.toString());
        }
        listKeys.add("MODIFY_TIME");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        listValues.add(df.format(System.currentTimeMillis()));
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("tableName", tableName);
        params.put("keys", listKeys);
        params.put("attributes", listValues);
        try {
            greatMapper.insert(params);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @GetMapping(value = "/getConfig/{tableId}")
    public HashMap displayConfig(@PathVariable("tableId") int tableId)
    {
        String tableName=getTableName(tableId);
        String chineseName=mdRepository.getOne(tableId).getChineseName();
        String configJson=greatMapper.getDesc(tableName);
        JSONObject jsonObject=JSONObject.fromObject(configJson);
        HashMap map=new HashMap();
        map.put("totalSize",getSize(tableId));
        map.put("all",jsonObject.get("all").toString());
        map.put("show",jsonObject.get("show").toString());
        map.put("pageName",chineseName);
        map.put("index",jsonObject.get("index").toString());
        map.put("upgrade",jsonObject.get("upgrade").toString());
        return map;
    }

    @PostMapping(value = "/setConfig/{tableId}")
    public Boolean setConfig(HttpServletRequest request,@PathVariable("tableId")int tableId)
    {
        String tableName=getTableName(tableId);
        String source=request.getParameter("source");
        String year=request.getParameter("year");
        JSONObject allJson=JSONObject.fromObject(greatMapper.getDesc(tableName));
        JSONObject jsonObject=allJson.getJSONObject("upgrade");
        JSONObject sourceObject=jsonObject.getJSONObject("source");
        JSONObject yearObject=jsonObject.getJSONObject("year");
        sourceObject.put("value",source);
        yearObject.put("value",year);
        try {
            greatMapper.updateDesc(tableName+"_upgrade", jsonObject.toString());
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
            ,@RequestParam(value = "page") int page,@RequestParam(value = "size") int size
            ,@RequestParam(value ="flag") int flag)
    {
        String orderFactor="";
        if(flag==1)
            orderFactor="evaluate_time";
        else
            orderFactor="PDSJ";
        String tableName=getTableName(tableId);
        String chineseName=mdRepository.getOne(tableId).getChineseName();
        int start=(page-1)*size;
        List<HashMap> hashMaps=greatMapper.desc(tableName,start,size,orderFactor);
        return hashMaps;
    }

    @PostMapping(value = "/freeSearch/{tableId}")
    public List<HashMap> freeSearch(@RequestBody JSONObject jsonObject,@PathVariable("tableId") int tableId
                                    ,@RequestParam(value="page",defaultValue = "-1") int page,
                                    @RequestParam(value = "size",defaultValue = "-1") int size)
    {
        String tableName=getTableName(tableId);
        List<Change> changes=new ArrayList<>();
        for(Object key:jsonObject.keySet())
        {
            Change change=new Change();
            change.setKey(key.toString());
            change.setValue(jsonObject.get(key).toString());
            changes.add(change);
        }
        HashMap map=new HashMap();
        map.put("conditions",changes);
        map.put("tableName",tableName);
        List<HashMap> resultMaps=new ArrayList<>();
        List<HashMap> searchMaps=greatMapper.comboSearch(map);
        HashMap hashMap=new HashMap();
        hashMap.put("totalSize",searchMaps.size());
        if(page==-1||size==-1)
        {
            searchMaps.add(hashMap);
            return searchMaps;
        }
        else
        {
            int start=(page-1)*size;
            int end=start+size;
            for(int j=start;j<searchMaps.size()&&j<end;j++)
            {
                resultMaps.add(searchMaps.get(j));
            }
            resultMaps.add(hashMap);
            return resultMaps;
        }
    }
    @GetMapping(value = "/getTableName/{tableId}")
    public String getTableName(@PathVariable("tableId") int tableId)
    {
        return greatMapper.freeSearch("META_ENTITY","name",""+tableId+"");
    }
   @PostMapping(value = "/update/{tableId}")
    public Boolean update(@PathVariable("tableId") int tableId,@RequestParam(value = "Id") String Id
           ,@RequestParam("flag") int flag, @RequestBody JSONObject jsonObject)
   {
       String tableName="";
       try {
           tableName=getTableName(tableId);
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
           Change change=new Change();
           change.setKey("MODIFY_TIME");
           SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           change.setValue(df.format(System.currentTimeMillis()));
           changeList.add(change);
       Map<String,Object> params=new HashMap<>();
       params.put("tableName",tableName);
       if(flag==1)
       params.put("Id",Id);
       else params.put("Id","'"+Id+"'");
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
    public Boolean delete(@RequestParam("Id") String Id,@PathVariable("tableId") int tableId
            ,@RequestParam("flag") int flag)
    {
        String tableName="";
        try {
            tableName=getTableName(tableId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        String index;
        if(flag==1)
        {
             index=Id;
        }
        else
            index="'"+Id+"'";
        try {
            greatMapper.delete(tableName,index);
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

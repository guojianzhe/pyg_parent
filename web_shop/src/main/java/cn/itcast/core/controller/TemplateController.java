package cn.itcast.core.controller;


import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TemplateController {

    @Reference
    private TypeTemplateService typeTemplateService;


    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id){


        return  typeTemplateService.findOne(id);

    }

    @RequestMapping("/findBySpecList")
    public List<Map> findBySpecList(Long id){

        return typeTemplateService.findBySpecList(id);
    }


}

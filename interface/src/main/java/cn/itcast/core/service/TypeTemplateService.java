package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

public interface TypeTemplateService {
    PageResult findPage(TypeTemplate typeTemplate, Integer page, Integer rows);
}

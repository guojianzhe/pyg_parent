package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService {


    PageResult findPage(Specification specification, Integer page, Integer rows);

    void add(SpecEntity specEntity);

    SpecEntity findOne(long id);

    void update(SpecEntity specEntity);

    void delete(long[] ids);

    List<Map<Integer,String>> selectOptionList();
}

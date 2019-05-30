package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {
    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Autowired
    private SpecificationOptionDao optionDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult findPage(TypeTemplate typeTemplate, Integer page, Integer rows) {
        //在查询的时候将模板放入redis中  这里为了简便  实际上应该在增删改的时候放到redis中
        /**
         * redis中缓存模板所有数据
         */
        List<TypeTemplate> templatesAll = typeTemplateDao.selectByExample(null);

        for (TypeTemplate template : templatesAll) {
            //模板id作为key,品牌集合作为value缓存到redis中
            String brandIds = template.getBrandIds();
            //将json转换为集合
            List<Map> brandList = JSON.parseArray(brandIds, Map.class);
            redisTemplate.boundHashOps(Constants.BRAND_LIST_REDIS).put(template.getId(),brandList);
            System.out.println("品牌缓存到redis");
            //模板id作为key,规格集合作为value缓存入redis中

            List<Map> specList = findBySpecList(template.getId());

            redisTemplate.boundHashOps(Constants.SPEC_LIST_REDIS).put(template.getId(),specList);

            System.out.println("模板规格缓存到redis");
        }


        /**
         * 模板分页查询
         */
        PageHelper.startPage(page,rows);

        TypeTemplateQuery query = new TypeTemplateQuery();


        if(typeTemplate.getName()!=null &&!"".equals(typeTemplate.getName())){
            query.createCriteria().andNameLike("%"+typeTemplate.getName()+"%");
        }


        Page<TypeTemplate> typeTemplateList = (Page<TypeTemplate>) typeTemplateDao.selectByExample(query);


        return new PageResult(typeTemplateList.getTotal(),typeTemplateList.getResult());
    }

    @Override
    public void add(TypeTemplate typeTemplate) {


        typeTemplateDao.insertSelective(typeTemplate);

    }

    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public void delete(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }
        }


    }

    @Override
    public List<Map> findBySpecList(Long id) {
        //1.根据模板id查询模板对象
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);

        //2.从模板对象中获取规格集合数据,获取到的是json格式字符串
        String specIds = typeTemplate.getSpecIds();

        //3.将json格式的字符串通过FastJson 解析成java中的List集合对象
        List<Map> maps = JSON.parseArray(specIds, Map.class);

        if(maps!=null){
            for (Map map : maps) {
                Long specId = Long.valueOf(String.valueOf(map.get("id")));

                SpecificationOptionQuery query = new SpecificationOptionQuery();

                query.createCriteria().andSpecIdEqualTo(specId);
                List<SpecificationOption> options = optionDao.selectByExample(query);

                map.put("options",options);
            }
        }
        return maps;
    }
}

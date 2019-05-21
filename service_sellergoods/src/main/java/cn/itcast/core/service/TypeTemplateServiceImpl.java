package cn.itcast.core.service;

import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {
    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Override
    public PageResult findPage(TypeTemplate typeTemplate, Integer page, Integer rows) {

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
}

package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Resource
    private SpecificationDao specificationDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;

    /**
     * 分页查询
     * @param specification
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult findPage(Specification specification, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);

        SpecificationQuery specificationQuery = new SpecificationQuery();

        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();


        if(specification!=null){
            if(specification.getSpecName()!=null&&!"".equals(specification.getSpecName())){
                criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
            }

        }

        Page<Specification> specificationList = (Page<Specification>) specificationDao.selectByExample(specificationQuery);

        return new PageResult(specificationList.getTotal(),specificationList.getResult());
    }

    @Override
    @Transactional
    public void add(SpecEntity specEntity) {
        //1.添加规格对象
        specificationDao.insertSelective(specEntity.getSpecification());
        //2.添加规格选项对象
        if(specEntity.getSpecificationOptionList()!=null){
            for (SpecificationOption option : specEntity.getSpecificationOptionList()) {
                //在mybatis中设置的<insert id="insertSelective" parameterType="cn.itcast.core.pojo.specification.Specification"
                //  useGeneratedKeys="true" keyProperty="id">  其中useGeneratedKeys="true" keyProperty="id"
                // 将specEntity.getSpecification()中的id主键返回
                option.setSpecId(specEntity.getSpecification().getId());
                specificationOptionDao.insertSelective(option);
            }

        }



    }

    @Override
    public SpecEntity findOne(long id) {


        //1.查询出规格
        Specification specification = specificationDao.selectByPrimaryKey(id);


        //2.查询出规格选项
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(id);


        List<SpecificationOption> options = specificationOptionDao.selectByExample(query);
        //将规格选项封装到返回的实体对象中
        SpecEntity specEntity = new SpecEntity();
        specEntity.setSpecification(specification);
        specEntity.setSpecificationOptionList(options);

        return specEntity;
    }

    @Override
    @Transactional
    public void update(SpecEntity specEntity) {
        //先将查询出来的规格选项全部删除

        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(specEntity.getSpecification().getId());

        specificationOptionDao.deleteByExample(query);

        specificationDao.updateByPrimaryKeySelective(specEntity.getSpecification());
        if(specEntity.getSpecificationOptionList()!=null){
            for (SpecificationOption option : specEntity.getSpecificationOptionList()) {
                //设置选项对象外键
                option.setSpecId(specEntity.getSpecification().getId());
                specificationOptionDao.insertSelective(option);
            }
        }


    }

    @Override
    @Transactional
    public void delete(long[] ids) {
        if(ids!=null){
            for (long id : ids) {
                //首先删除规格

                specificationDao.deleteByPrimaryKey(id);

                //2删除规格选项
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                query.createCriteria().andSpecIdEqualTo(id);

                specificationOptionDao.deleteByExample(query);
            }
        }

    }
}

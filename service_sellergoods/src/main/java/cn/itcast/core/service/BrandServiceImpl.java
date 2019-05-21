package cn.itcast.core.service;


import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDao brandDao;

    @Override
    public List<Brand> findAll() {

        return brandDao.selectByExample(null);
    }

    @Override
    public PageResult findPage(Brand brand, Integer page, Integer rows) {
        //使用mybatis的分页助手,实现分页,参数1.当前页,参数2,每页展示数据条数
        PageHelper.startPage(page,rows);

        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if(brand!=null){
            if(brand.getName()!=null&&!"".equals(brand.getName())){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if(brand.getFirstChar()!=null&&!"".equals(brand.getFirstChar())){
                criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
            }

        }
        Page<Brand> brandList = (Page<Brand>)brandDao.selectByExample(brandQuery);

        return new PageResult(brandList.getTotal(),brandList.getResult());
    }

    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    @Override
    public void delete(long[] ids) {
        if(ids!=null){
            for (long id : ids) {
                brandDao.deleteByPrimaryKey(id);
            }
        }


    }

    @Override
    public List<Map> selectOptionList() {
        return brandDao.selectOptionList();
    }


}

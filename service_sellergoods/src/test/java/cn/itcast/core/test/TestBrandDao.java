package cn.itcast.core.test;


import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext*.xml"})
public class TestBrandDao {

    @Autowired
    private BrandDao brandDao;

    @Test
    public void testFindBrandById(){

        Brand brand = brandDao.selectByPrimaryKey(1L);

        System.out.println("===="+brand);


    }

    @Test
    public void testFindBrandAll(){

        List<Brand> brands = brandDao.selectByExample(null);
        for (Brand brand : brands) {
            System.out.println("===="+brand);
        }

    }

    @Test
    public void testFindBrandByWhere(){

        //创建查询对象
        BrandQuery brandQuery = new BrandQuery();
        //设置查询的字段名,如果不写默认是* 查询所有
        brandQuery.setFields("id,name");
        //不设置默认是false不去重
        brandQuery.setDistinct(true);
        //设置排序

//        brandQuery.setOrderByClause("id desc");
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        criteria.andIdEqualTo(1L);

        //List<Brand> brands = brandDao.selectByExample(brandQuery);
        List<Brand> brands = brandDao.selectByExample(brandQuery);
        for (Brand brand : brands) {
            System.out.println("===="+brand);
        }

    }
}

package cn.itcast.core.test;


import cn.itcast.core.dao.specification.SpecificationDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext*.xml"})
public class TestSpec {

    @Resource
    SpecificationDao specificationDao;

    @Test
    public void TestSelectOptionList(){

        List<Map<Integer, String>> maps = specificationDao.selectOptionList();

        for (Map<Integer, String> map : maps) {
            System.out.println(map);
        }


    }

}

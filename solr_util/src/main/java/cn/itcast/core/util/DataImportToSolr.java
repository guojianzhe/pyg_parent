package cn.itcast.core.util;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;
import sun.applet.Main;

import java.util.List;
import java.util.Map;

@Component
public class DataImportToSolr {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private ItemDao itemDao;


    public void importItemDataToSolr(){
        ItemQuery query = new ItemQuery();

        ItemQuery.Criteria criteria = query.createCriteria();

        criteria.andStatusEqualTo("1");

        List<Item> items = itemDao.selectByExample(query);
        if(items!=null){
            for (Item item : items) {
                //获取json格式的字符创
                String specJsonStr = item.getSpec();
                Map map = JSON.parseObject(specJsonStr, Map.class);
                item.setSpecMap(map);

            }


            //保存
            solrTemplate.saveBeans(items);
            //提交
            solrTemplate.commit();

        }


    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");

        DataImportToSolr bean = (DataImportToSolr) context.getBean("dataImportToSolr");
        bean.importItemDataToSolr();


    }



}

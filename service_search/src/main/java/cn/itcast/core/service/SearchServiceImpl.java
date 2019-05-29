package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map paramMap) {

        //1.根据关键字,分页,高亮,过滤,排序查询
        Map<String, Object> stringObjectMap = highlightSearch(paramMap);
        //2.根据查询参数,获取对应的分类结果集,由于分类重复,所以需要分组去重



        return stringObjectMap;
    }

    /**
     * 根据关键字,分页,高亮,过滤,排序查询,并且将查询结果返回
     * @param paramMap
     * @return
     */
    private Map<String,Object> highlightSearch(Map paramMap){
        //获取查询关键字
        String keywords = String.valueOf(paramMap.get("keywords"));
        //当前页面
        Integer pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
        //每页查询多少条数据
        Integer pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));

        //创建查询对象  普通查询对象
        //Query query = new SimpleQuery();

        HighlightQuery query = new SimpleHighlightQuery();

        Criteria criteria = new Criteria("item_keywords").is(keywords);

        //
        if(pageNo == null || pageNo<=0){
            pageNo=1;
        }

        //将查询条件放入查询对象中
        query.addCriteria(criteria);
        //计算从第几条开始查询
        Integer start = (pageNo-1)* pageSize;
        //设置从第几条开始查询
        query.setOffset(start);
        //设置每页查询多少条数据
        query.setRows(pageSize);

        //创建高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置那个域需要高亮显示
        highlightOptions.addField("item_title");
        //设置高翔前缀
        highlightOptions.setSimplePrefix("<em style=\"color:red\">");
        //设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //将高亮选项加入到查询对象中
        query.setHighlightOptions(highlightOptions);


        //普通查询
        //ScoredPage<Item> items = solrTemplate.queryForPage(query, Item.class);

        HighlightPage<Item> items = solrTemplate.queryForHighlightPage(query, Item.class);

        //获取带高亮的集合
        List<HighlightEntry<Item>> highlighted = items.getHighlighted();

        List<Item> itemList = new ArrayList<>();

        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            //获取到不带高亮的实体类对象
            Item item = itemHighlightEntry.getEntity();

            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
            if(highlights!=null && highlights.size()>0){
                List<String> highlightTitle = highlights.get(0).getSnipplets();
                if(highlightTitle!=null && highlightTitle.size()>0){
                    //获取到高亮的title标题
                    String title = highlightTitle.get(0);
                    item.setTitle(title);
                }
            }
            itemList.add(item);
        }


        Map<String,Object> map = new HashMap<>();
        //查询到的结果集
        map.put("rows",itemList);
        //查询到的总页数
        map.put("total",items.getTotalElements());
        //查询到的总条数
        map.put("totalPages",items.getTotalPages());


        return map;
    }
}

package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    SolrTemplate solrTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map paramMap) {

        //1.根据关键字,到solr中分页,高亮,过滤,排序查询
        Map<String, Object> resultMap = highlightSearch(paramMap);
        //2.根据查询参数,获取对应的分类结果集,由于分类重复,所以需要分组去重
        List<String> groupCategory = findGroupCategory(paramMap);

        resultMap.put("categoryList",groupCategory);
        //3.判断paramMap传入参数中是否有分类名称,
        String category = String.valueOf(paramMap.get("category"));
        if(category!=null&&!"".equals(category)){
            //5.如果有分类参数,则根据分类查询对应的品牌集合和规格集合
            Map categoryAndBrandList = findCategoryAndBrandList(category);
            resultMap.putAll(categoryAndBrandList);
        }else{

            //4.如没有默认根据第一个分类查询对应的品牌集合个规格集合
            Map categoryAndBrandList = findCategoryAndBrandList(groupCategory.get(0));
            resultMap.putAll(categoryAndBrandList);
        }


        return resultMap;
    }

    /**
     * 根据关键字,分页,高亮,过滤,排序查询,并且将查询结果返回
     * @param paramMap
     * @return
     */
    private Map<String,Object> highlightSearch(Map paramMap){
        //获取查询关键字
        String keywords = String.valueOf(paramMap.get("keywords"));
        if(keywords!=null){
            keywords = keywords.replaceAll(" ","");
        }
        //当前页面
        Integer pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
        //每页查询多少条数据
        Integer pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));
        //获取页面点击的分类过滤条件
        String category = String.valueOf(paramMap.get("category"));
        //获取页面点击的品牌过滤条件
        String brand = String.valueOf(paramMap.get("brand"));
        //获取页面点击的规格过滤条件
        String specJsonStr = String.valueOf(paramMap.get("spec"));

        //获取页面点击的价格区间过滤条件
        String price = String.valueOf(paramMap.get("price"));

        //获取页面传入的排序的域
        String sortField = String.valueOf(paramMap.get("sortField"));

        //获取页面传入的排序的方式
        String sortType = String.valueOf(paramMap.get("sort"));


        //创建查询对象  普通查询对象
        //Query query = new SimpleQuery();

        //创建查询对象  高亮查询
        HighlightQuery query = new SimpleHighlightQuery();
        //创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(keywords);


        //将查询条件放入查询对象中
        query.addCriteria(criteria);

        if(pageNo == null || pageNo<=0){
            pageNo=1;
        }

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

        /**
         * 过滤查询
         */
        //根据分类过滤查询
        if(category!=null&&!"".equals(category)){
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFacetQuery();
            //创建条件对象
            Criteria filterCriteria1 = new Criteria("item_category").is(category);
            //将条件对象放入到过滤对象中
            filterQuery.addCriteria(filterCriteria1);
            //过滤对象放入到查询对象中
            query.addFilterQuery(filterQuery);

        }
        //根据品牌过滤查询
        if(brand!=null&&!"".equals(brand)){
            FilterQuery filterQuery = new SimpleFacetQuery();
            Criteria filterCriteria1 = new Criteria("item_brand").is(brand);
            filterQuery.addCriteria(filterCriteria1);
            //过滤对象放入到查询对象中
            query.addFilterQuery(filterQuery);
        }
        //根据规格过滤查询
        if(specJsonStr!=null&&!"".equals(specJsonStr)){
            Map<String,String> maps = JSON.parseObject(specJsonStr, Map.class);
            if(maps!=null && maps.size()>0){
                Set<Map.Entry<String, String>> entries = maps.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    //创建过滤查询对象
                    FilterQuery filterQuery = new SimpleFacetQuery();
                    //创建条件对象
                    Criteria filterCriteria1 = new Criteria("item_spec_"+entry.getKey()).is(entry.getValue());
                    //将条件对象放入到过滤对象中
                    filterQuery.addCriteria(filterCriteria1);
                    //过滤对象放入到查询对象中
                    query.addFilterQuery(filterQuery);
                }

            }

        }
        //根据价格过滤查询  0-500 500-1000 1000-1500
        if(price!=null && !"".equals(price)){
            //切分价格,这个数组中有最小值和最大值
            String[] split = price.split("-");
            if(split!=null&&split.length==2){
                //说明最小值是0
                if(!"0".equals(split[0])){
                    FilterQuery filterQuery = new SimpleFacetQuery();
                    //创建条件对象
                    Criteria filterCriteria1 = new Criteria("item_price").greaterThanEqual(split[0]);
                    //将条件对象放入到过滤对象中
                    filterQuery.addCriteria(filterCriteria1);
                    //过滤对象放入到查询对象中
                    query.addFilterQuery(filterQuery);
                }

                if(!"*".equals(split[1])){
                    FilterQuery filterQuery = new SimpleFacetQuery();
                    //创建条件对象
                    Criteria filterCriteria1 = new Criteria("item_price").lessThanEqual(split[1]);
                    //将条件对象放入到过滤对象中
                    filterQuery.addCriteria(filterCriteria1);
                    //过滤对象放入到查询对象中
                    query.addFilterQuery(filterQuery);
                }
            }


        }

        //添加排序条件

        if(sortField!=null && sortType!=null && !"".equals(sortField)&& !"".equals(sortType)){
            //升序排序
            if("ASC".equals(sortType)){
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                //将排序对象放入到查询对象中
                query.addSort(sort);
            }
            //降序排序
            if("DESC".equals(sortType)){
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }

            //降序排序




        }




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

    /**
     * 根据关键字到solr中查询结果集中对应的分类集合,要分组去重
     * @param paramMap 页面传入的查询参数
     * @return
     */
    private List<String> findGroupCategory(Map paramMap){
        List<String> stringList =new ArrayList<>();

        String keywords = String.valueOf(paramMap.get("keywords"));

        if(keywords!=null){
            keywords = keywords.replaceAll(" ","");
        }

        //创建查询对象
        Query query = new SimpleQuery();
        //创建查询条件对象  is 根据切分词  查询
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //将查询条件放入查询对象中
        query.addCriteria(criteria);

        //创建分组对象
        GroupOptions groupOptions = new GroupOptions();
        //设置根据分类域进行分组
        groupOptions.addGroupByField("item_category");
        //设置将分组对象放入查询对象中
        query.setGroupOptions(groupOptions);

        //分组查询分类集合
        GroupPage<Item> items = solrTemplate.queryForGroupPage(query, Item.class);
        //获取结果集中分类域的集合
        GroupResult<Item> item_category = items.getGroupResult("item_category");

        //获取分类域的实体集合
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();

        //遍历实体集合得到实体对象
        for (GroupEntry<Item> groupEntry : groupEntries) {

            String groupValue = groupEntry.getGroupValue();

            stringList.add(groupValue);
        }


        return stringList;
    }

    /**
     * 根据分类名称查询对应的品牌集合和规格集合
     * @param categoryName 分类名称
     * @return
     */
    private Map findCategoryAndBrandList(String categoryName){

        //1.根据分类名称到redis中查询对应的模板id
        Long templateId = (Long) redisTemplate.boundHashOps(Constants.CATEGORY_LIST_REDIS).get(categoryName);
        //2.根据模板id到redis中查询对应的品牌集合
        List<Map> brandList= (List<Map>)redisTemplate.boundHashOps(Constants.BRAND_LIST_REDIS).get(templateId);
        //3.根据模板id到redis中查询对应的规格集合
        List<Map> specList= (List<Map>)redisTemplate.boundHashOps(Constants.SPEC_LIST_REDIS).get(templateId);
        //4.将品牌集合和规格集合数据封装到Map中返回

        Map resultMap = new HashMap();

        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);

        return resultMap;
    }


}

package cn.itcast.core.controller;


import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodService;
import cn.itcast.core.service.SolrManagerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodService goodService;

    @Reference
    private SolrManagerService solrManagerService;

    @RequestMapping("/search")
    public PageResult search(@RequestBody Goods goods,Integer page,Integer rows){
        //获取当前登录的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(username);
        return goodService.findPage(goods,page,rows);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody GoodsEntity goodsEntity){
        try {

        //获取登录用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        goodsEntity.getGoods().setSellerId(name);

            goodService.add(goodsEntity);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }
    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id){

        return goodService.findOne(id);

    }
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsEntity goodsEntity){


        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //获取商品的所有者
            String sellerId = goodsEntity.getGoods().getSellerId();
            if(!username.equals(sellerId)){
                return new Result(false,"你没有权限修改此商品!");
            }
            goodService.update(goodsEntity);

            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }

    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            if(ids!=null) {
                for (Long id : ids) {
                    //1.根据商品id到数据库中删除

                    goodService.delete(id);

                    //2.根据商品id到solr索引库中删除对应数据

                    solrManagerService.deleteItemFromSolr(id);
                }
            }
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

}

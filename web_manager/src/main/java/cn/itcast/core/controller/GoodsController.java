package cn.itcast.core.controller;


import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodService;
import cn.itcast.core.service.SolrManagerService;
import com.alibaba.dubbo.config.annotation.Reference;
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


    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id){

        return goodService.findOne(id);

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

    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){

        try {
            if(ids!=null){
                for (Long id : ids) {
                    //1.导数据库中根据商品id改变商品的商家状态
                    goodService.updateStatus(id,status);
                    //2.对于审核通过的商品,将根据商品id获取库存数据,放入solr索引库中供搜索使用
                    if("1".equals(status)){
                        solrManagerService.saveItemToSolr(id);
                    }


                }
            }


            return new Result(true,"状态修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"状态修改失败!");
        }

    }

}

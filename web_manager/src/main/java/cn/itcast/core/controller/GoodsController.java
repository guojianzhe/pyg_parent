package cn.itcast.core.controller;


import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodService;
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

            goodService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){

        try {

            goodService.updateStatus(ids,status);
            return new Result(true,"状态修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"状态修改失败!");
        }

    }

}

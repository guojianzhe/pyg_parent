package cn.itcast.core.controller;


import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;


    @RequestMapping("/findAll")
    public List<Brand> findAll(){

        List<Brand> brandList = brandService.findAll();

        return brandList;

    }

    /**
     * 分页查询
     * @param page  当前页
     * @param rows  每页显示数据条数
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(Integer page, Integer rows){

        PageResult resouse = brandService.findPage(null, page, rows);
        return resouse;

    }

    /**
     * 添加品牌
     * @param brand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand){


        try {
            brandService.add(brand);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }


    }
    /**
     * 查询单个
     */

    @RequestMapping("/findOne")
    public Brand findOne(Long id){

        return brandService.findOne(id);

    }

    /**
     * 修改
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand){

        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }

    }

    @RequestMapping("/delete")
    public Result delete(long[] ids){

        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }

    }

    /**
     * 搜索
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody Brand brand,Integer page,Integer rows){


        return brandService.findPage(brand, page, rows);
    }

    /**
     * 获取模板下拉数据
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map>  selectOptionList(){

        return brandService.selectOptionList();

    }


}

package cn.itcast.core.controller;


import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.CategoryService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contentCategory")
public class CategoryController {

    @Reference
    private CategoryService categoryService;


    @RequestMapping("/findAll")
    public List<ContentCategory> findAll(){

        List<ContentCategory> categoryList = categoryService.findAll();

        return categoryList;

    }

    /**
     * 添加品牌
     * @param category
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody ContentCategory category){


        try {
            categoryService.add(category);
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
    public ContentCategory findOne(Long id){

        return categoryService.findOne(id);

    }

    /**
     * 修改
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ContentCategory category){

        try {
            categoryService.update(category);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }

    }

    @RequestMapping("/delete")
    public Result delete(long[] ids){

        try {
            categoryService.delete(ids);
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
    public PageResult search(@RequestBody ContentCategory category,Integer page,Integer rows){


        return categoryService.findPage(category, page, rows);
    }


}

package cn.itcast.core.controller;


import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference
    private ContentService contentService;


    @RequestMapping("/findAll")
    public List<Content> findAll(){
        
        List<Content> contentList = contentService.findAll();

        return contentList;

    }

    /**
     * 添加品牌
     * @param content
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Content content){


        try {
            contentService.add(content);
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
    public Content findOne(Long id){

        return contentService.findOne(id);

    }

    /**
     * 修改
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Content content){

        try {
            contentService.update(content);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }

    }

    @RequestMapping("/delete")
    public Result delete(long[] ids){

        try {
            contentService.delete(ids);
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
    public PageResult search(@RequestBody Content content,Integer page,Integer rows){


        return contentService.findPage(content, page, rows);
    }


}

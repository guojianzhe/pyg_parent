package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentCategoryDao;
import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.ad.ContentCategoryQuery;
import cn.itcast.core.pojo.entity.PageResult;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    ContentCategoryDao categoryDao;

    @Override
    public List<ContentCategory> findAll() {

        return categoryDao.selectByExample(null);
    }

    @Override
    public PageResult findPage(ContentCategory contentCategory, Integer page, Integer rows) {

        PageHelper.startPage(page,rows);

        ContentCategoryQuery query = new ContentCategoryQuery();
        if(contentCategory!=null){
            if(contentCategory.getName()!=null&&!"".equals(contentCategory.getName())){
                query.createCriteria().andNameLike("%"+contentCategory.getName()+"%");
            }
        }



        Page<ContentCategory> categoryPage = (Page<ContentCategory>) categoryDao.selectByExample(query);

        return new PageResult(categoryPage.getTotal(),categoryPage.getResult());
    }

    @Override
    public void add(ContentCategory contentCategory) {
        categoryDao.insertSelective(contentCategory);
    }

    @Override
    public ContentCategory findOne(Long id) {

        return categoryDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(ContentCategory contentCategory) {
        categoryDao.updateByPrimaryKeySelective(contentCategory);
    }

    @Override
    public void delete(long[] ids) {
        if(ids!=null){
            for (long id : ids) {
                categoryDao.deleteByPrimaryKey(id);
            }
        }
    }

}


package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {
    @Autowired
    ContentDao contentDao;


    @Override
    public List<Content> findAll() {
        return contentDao.selectByExample(null);
    }

    @Override
    public void add(Content content) {
        contentDao.insertSelective(content);
    }

    @Override
    public Content findOne(Long id) {

        return contentDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Content content) {
        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public void delete(long[] ids) {
        if(ids!=null){
            for (long id : ids) {
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public PageResult findPage(Content content, Integer page, Integer rows) {

        PageHelper.startPage(page,rows);


        ContentQuery query = new ContentQuery();
        if(content!=null){
            if(content.getTitle()!=null&&!"".equals(content.getTitle())){
                query.createCriteria().andTitleLike("%"+content.getTitle()+"%");
            }
        }
       Page<Content> contentPage = (Page<Content>)contentDao.selectByExample(query);

        return new PageResult(contentPage.getTotal(),contentPage.getResult());
    }

    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        ContentQuery query = new ContentQuery();

        query.createCriteria().andCategoryIdEqualTo(categoryId);

        return contentDao.selectByExample(query);
    }
}
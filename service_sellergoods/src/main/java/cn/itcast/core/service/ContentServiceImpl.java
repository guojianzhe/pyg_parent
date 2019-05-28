
package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {
    @Autowired
    ContentDao contentDao;

    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public List<Content> findAll() {

        return contentDao.selectByExample(null);
    }

    @Override
    public void add(Content content) {


        //1.将新广告,添加到数据库中

        contentDao.insertSelective(content);

        //2.根据分类id到redis中删除对应分类的广告集合数据
        System.out.println("通过添加广告的方式删除了id为"+content.getCategoryId()+"的广告");
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());

    }

    @Override
    public Content findOne(Long id) {

        return contentDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Content content) {
        //1.根据广告id到数据库中查询原来的广告对象
        Content oldContent = contentDao.selectByPrimaryKey(content.getId());


        //2.根据原来的广告对象中的分类id,到redis中删除对应的广告集合数据
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(oldContent.getCategoryId());

        System.out.println("删除老数据redis中的id是"+oldContent.getCategoryId());
        //3.根据传入的最新的广告对象中的分类id,删除redis中对应的广告集合数据
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
        System.out.println("删除老数据redis中的id是"+content.getCategoryId());
        //4.将新的广告对象更新到数据库中
        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public void delete(long[] ids) {
        if(ids!=null){
            for (long id : ids) {
                //1.根据广告id,到数据库中查询广告对象
                Content content = contentDao.selectByPrimaryKey(id);
                //2.根据广告对象中的分类id,删除redis中对应的广告集合数据
                redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
                System.out.println("删除了id为"+content.getCategoryId()+"的广告");
                //3.根据广告id删除数据库中的广告数据
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

    /**
     * 整个redis相当于一个大的hashMap,在这个map中key是不可以重复的,所以是稀缺资源
     * @param categoryId
     * @return
     */
    @Override
    public List<Content> findByCategoryIdFromRedis(Long categoryId) {

        //1.首先根据分类id到redis中获取数据
        List<Content> contentList = (List<Content>) redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).get(categoryId);
        System.out.println("redis中查询Content");
        //2.如果redis中没有数据则从数据库中查询出来
        if(contentList==null){
            //3.如果数据库中获取到数据,则放入redis中一份
             contentList = findByCategoryId(categoryId);
            System.out.println("数据库查询Content");
            redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).put(categoryId,contentList);

        }

        return contentList;
    }
}
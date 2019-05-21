package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SellerServiceImpl implements SellerService {

    @Resource
    SellerDao sellerDao;

    @Override
    public void add(Seller seller) {

        seller.setCreateTime(new Date());
        //审核状态默认为零  审核未通过  1 审核通过
        seller.setStatus("0");

        sellerDao.insertSelective(seller);
    }

    @Override
    public PageResult findPage(Seller seller, Integer page, Integer rows) {

        PageHelper.startPage(page,rows);

        SellerQuery query = new SellerQuery();

        SellerQuery.Criteria criteria = query.createCriteria();
        if(seller!=null){
            if(seller.getStatus()!=null &&!"".equals(seller.getStatus())){
                criteria.andStatusEqualTo(seller.getStatus());
            }
            if(seller.getName()!=null&&!"".equals(seller.getName())){
                criteria.andNameLike("%"+seller.getName()+"%");
            }
            if(seller.getNickName()!=null&&!"".equals(seller.getNickName())){
                criteria.andNickNameLike("%"+seller.getNickName()+"%");
            }


        }


        Page<Seller> sellerList = (Page<Seller>)sellerDao.selectByExample(query);


        return new PageResult(sellerList.getTotal(),sellerList.getResult());
    }

    @Override
    public Seller findOne(String id) {
        return sellerDao.selectByPrimaryKey(id);
    }
}

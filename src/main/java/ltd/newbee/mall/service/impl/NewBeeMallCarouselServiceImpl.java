/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service.impl;

import ltd.newbee.mall.common.Constants;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.vo.NewBeeMallIndexCarouselVO;
import ltd.newbee.mall.dao.CarouselMapper;
import ltd.newbee.mall.entity.Carousel;
import ltd.newbee.mall.service.NewBeeMallCarouselService;
import ltd.newbee.mall.util.BeanUtil;
import ltd.newbee.mall.util.PageQueryUtil;
import ltd.newbee.mall.util.PageResult;
import ltd.newbee.mall.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class NewBeeMallCarouselServiceImpl implements NewBeeMallCarouselService {

    /**
     * 首页轮播图缓存 key 前缀，完整 key 形如 newbee:index:carousel:5
     */
    private static final String INDEX_CAROUSEL_CACHE_PREFIX = "newbee:index:carousel:";

    @Autowired
    private CarouselMapper carouselMapper;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public PageResult getCarouselPage(PageQueryUtil pageUtil) {
        List<Carousel> carousels = carouselMapper.findCarouselList(pageUtil);
        int total = carouselMapper.getTotalCarousels(pageUtil);
        PageResult pageResult = new PageResult(carousels, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String saveCarousel(Carousel carousel) {
        if (carouselMapper.insertSelective(carousel) > 0) {
            evictIndexCarouselCache();
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateCarousel(Carousel carousel) {
        Carousel temp = carouselMapper.selectByPrimaryKey(carousel.getCarouselId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        temp.setCarouselRank(carousel.getCarouselRank());
        temp.setRedirectUrl(carousel.getRedirectUrl());
        temp.setCarouselUrl(carousel.getCarouselUrl());
        temp.setUpdateTime(new Date());
        if (carouselMapper.updateByPrimaryKeySelective(temp) > 0) {
            evictIndexCarouselCache();
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public Carousel getCarouselById(Integer id) {
        return carouselMapper.selectByPrimaryKey(id);
    }

    @Override
    public Boolean deleteBatch(Integer[] ids) {
        if (ids.length < 1) {
            return false;
        }
        //删除数据
        boolean deleted = carouselMapper.deleteBatch(ids) > 0;
        if (deleted) {
            evictIndexCarouselCache();
        }
        return deleted;
    }

    @Override
    public List<NewBeeMallIndexCarouselVO> getCarouselsForIndex(int number) {
        String cacheKey = INDEX_CAROUSEL_CACHE_PREFIX + number;
        // 1. 先读缓存，命中直接返回（这次请求就是 0 次数据库查询）
        List<NewBeeMallIndexCarouselVO> cached = redisCacheUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        // 2. 缓存没命中，回源查数据库（原逻辑不变）
        List<NewBeeMallIndexCarouselVO> newBeeMallIndexCarouselVOS = new ArrayList<>(number);
        List<Carousel> carousels = carouselMapper.findCarouselsByNum(number);
        if (!CollectionUtils.isEmpty(carousels)) {
            newBeeMallIndexCarouselVOS = BeanUtil.copyList(carousels, NewBeeMallIndexCarouselVO.class);
        }
        // 3. 写回缓存，下次请求直接命中
        redisCacheUtil.set(cacheKey, newBeeMallIndexCarouselVOS);
        return newBeeMallIndexCarouselVOS;
    }

    /**
     * 轮播图数据变更后删除首页缓存，下次访问自动重建。
     * 这就是 Cache-Aside 模式：先更新数据库，再删除缓存（而不是更新缓存）。
     */
    private void evictIndexCarouselCache() {
        redisCacheUtil.delete(INDEX_CAROUSEL_CACHE_PREFIX + Constants.INDEX_CAROUSEL_NUMBER);
    }
}

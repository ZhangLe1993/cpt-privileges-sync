package com.aihuishou.bi.service;

import com.aihuishou.bi.conf.CacheConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final Logger logger = LoggerFactory.getLogger(CacheService.class);

    /**
     *  清除 MAP_MENU_AUTH 缓存
     */
    @CacheEvict(value = CacheConf.MAP_MENU_AUTH, allEntries = true)
    public void removeMMA() {
        //logger.info("缓存：【{}】清除成功", CacheConf.MAP_MENU_AUTH);
    }

    /**
     *  清除 LIST_NODE_AUTH 缓存
     */
    @CacheEvict(value = CacheConf.LIST_NODE_AUTH, allEntries = true)
    public void removeLNA() {
        //logger.info("缓存：【{}】清除成功", CacheConf.LIST_NODE_AUTH);
    }

    /**
     *  根据 oBId 清除 LIST_USER_AUTH 缓存
     */
    @CacheEvict(value = CacheConf.LIST_USER_AUTH, key = "#obId")
    public void removeLUA(String obId) {
        //logger.info("用户:【{}】的权限缓存：【{}】清除成功", obId, CacheConf.LIST_USER_AUTH);
    }

    /**
     *  清除 所有用户的 LIST_USER_AUTH 缓存
     */
    @CacheEvict(value = CacheConf.LIST_USER_AUTH, allEntries = true)
    public void removeLUA() {
        //logger.info("所有用户的权限缓存：【{}】清除成功", CacheConf.LIST_USER_AUTH);
    }

    /**
     *  清除 所有用户的 LIST_USER_AUTH 缓存
     */
    @CacheEvict(value = CacheConf.LIST_USER_AUTH_MONGO, allEntries = true)
    public void removeLUAM() {
        //logger.info("所有用户的权限缓存：【{}】清除成功", CacheConf.LIST_USER_AUTH_MONGO);
    }

    /**
     *  根据 position 清除 POSITION_MAPPING_MODEL 缓存
     */
    @CacheEvict(value = CacheConf.POSITION_MAPPING_MODEL, key = "#source")
    public void removePMM(String source) {
        //logger.info("一个源菜单:【{}】的菜单映射缓存：【{}】清除成功", source, CacheConf.POSITION_MAPPING_MODEL);
    }

    /**
     *  清除 所有源菜单的菜单映射的 POSITION_MAPPING_MODEL 缓存
     */
    @CacheEvict(value = CacheConf.POSITION_MAPPING_MODEL, allEntries = true)
    public void removePMM() {
        //logger.info("一个源菜单的所有菜单映射缓存：【{}】清除成功", CacheConf.POSITION_MAPPING_MODEL);
    }


    /**
     *  清除 所有源菜单的仅仅映射 POSITION_MAPPING_MAP 缓存
     */
    @CacheEvict(value = CacheConf.POSITION_MAPPING_MAP, allEntries = true)
    public void removePMMS() {
        //logger.info("所有源菜单的仅仅映射缓存：【{}】清除成功", CacheConf.POSITION_MAPPING_MAP);
    }


    /**
     *  清除 菜单和文件路径URL映射缓存
     */
    @CacheEvict(value = CacheConf.POSITION_MAP, allEntries = true)
    public void removePM() {
        //logger.info("菜单和文件路径URL映射缓存：【{}】清除成功", CacheConf.POSITION_MAP);
    }


    /**
     *  清除 精致提炼菜单
     */
    @CacheEvict(value = CacheConf.REFINED_MENU, allEntries = true)
    public void removeRM() {
        //logger.info("精致提炼菜单缓存：【{}】清除成功", CacheConf.REFINED_MENU);
    }

    /**
     *  清除 精致提炼菜单
     */
    @CacheEvict(value = CacheConf.CURRENT_USER, allEntries = true)
    public void removeCU() {
        //logger.info("当前登录用户缓存：【{}】清除成功", CacheConf.CURRENT_USER);
    }

}

package com.aihuishou.bi.handler;

import com.aihuishou.bi.entity.User;
import com.aihuishou.bi.entity.UserOperation;
import com.aihuishou.bi.service.SyncService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SyncHandle implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SyncHandle.class);

    @Autowired
    private SyncService syncService;

    @Override
    public void run(String... args) throws Exception {
        long startTime = System.currentTimeMillis();
        List<User> users = syncService.all();
        if(users == null || users.size() == 0) {
            return;
        }
        syncService.truncate();
        List<List<User>> batch = Lists.partition(users, 200);
        //并发处理
        batch.parallelStream().forEach(list -> {
            List<UserOperation> userOperations = Collections.synchronizedList(new ArrayList<>());
            for(User user : list) {
                Integer obId = user.getObId();
                if(obId == null || "".equalsIgnoreCase(obId.toString())) {
                    continue;
                }
                List<UserOperation> temp = syncService.syncUserPermission(obId.toString());
                userOperations.addAll(temp);
                //logger.info("加入员工 obId:【{}】,姓名:【{}】", obId, user.getName());
            }
            try {
                syncService.save(userOperations);
                logger.info("线程批次存入数据库: 当前线程：{}", Thread.currentThread());
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                userOperations = null;
            }
        });
        long endTime = System.currentTimeMillis();
        logger.info("耗时：{} ms", endTime - startTime);
    }
}
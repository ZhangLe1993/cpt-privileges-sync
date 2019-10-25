package com.aihuishou.bi.service;

import com.aihuishou.bi.entity.OperationMapping;
import com.aihuishou.bi.entity.User;
import com.aihuishou.bi.entity.UserOperation;
import com.aihuishou.bi.utils.RestTemplateUtils;
import com.aihuishou.bi.utils.RetryTemplate;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SyncService {

    @Value("${third.interface.permission}")
    private String permissionInterface;

    @Autowired
    private DataSource dataSource;

    @Autowired
    @Qualifier(value = "basicMongoTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    private CacheService cacheService;

    public List<UserOperation> syncUserPermission(Set<String> container, String obId) {
        Map<String, Object> params = ImmutableMap.of("observerId", obId);
        ResponseEntity<String> content = null;
        try {
            content = new RetryTemplate() {
                @Override
                protected ResponseEntity<String> doService() {
                    return RestTemplateUtils.post(permissionInterface, params, String.class);
                }
            }.setRetryTime(3).setSleepTime(1000).execute();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (content == null) {
            return new ArrayList<>();
        }
        String body = content.getBody();
        JSONArray jsonArray = JSONArray.parseArray(body);
        if (jsonArray == null || jsonArray.size() == 0) {
            return new ArrayList<>();
        }
        List<UserOperation> operations = jsonArray.stream().filter(f -> {
            JSONObject json = (JSONObject) f;
            String accessName = json.getString("name");
            return container.contains(accessName);
        }).map(p -> {
            JSONObject json = (JSONObject) p;
            UserOperation userOperation = new UserOperation();
            userOperation.setObserverId(Integer.parseInt(obId));
            userOperation.setAccessId(json.getInteger("iD"));
            userOperation.setAccessName(json.getString("name"));
            userOperation.setActive(1);
            return userOperation;
        }).collect(Collectors.toList());
        return operations;
    }

    public List<User> all() throws SQLException {
        String sql = "SELECT observer_account_id as obId, observer_account_user_name AS name, observer_account_mobile_txt AS mobile,observer_account_email_txt AS email,observer_account_employee_no AS employeeNo from dim_observer_account where observer_account_is_active_flag = 1 and observer_account_id <> -1";
        return new QueryRunner(dataSource).query(sql, new BeanListHandler<>(User.class));
    }

    public void truncate() throws SQLException {
        String sql = "truncate table user_operation_min;";
        new QueryRunner(dataSource).update(sql);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transactional() throws SQLException {
        drop();
        reName();
    }

    /**
     * 复制表结构
     */
    public void like() throws SQLException {
        String sql = "CREATE TABLE user_operation_min_temp LIKE user_operation_min;";
        new QueryRunner(dataSource).update(sql);
    }

    /**
     * 删掉旧表
     * @throws SQLException
     */
    public void drop() throws SQLException {
        String sql = "drop table user_operation_min;";
        new QueryRunner(dataSource).update(sql);
    }

    /**
     * 新表重命名
     */
    public void reName() throws SQLException {
        String sql = "ALTER TABLE user_operation_min_temp RENAME TO  user_operation_min;";
        new QueryRunner(dataSource).update(sql);
    }

    public void save(List<UserOperation> list) throws SQLException {
        if (list == null || list.size() == 0) {
            return;
        }
        pre(list);
    }

    /**
     * 预处理占位符问题
     * @param list
     * @throws SQLException
     */
    private void pre(List<UserOperation> list) throws SQLException {
        // 21845 * 3 = 65535  占位符不可以超过这个数
        if (list.size() > 21845) {
            List<List<UserOperation>> parts = Lists.partition(list, 21845);
            parts.forEach(this::batch);
            return;
        }
        batch(list);
    }

    /**
     * 数据存入新的临时表
     * @param list
     */
    private void batch(List<UserOperation> list) {
        String sql = "INSERT INTO user_operation_min_temp(observer_id, access_id, access_name) VALUES (?, ?, ?) ";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            for (UserOperation obj : list) {
                preparedStatement.setInt(1, obj.getObserverId());
                preparedStatement.setInt(2, obj.getAccessId());
                preparedStatement.setString(3, obj.getAccessName());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        } catch (Exception e) {

        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                    preparedStatement = null;
                } catch (Exception e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (Exception e) {

                }
            }
        }
    }

    /**
     * 本地数据库有的权限
     * @return
     * @throws SQLException
     */
    public List<String> container() throws SQLException {
        String sql = "select distinct target_operation from operation_mapping;";
        return new QueryRunner(dataSource).query(sql, new ColumnListHandler<String>("target_operation"));
    }


    public void recreate() throws SQLException {
        String sql = "DROP TABLE IF EXISTS `user_operation_min`;";
        new QueryRunner(dataSource).update(sql);
        sql = "CREATE TABLE `user_operation_min` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `observer_id` int(11) NOT NULL,\n" +
                "  `access_id` int(11) NOT NULL,\n" +
                "  `access_name` varchar(32) NOT NULL,\n" +
                "  `active` tinyint(4) NOT NULL DEFAULT '1',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  KEY `index_observer_id` (`observer_id`),\n" +
                "  KEY `index_access_name` (`access_name`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        new QueryRunner(dataSource).update(sql);
    }

    public Set<String> syncOperationList() throws SQLException {
        List<OperationMapping> list = mongoTemplate.find(new Query(), OperationMapping.class, "userPermissionOperationMapping");
        new QueryRunner(dataSource).update("truncate table operation_mapping;");
        String sql = "INSERT INTO `operation_mapping`(source_operation, target_operation) VALUES(?, ?)";
        Object[][] params = new Object[list.size()][2];
        for(int i = 0; i < list.size(); i++) {
            String operation  = list.get(i).getOperation();
            String accessName = list.get(i).getAccessName();
            params[i] = new Object[]{operation, accessName};
        }
        new QueryRunner(dataSource).batch(sql, params);
        return list.stream().map(OperationMapping::getAccessName).collect(Collectors.toSet());
    }



    public void clearCache() {
        cacheService.removeLNA();
        cacheService.removeLUA();
        cacheService.removeMMA();
        cacheService.removePM();
        cacheService.removePMM();
        cacheService.removePMMS();
        cacheService.removeRM();
        cacheService.removeCU();
        cacheService.removeLUAM();
    }
}

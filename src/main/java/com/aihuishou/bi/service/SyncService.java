package com.aihuishou.bi.service;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SyncService {

    @Value("${third.interface.permission}")
    private String permissionInterface;
    @Resource
    private DataSource dataSource;

    public List<UserOperation> syncUserPermission(String obId) {
        Map<String, Object> params = ImmutableMap.of("data", ImmutableMap.of("observerId", obId));
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
        List<UserOperation> operations = jsonArray.stream().map(p -> {
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

    public void save(List<UserOperation> list) throws SQLException {
        if (list == null || list.size() == 0) {
            return;
        }
        pre(list);
        /*int size = list.size();
        Object[][] params = new Object[size][3];
        int i;
        for (i = 0; i < size; i++) {
            UserOperation obj = list.get(i);
            params[i] = new Object[]{obj.getObserverId(), obj.getAccessId(), obj.getAccessName()};
        }
        String sql = "insert into user_operation(observer_id, access_id, access_name) values (?, ?, ?);";
        new QueryRunner(dataSource).batch(sql, params);*/
    }

    private void pre(List<UserOperation> list) throws SQLException {
        // 21845 * 3 = 65535  占位符不可以超过这个数
        if (list.size() > 21845) {
            List<List<UserOperation>> parts = Lists.partition(list, 21845);
            parts.forEach(this::batch);
            return;
        }
        batch(list);
    }


    private void batch(List<UserOperation> list) {
        String sql = "INSERT INTO user_operation_min(observer_id, access_id, access_name) VALUES (?, ?, ?) ";
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


}

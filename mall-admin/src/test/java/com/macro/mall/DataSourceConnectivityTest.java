package com.macro.mall;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MallAdminApplication.class)
@ActiveProfiles("dev")
public class DataSourceConnectivityTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Test
    public void shouldConnectToConfiguredDatabase() throws Exception {
        // 通过环境感知，确保连接串已经套用环境变量或默认值
        String expectedUrl = environment.getProperty("spring.datasource.url");
        assertNotNull("spring.datasource.url 未加载", expectedUrl);

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull("未创建数据库连接", connection);
            assertEquals("数据源未使用期望的 JDBC URL", expectedUrl, connection.getMetaData().getURL());

            try (PreparedStatement statement = connection.prepareStatement("select 1");
                 ResultSet resultSet = statement.executeQuery()) {
                assertTrue("无法执行最小化探活查询", resultSet.next());
            }
        }
    }
}

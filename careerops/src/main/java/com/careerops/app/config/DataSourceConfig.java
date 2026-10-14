
package com.careerops.app.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;

@Configuration
@Profile("!test")
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/careerops?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}")
    private String configuredUrl;

    @Value("${spring.datasource.username:root}")
    private String configuredUsername;

    @Value("${spring.datasource.password:}")
    private String configuredPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = configuredUrl;
        String username = configuredUsername;
        String password = configuredPassword;
        String driver = null;

        if (url != null && url.startsWith("jdbc:mysql:")) {
            boolean reachable = isHostPortReachable(url);
            if (!reachable) {
                log.warn("MySQL server at '{}' is not reachable. Falling back to persistent local file database.", url);
                File dataDir = new File("careerops/data");
                if (!dataDir.exists()) {
                    dataDir = new File("data");
                }
                if (!dataDir.exists()) {
                    boolean created = dataDir.mkdirs();
                    log.info("Created local data directory: {}", created);
                }
                String absPath = dataDir.getAbsolutePath().replace('\\', '/');
                url = "jdbc:h2:file:" + absPath + "/careerops;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE";
                username = "sa";
                password = "";
                driver = "org.h2.Driver";
            } else {
                driver = "com.mysql.cj.jdbc.Driver";
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        if (driver != null) {
            config.setDriverClassName(driver);
        }
        config.setPoolName("CareerOpsHikariPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);

        log.info("CareerOps DataSource initialized successfully with URL: {}", url);
        return new HikariDataSource(config);
    }

    private boolean isHostPortReachable(String jdbcUrl) {
        try {
            String clean = jdbcUrl.substring("jdbc:mysql://".length());
            int slashIdx = clean.indexOf('/');
            String hostPortPart = (slashIdx != -1) ? clean.substring(0, slashIdx) : clean;
            int qIdx = hostPortPart.indexOf('?');
            if (qIdx != -1) {
                hostPortPart = hostPortPart.substring(0, qIdx);
            }
            String host = "localhost";
            int port = 3306;
            if (hostPortPart.contains(":")) {
                String[] parts = hostPortPart.split(":");
                host = parts[0].trim();
                port = Integer.parseInt(parts[1].trim());
            } else if (!hostPortPart.isEmpty()) {
                host = hostPortPart.trim();
            }

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 1500);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}

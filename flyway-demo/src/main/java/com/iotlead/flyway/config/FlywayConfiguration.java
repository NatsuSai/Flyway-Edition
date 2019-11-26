/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iotlead.flyway.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.database.shardingsphere.ShardingSphereDatabase;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FlywayConfig
 *
 * @author liufuhong
 * @since 2019-11-26 9:14
 */

@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = "enable-custom-config", havingValue = "true")
public class FlywayConfiguration {

  @Bean
  public FlywayMigrationInitializer flywayInitializer(Flyway flyway, ObjectProvider<FlywayMigrationStrategy> migrationStrategy) {
    //添加ss配置，覆盖元mysql配置
    Flyway.DATABASE_MAPPING.put(DatabaseType.MYSQL, ShardingSphereDatabase::new);

    return new FlywayMigrationInitializer(flyway, migrationStrategy.getIfAvailable());
  }
}

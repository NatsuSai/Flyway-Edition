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
package com.iotlead.flyway.config.flyway;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * ShardingSphere connection.
 */
public class ShardingSphereConnection extends Connection<ShardingSphereDatabase> {
    private static final Log LOG = LogFactory.getLog(ShardingSphereConnection.class);

    private static final String USER_VARIABLES_TABLE_MARIADB = "information_schema.user_variables";
    private static final String USER_VARIABLES_TABLE_MYSQL = "performance_schema.user_variables_by_thread";

    private final String userVariablesQuery;
    private final boolean canResetUserVariables;

    ShardingSphereConnection(Configuration configuration, ShardingSphereDatabase database, java.sql.Connection connection, boolean originalAutoCommit) {
        super(configuration, database, connection, originalAutoCommit);

        userVariablesQuery = "SELECT variable_name FROM "
                + (database.isMariaDB() ? USER_VARIABLES_TABLE_MARIADB : USER_VARIABLES_TABLE_MYSQL)
                + " WHERE variable_value IS NOT NULL";
        canResetUserVariables = hasUserVariableResetCapability();
    }

    private boolean hasUserVariableResetCapability() {
        if (database.isMariaDB() && !database.getVersion().isAtLeast("10.2")) {
            LOG.debug("Disabled user variable reset as it is only available from MariaDB 10.2 onwards");
            return false;
        }
        if (!database.isMariaDB() && !database.getVersion().isAtLeast("5.7")) {
            LOG.debug("Disabled user variable reset as it is only available from MySQL 5.7 onwards");
            return false;
        }

        try {
            jdbcTemplate.queryForStringList(userVariablesQuery);
            return true;
        } catch (SQLException e) {
            LOG.debug("Disabled user variable reset as "
                    + (database.isMariaDB() ? USER_VARIABLES_TABLE_MARIADB : USER_VARIABLES_TABLE_MYSQL)
                    + "cannot be queried (SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode() + ")");
            return false;
        }
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        resetUserVariables();
    }

    // #2197: prevent user-defined variables from leaking beyond the scope of a migration
    private void resetUserVariables() throws SQLException {
        if (canResetUserVariables) {
            List<String> userVariables = jdbcTemplate.queryForStringList(userVariablesQuery);
            if (!userVariables.isEmpty()) {
                boolean first = true;
                StringBuilder setStatement = new StringBuilder("SET ");
                for (String userVariable : userVariables) {
                    if (first) {
                        first = false;
                    } else {
                        setStatement.append(",");
                    }
                    setStatement.append("@").append(userVariable).append("=NULL");
                }
                jdbcTemplate.executeStatement(setStatement.toString());
            }
        }
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT DATABASE()");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        if (StringUtils.hasLength(schema)) {
//            jdbcTemplate.getConnection().setCatalog(schema);
        } else {
            try {
                // Weird hack to switch back to no database selected...
                String newDb = database.quote(UUID.randomUUID().toString());
                jdbcTemplate.execute("CREATE SCHEMA " + newDb);
                jdbcTemplate.execute("USE " + newDb);
                jdbcTemplate.execute("DROP SCHEMA " + newDb);
            } catch (Exception e) {
                LOG.warn("Unable to restore connection to having no default schema: " + e.getMessage());
            }
        }
    }

    @Override
    protected Schema doGetCurrentSchema() throws SQLException {
        String schemaName = getCurrentSchemaNameOrSearchPath();

        // #2206: MySQL and MariaDB can have URLs where no current schema is set, so we must handle this case explicitly.
        return schemaName == null ? null : getSchema(schemaName);
    }

    @Override
    public Schema getSchema(String name) {
        return new ShardingSphereSchema(jdbcTemplate, database, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        if (database.isPxcStrict()) {
            return super.lock(table, callable);
        }
        return new ShardingSphereNamedLockTemplate(jdbcTemplate, table.toString().hashCode()).execute(callable);
    }
}
/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.ahto.example.traffic.data.database.repositories.sql;

import fi.ahto.example.traffic.data.contracts.database.sql.DBCalendarDate;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Jouni Ahto
 */
@Repository
public class SQLCalendarDateBatchRepositoryImpl implements SQLCalendarDateBatchRepository {
    private String query;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        query = "INSERT INTO calendar_dates (service_num, exception_date, exception_type) VALUES (?, ?, ?)";
    }

    @Override
    public void saveBatch(List<DBCalendarDate> dates) {
        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DBCalendarDate date = dates.get(i);
                
                ps.setLong(1, date.getServiceId());
                ps.setObject(2, date.getExceptionDate());
                ps.setShort(3, date.getExceptionType());
            }

            @Override
            public int getBatchSize() {
                return dates.size();
            }
        });
    }
}

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
import fi.ahto.example.traffic.data.contracts.database.sql.DBFrequency;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
/**
 *
 * @author Jouni Ahto
 */
@Repository
public class SQLFrequencyBatchRepositoryImpl implements SQLFrequencyBatchRepository {
    private String query;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        query = "INSERT INTO frequencies (trip_num, start_time, end_time, headway_secs, exact_times) VALUES (?, ?, ?, ? ,?)";
    }
/*
    end_time integer NOT NULL,
    headway_secs smallint NOT NULL,
    start_time integer NOT NULL,
    trip_num bigint NOT NULL,
    exact_times smallint
*/
    public void saveBatch(List<DBFrequency> frequencies) {
        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DBFrequency freq = frequencies.get(i);
                
                ps.setLong(1, freq.getTripNum());
                ps.setInt(2, freq.getStartTime().toSecondOfDay());
                ps.setInt(3, freq.getEndTime().toSecondOfDay());
                ps.setShort(4, freq.getHeadwaySecs());
                ps.setShort(5, freq.getExactTimes());
            }

            @Override
            public int getBatchSize() {
                return frequencies.size();
            }
        });
    }

}

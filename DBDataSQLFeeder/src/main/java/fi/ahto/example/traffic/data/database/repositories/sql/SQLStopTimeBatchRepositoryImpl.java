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

import fi.ahto.example.traffic.data.contracts.database.sql.DBStopTime;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Jouni Ahto
 */
@Repository
public class SQLStopTimeBatchRepositoryImpl implements SQLStopTimeBatchRepository {
    private String query;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.query = "INSERT INTO stop_times " +
            "(trip_num, stop_num, arrival, departure, stop_sequence, pickup_type, dropoff_type, timepoint, headsign, dist_traveled) " + ""
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    /*
        try {
            String url = JdbcUtils.extractDatabaseMetaData(dataSource, "getURL");
            int i = 0;
            // if (dataSource.getConnection().
            // query = "INSERT INTO calendar_dates (service_num, exception_date, exception_type) VALUES (?, ?, ?)";
        } catch (MetaDataAccessException ex) {
            Logger.getLogger(SQLStopTimeBatchRepositoryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    */
    }

    @Override
    public void saveBatch(List<DBStopTime> times) {
        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DBStopTime time = times.get(i);
                ps.setLong(1, time.getTripNum());
                ps.setLong(2, time.getStopNum());
                ps.setInt(3, time.getArrival().toSecondOfDay());
                ps.setInt(4, time.getDeparture().toSecondOfDay());
                ps.setInt(5, time.getStopSequence());
                ps.setShort(6, time.getPickupType());
                ps.setShort(7, time.getDropoffType());
                ps.setShort(8, time.getTimepoint());
                ps.setString(9, time.getHeadsign());
                // ps.setDouble(10, time.getDistTraveled());
                ps.setDouble(10, 0.0);
                
                int j = 0;
            }

            @Override
            public int getBatchSize() {
                return times.size();
            }
        });
    }
}

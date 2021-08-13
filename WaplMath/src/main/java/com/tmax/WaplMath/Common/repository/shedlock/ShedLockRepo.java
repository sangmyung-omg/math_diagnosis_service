package com.tmax.WaplMath.Common.repository.shedlock;

import java.sql.Timestamp;
import java.util.List;

import com.tmax.WaplMath.Common.model.shedlock.ShedLock;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ShedLockRepo extends CrudRepository<ShedLock, String>{
    @Query("select s from ShedLock s where s.lockUntil > :ts_now and name=:name")
    List<ShedLock> checkLock(@Param("name") String lockname, @Param("ts_now") Timestamp tsNow);
}
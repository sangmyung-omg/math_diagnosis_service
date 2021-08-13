package com.tmax.WaplMath.Common.util.shedlock;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import java.net.InetAddress;

import com.tmax.WaplMath.Common.model.shedlock.ShedLock;
import com.tmax.WaplMath.Common.repository.shedlock.ShedLockRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ShedLockUtil {
    @Autowired
    private ShedLockRepo shedLockRepo;

    private String hostname;

    public ShedLockUtil() {
        //Set hostname. maynot work in windows
        try{
            this.hostname = InetAddress.getLocalHost().getHostName();
        }
        catch(Throwable e){
            log.error("Cannot get hostname");
        }
    }

    public boolean isLocked(String lockname){
        Timestamp tsNow = Timestamp.from(ZonedDateTime.now().toInstant());
        List<ShedLock> result = shedLockRepo.checkLock(lockname, tsNow);
        return result.size() != 0;
    }

    public boolean setLock(String lockname, Duration locktime){
        ZonedDateTime dtNow = ZonedDateTime.now();
        ZonedDateTime dtUntil = dtNow.plus(locktime);
        Timestamp tsNow = Timestamp.from(dtNow.toInstant());
        Timestamp tsUntil = Timestamp.from(dtUntil.toInstant());
        
        try {
            shedLockRepo.save(ShedLock.builder()
                                      .name(lockname)
                                      .lockedAt(tsNow)
                                      .lockUntil(tsUntil)
                                      .lockedBy(this.hostname)
                                      .build()
                                      );
        }
        catch(Throwable e){
            log.error("Something got wrong during {} lock save", lockname);
            return false;
        }

        return true;
    }

    public boolean tryLock(String lockname, Duration locktime){
        if(isLocked(lockname)){
            log.info("{} is already locked", lockname);
            return false;
        }

        return setLock(lockname, locktime);
    }

    public boolean releaseLock(String lockname){
        shedLockRepo.deleteById(lockname);
        return true;
    }
}

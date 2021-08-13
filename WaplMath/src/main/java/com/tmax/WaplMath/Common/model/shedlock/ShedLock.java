package com.tmax.WaplMath.Common.model.shedlock;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="SHEDLOCK")
public class ShedLock {
    @Id
    private String name;

    private Timestamp lockUntil;
    private Timestamp lockedAt;

    private String lockedBy;
}

package com.tmax.Recommend.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="PROBLEM_SET_DEMO")
public class ProblemSetDemoDAO {
	
	@Id
	private String probSetUuid;
	
	@Column(name="PROB1_UUID")
	private String prob1Uuid;
	
	@Column(name="PROB2_UUID")
	private String prob2Uuid;
	
	@Column(name="PROB3_UUID")
	private String prob3Uuid;
	
	private String chapter;
	private String part;
	
}

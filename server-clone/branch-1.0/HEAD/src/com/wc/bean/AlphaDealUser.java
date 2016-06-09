package com.wc.bean;
// default package

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

/**
 * WcUser entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name="dealMonitorUser"
    ,catalog="hcapi"
, uniqueConstraints = @UniqueConstraint(columnNames="userId")
)

public class AlphaDealUser  implements java.io.Serializable {


    // Fields    

     private String userId;
     private int running;
     private Timestamp lastRun;
     
    // Constructors

    /** default constructor */
    public AlphaDealUser() {
    }

	/** minimal constructor */
    public AlphaDealUser(String userId, int running) {
        this.userId = userId;
        this.running = running;
    }
    
    // Property accessors
    @Id 
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @GeneratedValue(generator = "system-uuid")
    @Column(name="UserId", unique=true, nullable=false)

    public String getUserId() {
        return this.userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Column(name="Running", unique=false, nullable=false, length=45)

    public int getRunning() {
        return this.running;
    }
    
    public void setRunning(int running) {
        this.running = running;
    }
    
    @Column(name="LastRun", unique=false, nullable=false, length=45)

    public Timestamp getLastRun() {
        return this.lastRun;
    }
    
    public void setLastRun(Timestamp lastRun) {
        this.lastRun = lastRun;
    }

}
package com.zx.utils.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * @author ZhaoXu
 * @date 2022/5/31 19:12
 */
@Entity
@Table(name = "scheduled_job_zx")
@Data
@EntityListeners(AuditingEntityListener.class)
public class BaseScheduledTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * 任务名字
     */
    private String jobName;

    /**
     * 任务组名字
     */
    private String jobGroupName;

    /**
     * 任务类名
     */
    private String jobClassName;

    /**
     * core表达式
     */
    private String cronExpression;

    /**
     * 任务参数
     */
    @Column(columnDefinition = "text")
    private String params;

    /**
     * 是否最少执行一次
     */
    private Boolean exeOnce;

    /**
     * 是否生效0/1
     */
    private Integer valid;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "gmt_create", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date gmtCreate;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "gmt_modified", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date gmtModified;
}


package com.zx.utils.repository;

import com.zx.utils.entity.ScheduledJobEntity;

import java.util.List;

/**
 * @author ZhaoXu
 * @date 2022/5/31 19:14
 */
public interface SchedulerJobRepository extends BaseRepository<ScheduledJobEntity, Long> {
    /**
     * 根据名称、组、状态查询任务状态
     * @param jobName       名称
     * @param jobGroupName  组
     * @param valid     状态
     * @return
     */
    List<ScheduledJobEntity> findByJobNameAndJobGroupNameAndValid(String jobName, String jobGroupName, Integer valid);
}

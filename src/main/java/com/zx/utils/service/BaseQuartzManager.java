package com.zx.utils.service;

import com.alibaba.fastjson.JSONObject;
import org.quartz.Job;

/**
 * @author ZhaoXu
 * @date 2022/5/31 17:00
 */
public interface BaseQuartzManager {
    /**
     * 新建一个定时任务
     * @param jobClass
     * @param jobName
     * @param jobGroupName
     * @param cronExpression
     * @param params
     * @param isInit
     */
    void createJob(Class<? extends Job> jobClass, String jobName, String jobGroupName, String cronExpression, JSONObject params, boolean isInit);

    /**
     * 删除定时任务
     *
     * @param jobName
     * @param jobGroupName
     */
    void deleteJob(String jobName, String jobGroupName);

    /**
     * 立即激活某个任务
     *
     * @param jobName
     * @param jobGroupName
     */
    void activeJob(String jobName, String jobGroupName);
}

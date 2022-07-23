package com.zx.utils.quartz.impl;

import com.alibaba.fastjson.JSONObject;
import com.zx.utils.constant.Constants;
import com.zx.utils.entity.ScheduledJobEntity;
import com.zx.utils.repository.SchedulerJobRepository;
import com.zx.utils.quartz.QuartzManager;
import com.zx.utils.util.ListUtil;
import com.zx.utils.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author ZhaoXu
 * @date 2022/5/31 17:03
 */
@Slf4j
@Service
public class QuartzManagerImpl implements QuartzManager, ApplicationRunner {
    @Autowired
    SchedulerJobRepository schedulerJobRepository;

    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

    public QuartzManagerImpl() throws SchedulerException {
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void createJob(Class<? extends Job> jobClass, String jobName, String jobGroupName, String cronExpression, JSONObject params, boolean exeOnce) {
        // 创建scheduler，调度器, 策略采用错过之后立即执行一次
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionFireAndProceed();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName, jobGroupName)
                .startNow()
                .withSchedule(scheduleBuilder)
                .build();
        // 定义一个JobDetail
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroupName)
                .build();
        trigger.getJobDataMap().putAll(params);
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            // 启动任务调度
            scheduler.start();
        } catch (Exception e) {
            log.error("创建定时任务失败，jobName：{}，jobGroupName：{}", jobName, jobGroupName);
        }

        List<ScheduledJobEntity> scheduledJobEntities = schedulerJobRepository.findByJobNameAndJobGroupNameAndValid(jobName, jobGroupName, Constants.VALID_TRUE);
        if (ListUtil.isEmpty(scheduledJobEntities)) {
            // 持久化到数据库
            ScheduledJobEntity scheduledJobEntity = new ScheduledJobEntity();
            scheduledJobEntity.setJobName(jobName);
            scheduledJobEntity.setExeOnce(exeOnce);
            scheduledJobEntity.setJobGroupName(jobGroupName);
            scheduledJobEntity.setJobClassName(jobClass.getName());
            scheduledJobEntity.setCronExpression(cronExpression);
            scheduledJobEntity.setValid(Constants.VALID_TRUE);
            scheduledJobEntity.setParams(params.toJSONString());
            schedulerJobRepository.save(scheduledJobEntity);
        }
        log.info("创建定时任务成功，jobName：{}，jobGroupName：{}", jobName, jobGroupName);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteJob(String jobName, String jobGroupName) {
        try {
            scheduler.pauseTrigger(TriggerKey.triggerKey(jobName, jobGroupName));
            scheduler.unscheduleJob(TriggerKey.triggerKey(jobName, jobGroupName));
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
        } catch (Exception e) {
            log.error("删除定时任务失败，jobName：{}，jobGroupName：{}", jobName, jobGroupName);
        }

        List<ScheduledJobEntity> scheduledJobEntities = schedulerJobRepository.findByJobNameAndJobGroupNameAndValid(jobName, jobGroupName, Constants.VALID_TRUE);
        if (!CollectionUtils.isEmpty(scheduledJobEntities)) {
            for (ScheduledJobEntity scheduledJobEntity : scheduledJobEntities) {
                scheduledJobEntity.setValid(Constants.VALID_FALSE);
            }
            schedulerJobRepository.saveAll(scheduledJobEntities);
            log.info("删除定时任务成功，jobName：{}，jobGroupName：{}", jobName, jobGroupName);
        }
    }

    @Override
    public void activeJob(String jobName, String jobGroupName) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MILLISECOND, 100);
            // 几分钟之后自动取消
            String cronExpression = TimeUtil.formatDateByPattern(calendar.getTime(), "ss mm HH dd MM ? yyyy");
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionFireAndProceed();

            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

            // 根据Cron表达式构建一个Trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);

            // 等300毫秒，激活定时任务
            Thread.sleep(300);
            deleteJob(jobName, jobGroupName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("激活定时任务失败，jobName：{}，jobGroupName：{}", jobName, jobGroupName);
        }
    }

    @Override
    @Async
    public void run(ApplicationArguments args) throws Exception {
        // 启动自动装配定时任务，执行之前未执行的任务
        List<ScheduledJobEntity> scheduledJobEntities = schedulerJobRepository.findByAttr("valid", Constants.VALID_TRUE.toString());
        for (ScheduledJobEntity scheduledJobEntity : scheduledJobEntities) {
            String jobName = scheduledJobEntity.getJobName();
            String jobGroupName = scheduledJobEntity.getJobGroupName();
            String jobClassName = scheduledJobEntity.getJobClassName();
            Boolean exeOnce = scheduledJobEntity.getExeOnce();

            long nowTime = System.currentTimeMillis();
            // 执行参数
            String paramsString = scheduledJobEntity.getParams();
            String cronExpressionString = scheduledJobEntity.getCronExpression();
            CronExpression cronExpression = new CronExpression(cronExpressionString);

            // 反解析下一次的执行时间
            Date nextDate = cronExpression.getNextValidTimeAfter(new Date());
            if (nextDate == null || nextDate.getTime() <= nowTime) {
                if (exeOnce) {
                    // 错过之后在5秒之后执行一次然后删除掉
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.SECOND, 5);
                    cronExpressionString = TimeUtil.formatDateByPattern(calendar.getTime(), "ss mm HH dd MM ? yyyy");
                } else {
                    deleteJob(jobName, jobGroupName);
                    return;
                }

            }

            JSONObject params = JSONObject.parseObject(paramsString);
            // 几分钟之后回退
            createJob((Class<? extends Job>) Class.forName(jobClassName), jobName, jobGroupName,
                    cronExpressionString, params, true);
        }
    }
}

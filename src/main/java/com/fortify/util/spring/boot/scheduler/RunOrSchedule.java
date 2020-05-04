package com.fortify.util.spring.boot.scheduler;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class RunOrSchedule implements CommandLineRunner {
	@Value("${runOnce:false}") private boolean runOnce;
	@Autowired private TaskScheduler scheduler;
	@Autowired private List<ISchedulableRunner> runners;
	@Autowired ApplicationContext context;

	@Override
	public void run(String... args) throws Exception {
		if ( runners==null || runners.isEmpty() ) {
			throw new RuntimeException("No runners found");
		} else {
			if ( isRunOnce(runners) ) {
				runOnce(runners);
			} else {
				schedule(runners);
			}
		}
	}
	
	private boolean isRunOnce(List<ISchedulableRunner> runners) {
		return runOnce || !runners.stream().anyMatch(this::hasSchedule);
	}
	
	private boolean hasSchedule(ISchedulableRunner runner) {
		return StringUtils.isNotBlank(runner.getCronSchedule())
				&& !"-".equals(runner.getCronSchedule());
	}

	private void runOnce(List<ISchedulableRunner> runners) {
		for ( ISchedulableRunner runner : runners ) {
			runner.run();
		}
		SpringApplication.exit(context);
	}
	
	private void schedule(List<ISchedulableRunner> runners) {
		for ( ISchedulableRunner runner : runners ) {
			scheduler.schedule(runner, new CronTrigger(runner.getCronSchedule()));
		}
	}
	
}

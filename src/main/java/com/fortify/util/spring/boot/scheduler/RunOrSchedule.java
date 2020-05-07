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
	@Autowired private List<ISchedulableRunnerFactory> runnerFactories;
	@Autowired ApplicationContext context;

	@Override
	public void run(String... args) throws Exception {
		// TODO Check if there are any enabled runner factories, throw exception otherwise
		if ( runnerFactories==null || runnerFactories.isEmpty() ) {
			throw new RuntimeException("No runners found");
		} else {
			if ( isRunOnce(runnerFactories) ) {
				runOnce(runnerFactories);
			} else {
				schedule(runnerFactories);
			}
		}
	}
	
	private boolean isRunOnce(List<ISchedulableRunnerFactory> runners) {
		return runOnce || !runners.stream().anyMatch(this::hasSchedule);
	}
	
	private boolean hasSchedule(ISchedulableRunnerFactory runnerFactory) {
		return StringUtils.isNotBlank(runnerFactory.getCronSchedule())
				&& !"-".equals(runnerFactory.getCronSchedule());
	}

	private void runOnce(List<ISchedulableRunnerFactory> runnerFactories) {
		for ( ISchedulableRunnerFactory runnerFactory : runnerFactories ) {
			if ( runnerFactory.isEnabled() ) {
				runnerFactory.getRunner().run();
			}
		}
		SpringApplication.exit(context);
	}
	
	private void schedule(List<ISchedulableRunnerFactory> runnerFactories) {
		for ( ISchedulableRunnerFactory runnerFactory : runnerFactories ) {
			if ( runnerFactory.isEnabled() && hasSchedule(runnerFactory) ) {
				scheduler.schedule(runnerFactory.getRunner(), new CronTrigger(runnerFactory.getCronSchedule()));
			}
		}
	}
	
}

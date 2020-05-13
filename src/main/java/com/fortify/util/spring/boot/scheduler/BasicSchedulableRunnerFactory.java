/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.util.spring.boot.scheduler;

import java.util.function.Supplier;

public class BasicSchedulableRunnerFactory implements ISchedulableRunnerFactory {
	private final Supplier<Boolean> enabled;
	private final Supplier<String> cronSchedule;
	private final Supplier<? extends ISchedulableRunner> runnerSupplier;
	
	public BasicSchedulableRunnerFactory(Supplier<? extends ISchedulableRunner> runnerSupplier, Supplier<Boolean> enabled, Supplier<String> cronSchedule) {
		this.runnerSupplier = runnerSupplier;
		this.enabled = enabled;
		this.cronSchedule = cronSchedule;
	}
	@Override
	public boolean isEnabled() {
		return enabled.get();
	}
	@Override
	public String getCronSchedule() {
		return cronSchedule.get();
	}
	@Override
	public ISchedulableRunner getRunner() {
		return runnerSupplier.get();
	}
}

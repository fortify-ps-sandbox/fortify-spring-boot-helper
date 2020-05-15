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
package com.fortify.util.spring.boot.env;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.StandardEnvironment;

public final class ModifyablePropertySource extends EnumerablePropertySource<ThreadLocal<Deque<ScopedPropertiesHolder>>> implements AutoCloseable {
	private static final Logger LOG = LoggerFactory.getLogger(ModifyablePropertySource.class);
	private static final ModifyablePropertySource INSTANCE = new ModifyablePropertySource();
	
	public static final ConfigurableEnvironment createEnvironment() {
		StandardEnvironment result = new StandardEnvironment();
		result.getPropertySources().addFirst(INSTANCE);
		LOG.trace("Created environment with modifyable property source support: {}", result);
		return result;
	}
	
	private ModifyablePropertySource() {
		super(ModifyablePropertySource.class.getName(), InheritableThreadLocal.withInitial(ModifyablePropertySource::createPropertiesDeque));
	}
	
	@Override
	public String[] getPropertyNames() {
		Deque<ScopedPropertiesHolder> deque = getDeque();
		List<String> propertyNames = deque.stream().map(p->p.getProperties().keySet()).flatMap(Set::stream).collect(Collectors.toList());
		LOG.trace("getPropertyNames(): {}", propertyNames);
		return propertyNames.toArray(new String[] {});
	}
	
	@Override
	public Object getProperty(String name) {
		Deque<ScopedPropertiesHolder> deque = getDeque();
		// TODO Support property variations? For example also look for propertyName if input name is property-name 
		Object result = deque.stream()
				.filter(p->p.getProperties().containsKey(name))
				.findFirst()
				.map(p->p.getProperties().get(name)).orElse(null);
		LOG.trace("getProperty({}): {}", name, result);
		return result;
	}

	public static final ModifyablePropertySource withProperties(Map<String, Object> properties) {
		Deque<ScopedPropertiesHolder> deque = INSTANCE.getDeque();
		ScopedPropertiesHolder scopedPropertiesHolder = getScopedPropertiesHolder(properties);
		LOG.trace("Adding {} to stack for properties {}", scopedPropertiesHolder, properties);
		deque.addFirst(scopedPropertiesHolder);
		return INSTANCE;
	}
	
	@Override
	public void close() {
		Deque<ScopedPropertiesHolder> deque = getDeque();
		ScopedPropertiesHolder removedScopedPropertiesHolder = deque.removeFirst();
		LOG.trace("Removed {} from stack", removedScopedPropertiesHolder);
	}
	
	public static final <R> R withProperties(Map<String, Object> properties, Callable<R> callable) throws Exception {
		try (ModifyablePropertySource tlps = withProperties(properties)) {
			return callable.call();
		}
	}
	
	public static final void withProperties(Map<String, Object> properties, Runnable runnable) {
		try (ModifyablePropertySource tlps = withProperties(properties)) {
			runnable.run();
		}
	}
	
	static final ScopedPropertiesHolder getScopedPropertiesHolder() {
		return INSTANCE.getDeque().peekFirst();
	}
	
	private Deque<ScopedPropertiesHolder> getDeque() {
		return getSource().get();
	}
	
	
	private static final Deque<ScopedPropertiesHolder> createPropertiesDeque() {
		return new LinkedList<>(Arrays.asList(ScopedPropertiesHolder.DEFAULT));
	}
	
	private static final ScopedPropertiesHolder getScopedPropertiesHolder(Map<String, Object> properties) {
		return ( properties==null || properties.size()==0 ) 
			? getScopedPropertiesHolder()
			: new ScopedPropertiesHolder(properties);
	}
}
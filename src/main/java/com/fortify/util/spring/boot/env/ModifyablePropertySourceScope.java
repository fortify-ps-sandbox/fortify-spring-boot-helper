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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;

public class ModifyablePropertySourceScope implements Scope {
	public static final String SCOPE_NAME = "propertySource";
	private static final Logger LOG = LoggerFactory.getLogger(ModifyablePropertySourceScope.class);
	
	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Map<String, Object> scopedObjects = getScopedObjects();
		if(!scopedObjects.containsKey(name)) {
	        Object newInstance = objectFactory.getObject();
			scopedObjects.put(name, newInstance);
	        LOG.trace("get({}): Created new scoped instance {}", name, newInstance);
	    }
	    Object result = scopedObjects.get(name);
	    LOG.trace("get({}): {}", name, result);
	    return result;
	}

	@Override
	public Object remove(String name) {
		LOG.trace("remove({})", name);
		getDestructionCallbacks().remove(name);
	    return getScopedObjects().remove(name);
	}

	private ScopedPropertiesHolder getScopedPropertiesHolder() {
		return ModifyablePropertySource.getScopedPropertiesHolder();
	}
	
	private Map<String, Object> getScopedObjects() {
		return getScopedPropertiesHolder().getScopedObjects();
	}
	
	private Map<String, Runnable> getDestructionCallbacks() {
		return getScopedPropertiesHolder().getDestructionCallbacks();
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		getScopedPropertiesHolder().registerDestructionCallback(name, callback);
	}

	@Override
	public Object resolveContextualObject(String key) {
		return null;
	}

	@Override
	public String getConversationId() {
		return getScopedPropertiesHolder().getId();
	}

	public static final class ModifyablePropertySourceScopeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	    @Override
	    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
	    	LOG.trace("Registering scope {}", SCOPE_NAME);
	        factory.registerScope(SCOPE_NAME, new ModifyablePropertySourceScope());
	    }
	}
}

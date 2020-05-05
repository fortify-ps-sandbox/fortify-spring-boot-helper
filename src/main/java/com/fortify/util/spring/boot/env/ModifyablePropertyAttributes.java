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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class ModifyablePropertyAttributes {
	static final ModifyablePropertyAttributes DEFAULT = new ModifyablePropertyAttributes(new HashMap<>());
	private final String id;
	private final Map<String, Object> scopedObjects = Collections.synchronizedMap(new HashMap<String, Object>());
	private final Map<String, Runnable> destructionCallbacks = Collections.synchronizedMap(new HashMap<String, Runnable>());
	private final Map<String, Object> properties;
	
	ModifyablePropertyAttributes(Map<String, Object> properties) {
		this.id = UUID.randomUUID().toString();
		this.properties = Collections.synchronizedMap(properties);
	}

	public String getId() {
		return id;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}

	public Map<String, Object> getScopedObjects() {
		return scopedObjects;
	}
	
	public Map<String, Runnable> getDestructionCallbacks() {
		return destructionCallbacks;
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		getDestructionCallbacks().put(name, callback);
	}
	
	public void clear() {
		properties.clear();
		for ( Runnable r : destructionCallbacks.values() ) {
			r.run();
		}
		destructionCallbacks.clear();
		scopedObjects.clear();
	}
}
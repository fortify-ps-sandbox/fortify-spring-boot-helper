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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import com.fortify.util.spring.boot.env.ModifyablePropertySourceScope.ModifyablePropertySourceScopeBeanFactoryPostProcessor;

// TODO Convert this into proper, separate unit tests
public final class ModifyablePropertySourceTest {
	@Test
	public void test1() {
		TestApplication.main(new String[] {});
	}

	@SpringBootApplication
	static class TestApplication implements CommandLineRunner {
		
		
		@Bean
		public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
			return new ModifyablePropertySourceScopeBeanFactoryPostProcessor();
		}
		
		
		/**
		 * Start the application
		 * @param args
		 */
		public static void main(String[] args) {
			System.setProperty("abc.value", getInitialValue());
			new SpringApplicationBuilder(TestApplication.class)
				.environment(ModifyablePropertySource.createEnvironment())
				.build().run(args);
		}

		@Override
		public void run(String... args) throws Exception {
			test(()->ctx.getBean("testConfigSingleton", TestConfig.class), i->getInitialValue(), true);
			test(()->ctx.getBean("testConfigPrototype", TestConfig.class), this::getValue, false);
			test(()->ctx.getBean("testConfigModifyableProperties", TestConfig.class), this::getValue, true);
			for ( int i = 0 ; i < 5 ; i++ ) {
				try (ModifyablePropertySource x = ModifyablePropertySource.withProperties(getProperties(i))) {
					Object o = ctx.getBean("testConfigModifyablePropertiesConditional");
					System.out.println("o: "+o);
				}
			}
		}
		
		private void test(Supplier<TestConfig> configSupplier, Function<Integer, String> expectedValueSupplier, boolean expectSame) {
			testForLoop(configSupplier, expectedValueSupplier, expectSame);
			testRecursive(configSupplier, expectedValueSupplier, expectSame);
		}

		private void testForLoop(Supplier<TestConfig> configSupplier, Function<Integer, String> expectedValueSupplier, boolean expectSame) {
			for ( int i = 0 ; i < 5 ; i++ ) {
				assertValue(configSupplier.get(), getInitialValue());
				try (ModifyablePropertySource x = ModifyablePropertySource.withProperties(getProperties(i))) {
					assertValues(configSupplier, expectedValueSupplier, expectSame, i);
				}
				assertValue(configSupplier.get(), getInitialValue());
			}
		}


		private void assertValues(Supplier<TestConfig> configSupplier, Function<Integer, String> expectedValueSupplier, boolean expectSame, int i) {
			TestConfig config1 = configSupplier.get();
			TestConfig config2 = configSupplier.get();
			assertValue(config1, expectedValueSupplier.apply(i));
			assertValue(config2, expectedValueSupplier.apply(i));
			if ( expectSame ) {
				Assert.assertSame(config1, config2);
			} else {
				Assert.assertNotSame(config1, config2);
			}
		}

		private void assertValue(TestConfig config, String value) {
			System.out.println(config+": "+config.getValue());
			Assert.assertEquals(value, config.getValue());
		}
		
		private void testRecursive(Supplier<TestConfig> configSupplier, Function<Integer, String> expectedValueSupplier, boolean expectSame) {
			assertValue(configSupplier.get(), getInitialValue());
			testRecursive(configSupplier, expectedValueSupplier, expectSame, 0);
			assertValue(configSupplier.get(), getInitialValue());
		}
		
		private void testRecursive(Supplier<TestConfig> configSupplier, Function<Integer, String> expectedValueSupplier, boolean expectSame, int i) {
			if ( i<5 ) {
				try (ModifyablePropertySource x = ModifyablePropertySource.withProperties(getProperties(i))) {
					assertValues(configSupplier, expectedValueSupplier, expectSame, i);
					testRecursive(configSupplier, expectedValueSupplier, expectSame, i+1);
				}
			}
		}
		
		private Map<String, Object> getProperties(int i) {
			Map<String, Object> p = new HashMap<>();
			p.put("abc.value", getValue(i));
			return p;
		}
		
		private static String getInitialValue() {
			return "test";
		}
		
		private String getValue(int i) {
			return "value"+i;
		}

		@Bean()
		@Scope("singleton")
		@ConfigurationProperties("abc")
		public TestConfig testConfigSingleton() {
			return new TestConfig();
		}
		
		@Bean()
		@Scope("prototype")
		@ConfigurationProperties("abc")
		public TestConfig testConfigPrototype() {
			return new TestConfig();
		}
		
		@Bean()
		@Scope(ModifyablePropertySourceScope.SCOPE_NAME)
		@ConfigurationProperties("abc")
		public TestConfig testConfigModifyableProperties() {
			return new TestConfig();
		}
		
		@Bean()
		@Scope(ModifyablePropertySourceScope.SCOPE_NAME)
		@ConfigurationProperties("abc")
		public Optional<TestConfig> testConfigModifyablePropertiesConditional(@Value("${abc.value}") String value) {
			return getValue(3).equals(value) ? Optional.of(new TestConfig()) : Optional.empty();
		}
		
		@Autowired ApplicationContext ctx;
		
		
		public static class TestConfig {
			private String value;

			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}
		}
	}
}
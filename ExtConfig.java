package com.atguigu.ext;

import java.util.concurrent.Executor;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

import com.atguigu.bean.Blue;

/**
 * ��չԭ��
 * BeanPostProcessor��bean���ô�������bean���������ʼ��ǰ��������ع�����
 * 
 * 1��BeanFactoryPostProcessor��beanFactory�ĺ��ô�������
 * 		��BeanFactory��׼��ʼ��֮����ã������ƺ��޸�BeanFactory�����ݣ�
 * 		���е�bean�����Ѿ�������ص�beanFactory������bean��ʵ����δ����
 * 
 */
 class AbstractApplicationContext:
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

		// Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
		// (e.g. through an @Bean method registered by ConfigurationClassPostProcessor)
		if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}
	}
	
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			//�������л�ȡ�����е�BeanDefinitionRegistryPostProcessor���
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		//ֱ����BeanFactory���ҵ�����������BeanFactoryPostProcessor���������ִ�����ǵķ���
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}
	
	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	//��������postProcessBeanFactory()����BeanFactoryPostProcessor��
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}
 
 
 * 
 * BeanFactoryPostProcessorԭ��:
 * 1)��ioc������������
 * 2)��invokeBeanFactoryPostProcessors(beanFactory);
 * 		����ҵ����е�BeanFactoryPostProcessor��ִ�����ǵķ�����
 * 			1����ֱ����BeanFactory���ҵ�����������BeanFactoryPostProcessor���������ִ�����ǵķ���
 * 			2�����ڳ�ʼ�������������ǰ��ִ��
 * 
 */
 
 class DefaultTestContext��
 //��ȡӦ�úϲ����������ģ���Щ����Ӧ��Ĭ��ʵ��CacheAwareContextLoaderDelegateί��
 public ApplicationContext getApplicationContext() {
		ApplicationContext context = this.cacheAwareContextLoaderDelegate.loadContext(this.mergedContextConfiguration);
		if (context instanceof ConfigurableApplicationContext) {
			@SuppressWarnings("resource")
			ConfigurableApplicationContext cac = (ConfigurableApplicationContext) context;
			Assert.state(cac.isActive(), () ->
					"The ApplicationContext loaded for [" + this.mergedContextConfiguration +
					"] is not active. This may be due to one of the following reasons: " +
					"1) the context was closed programmatically by user code; " +
					"2) the context was closed during parallel test execution either " +
					"according to @DirtiesContext semantics or due to automatic eviction " +
					"from the ContextCache due to a maximum cache size policy.");
		}
		return context;
	}
 
 class DefaultCacheAwareContextLoaderDelegate��
 
 @Override
	public ApplicationContext loadContext(MergedContextConfiguration mergedContextConfiguration) {
		synchronized (this.contextCache) {
			ApplicationContext context = this.contextCache.get(mergedContextConfiguration);
			if (context == null) {
				try {
					context = loadContextInternal(mergedContextConfiguration);
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("Storing ApplicationContext in cache under key [%s]",
								mergedContextConfiguration));
					}
					this.contextCache.put(mergedContextConfiguration, context);
				}
				catch (Exception ex) {
					throw new IllegalStateException("Failed to load ApplicationContext", ex);
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					//ʹ��key�ӻ����м���ApplicationContext
					logger.debug(String.format("Retrieved ApplicationContext from cache with key [%s]",
							mergedContextConfiguration));
				}
			}

			this.contextCache.logStatistics();

			return context;
		}
	}

 
 protected ApplicationContext loadContextInternal(MergedContextConfiguration mergedContextConfiguration)
			throws Exception {

		//��ȡ�����ļ�����
		ContextLoader contextLoader = mergedContextConfiguration.getContextLoader();
		Assert.notNull(contextLoader, "Cannot load an ApplicationContext with a NULL 'contextLoader'. " +
				"Consider annotating your test class with @ContextConfiguration or @ContextHierarchy.");

		ApplicationContext applicationContext;

		if (contextLoader instanceof SmartContextLoader) {
			SmartContextLoader smartContextLoader = (SmartContextLoader) contextLoader;
			applicationContext = smartContextLoader.loadContext(mergedContextConfiguration);
		}
		else {
			String[] locations = mergedContextConfiguration.getLocations();
			Assert.notNull(locations, "Cannot load an ApplicationContext with a NULL 'locations' array. " +
					"Consider annotating your test class with @ContextConfiguration or @ContextHierarchy.");
			applicationContext = contextLoader.loadContext(locations);
		}

		return applicationContext;
	}
 
 public class SpringBootContextLoader extends AbstractContextLoader {

	@Override
	public ApplicationContext loadContext(MergedContextConfiguration config)
			throws Exception {
		Class<?>[] configClasses = config.getClasses();
		String[] configLocations = config.getLocations();
		Assert.state(
				!ObjectUtils.isEmpty(configClasses)
						|| !ObjectUtils.isEmpty(configLocations),
				() -> "No configuration classes "
						+ "or locations found in @SpringApplicationConfiguration. "
						+ "For default configuration detection to work you need "
						+ "Spring 4.0.3 or better (found " + SpringVersion.getVersion()
						+ ").");
		SpringApplication application = getSpringApplication();
		application.setMainApplicationClass(config.getTestClass());
		application.addPrimarySources(Arrays.asList(configClasses));
		application.getSources().addAll(Arrays.asList(configLocations));
		ConfigurableEnvironment environment = getEnvironment();
		if (!ObjectUtils.isEmpty(config.getActiveProfiles())) {
			setActiveProfiles(environment, config.getActiveProfiles());
		}
		ResourceLoader resourceLoader = (application.getResourceLoader() != null)
				? application.getResourceLoader()
				: new DefaultResourceLoader(getClass().getClassLoader());
		TestPropertySourceUtils.addPropertiesFilesToEnvironment(environment,
				resourceLoader, config.getPropertySourceLocations());
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(environment,
				getInlinedProperties(config));
		application.setEnvironment(environment);
		List<ApplicationContextInitializer<?>> initializers = getInitializers(config,
				application);
		if (config instanceof WebMergedContextConfiguration) {
			application.setWebApplicationType(WebApplicationType.SERVLET);
			if (!isEmbeddedWebEnvironment(config)) {
				new WebConfigurer().configure(config, application, initializers);
			}
		}
		else if (config instanceof ReactiveWebMergedContextConfiguration) {
			application.setWebApplicationType(WebApplicationType.REACTIVE);
			if (!isEmbeddedWebEnvironment(config)) {
				new ReactiveWebConfigurer().configure(application);
			}
		}
		else {
			application.setWebApplicationType(WebApplicationType.NONE);
		}
		application.setInitializers(initializers);
		return application.run();
	}

 
 class SpringApplication��
 
 public ConfigurableApplicationContext run(String... args) {
	 //��ʾ���������ʱ��
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
		configureHeadlessProperty();
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting();
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(
					args);
			ConfigurableEnvironment environment = prepareEnvironment(listeners,
					applicationArguments);
			configureIgnoreBeanInfo(environment);
			Banner printedBanner = printBanner(environment);
			context = createApplicationContext();
			exceptionReporters = getSpringFactoriesInstances(
					SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
			prepareContext(context, environment, listeners, applicationArguments,
					printedBanner);
					//ˢ��������
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass)
						.logStarted(getApplicationLog(), stopWatch);
			}
			listeners.started(context);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}

		try {
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, null);
			throw new IllegalStateException(ex);
		}
		return context;
	}
 
 private void refreshContext(ConfigurableApplicationContext context) {
		refresh(context);
		if (this.registerShutdownHook) {
			try {
				context.registerShutdownHook();
			}
			catch (AccessControlException ex) {
				// Not allowed in some environments.
			}
		}
	}
 
 protected void refresh(ApplicationContext applicationContext) {
		Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
		((AbstractApplicationContext) applicationContext).refresh();
	}
 
 class AbstractApplicationContext��
 @Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// Prepare this context for refreshing.
			prepareRefresh();

			// Tell the subclass to refresh the internal bean factory.
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
			prepareBeanFactory(beanFactory);

			try {
				// Allows post-processing of the bean factory in context subclasses.
				postProcessBeanFactory(beanFactory);

				// Invoke factory processors registered as beans in the context.
				//��ʱbeanδ����ʼ����ֻ�Ǳ�������
				invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
				registerBeanPostProcessors(beanFactory);

				// Initialize message source for this context.
				initMessageSource();

				// Initialize event multicaster for this context.
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				onRefresh();

				// Check for listener beans and register them.
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				finishRefresh();
			}

			catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}

			finally {
				// Reset common introspection caches in Spring's core, since we
				// might not ever need metadata for singleton beans anymore...
				resetCommonCaches();
			}
		}
	}

	
//���δ������е�postProcessBeanDefinitionRegistry()����
private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
	}
	
public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}
 */
 
 * 2��BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor
 * 		postProcessBeanDefinitionRegistry();
 * 		������bean������Ϣ��Ҫ�����أ�beanʵ����δ�����ģ�
 * 
 * 		������BeanFactoryPostProcessorִ�У�
 * 		����BeanDefinitionRegistryPostProcessor���������ٶ������һЩ�����
 * 
 * 	ԭ��
 * 		1����ioc��������
 * 		2����refresh()-��invokeBeanFactoryPostProcessors(beanFactory);
 * 		3�����������л�ȡ�����е�BeanDefinitionRegistryPostProcessor�����
 * 			1�����δ������е�postProcessBeanDefinitionRegistry()����
 * 			2����������postProcessBeanFactory()����BeanFactoryPostProcessor��
 * 
 * 		4�����������������ҵ�BeanFactoryPostProcessor�����Ȼ�����δ���postProcessBeanFactory()����
 * 	
 * 3��ApplicationListener�����������з������¼����¼�����ģ�Ϳ�����
 * 	  public interface ApplicationListener<E extends ApplicationEvent>
 * 		���� ApplicationEvent ������������¼���
 * 
 * 	 ���裺
 * 		1����дһ����������ApplicationListenerʵ���ࣩ������ĳ���¼���ApplicationEvent�������ࣩ
 * 			@EventListener;
 * 			ԭ��ʹ��EventListenerMethodProcessor�����������������ϵ�@EventListener��
 * 
 * 		2�����Ѽ��������뵽������
 * 		3����ֻҪ������������¼��ķ��������Ǿ��ܼ���������¼���
 * 				ContextRefreshedEvent������ˢ����ɣ�����bean����ȫ�������ᷢ������¼���
 * 				ContextClosedEvent���ر������ᷢ������¼���
 * 		4��������һ���¼���
 * 				applicationContext.publishEvent()��
 
 class AbstractApplicationContext��
 //����ˢ����ɻᷢ��ContextRefreshedEvent�¼�
 protected void finishRefresh() {
		// Clear context-level resource caches (such as ASM metadata from scanning).
		clearResourceCaches();

		// Initialize lifecycle processor for this context.
		initLifecycleProcessor();

		// Propagate refresh to lifecycle processor first.
		getLifecycleProcessor().onRefresh();

		// Publish the final event.
		publishEvent(new ContextRefreshedEvent(this));

		// Participate in LiveBeansView MBean, if active.
		LiveBeansView.registerApplicationContext(this);
	}
	
	@Override
	public void publishEvent(ApplicationEvent event) {
		publishEvent(event, null);
	}
	
	protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
		Assert.notNull(event, "Event must not be null");

		// Decorate event as an ApplicationEvent if necessary
		ApplicationEvent applicationEvent;
		if (event instanceof ApplicationEvent) {
			applicationEvent = (ApplicationEvent) event;
		}
		else {
			applicationEvent = new PayloadApplicationEvent<>(this, event);
			if (eventType == null) {
				eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
			}
		}

		// Multicast right now if possible - or lazily once the multicaster is initialized
		if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add(applicationEvent);
		}
		else {
			
			//��ȡ�¼��Ķಥ�����ɷ�����,���¼����͸����������
			getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
		}

		// Publish event via parent context as well...
		if (this.parent != null) {
			if (this.parent instanceof AbstractApplicationContext) {
				((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
			}
			else {
				this.parent.publishEvent(event);
			}
		}
	}
	
	ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
					"call 'refresh' before multicasting events via the context: " + this);
		}
		return this.applicationEventMulticaster;
	}
	
	protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		
		//��ȥ����������û��id=��applicationEventMulticaster�������
		//,APPLICATION_EVENT_MULTICASTER_BEAN_NAME="applicationEventMulticaster"
		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
			this.applicationEventMulticaster =
					beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		}
		else {
			
			//���û���򴴽������Ҽ��뵽�����У����ǾͿ������������Ҫ�ɷ��¼����Զ�ע�����applicationEventMulticaster���Ҽ��뵽�����У�
			
			
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "' bean, using " +
						"[" + this.applicationEventMulticaster.getClass().getSimpleName() + "]");
			}
		}
	}
	
	protected void registerListeners() {
		// Register statically specified listeners first.
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let post-processors apply to them!
		//���������õ����еļ�������������ע�ᵽapplicationEventMulticaster��
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String listenerBeanName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
		}

		// Publish early application events now that we finally have a multicaster...
		Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
		this.earlyApplicationEvents = null;
		if (earlyEventsToProcess != null) {
			for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
				
				//��listenerע�ᵽApplicationEventMulticaster��
				
				getApplicationEventMulticaster().multicastEvent(earlyEvent);
			}
		}
	}
	
class SimpleApplicationEventMulticaster��
	@Override
	public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
		ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
		//��ȡ�����е�ApplicationListener
		for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
			//�����Executor������֧��ʹ��Executor�����첽�ɷ�
			Executor executor = getTaskExecutor();
			if (executor != null) {
				executor.execute(() -> invokeListener(listener, event));
			}
			else {
				//����ͬ���ķ�ʽֱ��ִ��listener����
				invokeListener(listener, event);
			}
		}
	}
	
	protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
		ErrorHandler errorHandler = getErrorHandler();
		if (errorHandler != null) {
			try {
				doInvokeListener(listener, event);
			}
			catch (Throwable err) {
				errorHandler.handleError(err);
			}
		}
		else {
			doInvokeListener(listener, event);
		}
	}
	
	private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
		try {
			//�õ�listener�ص�onApplicationEvent����
			listener.onApplicationEvent(event);
		}
		catch (ClassCastException ex) {
			String msg = ex.getMessage();
			if (msg == null || matchesClassCastMessage(msg, event.getClass())) {
				// Possibly a lambda-defined listener which we could not resolve the generic event type for
				// -> let's suppress the exception and just log a debug message.
				Log logger = LogFactory.getLog(getClass());
				if (logger.isTraceEnabled()) {
					logger.trace("Non-matching event type for listener: " + listener, ex);
				}
			}
			else {
				throw ex;
			}
		}
	}

 
 
 * 	
 *  ԭ��
 *  	ContextRefreshedEvent��IOCTest_Ext$1[source=�ҷ�����ʱ��]��ContextClosedEvent��
 *  1����ContextRefreshedEvent�¼���
 *  	1����������������refresh()��
 *  	2����finishRefresh();����ˢ����ɻᷢ��ContextRefreshedEvent�¼�
 *  2�����Լ������¼���
 *  3���������رջᷢ��ContextClosedEvent��
 *  
 *  ���¼��������̡���
 *  	3����publishEvent(new ContextRefreshedEvent(this));
 *  			1������ȡ�¼��Ķಥ�����ɷ�������getApplicationEventMulticaster()
 *  			2����multicastEvent�ɷ��¼���
 *  			3������ȡ�����е�ApplicationListener��
 *  				for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
 *  				1���������Executor������֧��ʹ��Executor�����첽�ɷ���
 *  					Executor executor = getTaskExecutor();
 *  				2��������ͬ���ķ�ʽֱ��ִ��listener������invokeListener(listener, event);
 *  				 �õ�listener�ص�onApplicationEvent������
 *  
 *  ���¼��ಥ�����ɷ�������
 *  	1����������������refresh();
 *  	2����initApplicationEventMulticaster();��ʼ��ApplicationEventMulticaster��
 *  		1������ȥ����������û��id=��applicationEventMulticaster���������
 *  		2�������û��this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
 *  			���Ҽ��뵽�����У����ǾͿ������������Ҫ�ɷ��¼����Զ�ע�����applicationEventMulticaster��
 *  
 *  ������������Щ��������
 *  	1����������������refresh();
 *  	2����registerListeners();
 *  		���������õ����еļ�������������ע�ᵽapplicationEventMulticaster�У�
 *  		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
 *  		//��listenerע�ᵽApplicationEventMulticaster��
 *  		getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
 *  		
 *   SmartInitializingSingleton ԭ��->afterSingletonsInstantiated();
 *   		1����ioc������������refresh()��
 *   		2����finishBeanFactoryInitialization(beanFactory);��ʼ��ʣ�µĵ�ʵ��bean��
 *   			1�����ȴ������еĵ�ʵ��bean��getBean();
 *   			2������ȡ���д����õĵ�ʵ��bean���ж��Ƿ���SmartInitializingSingleton���͵ģ�
 *   				����Ǿ͵���afterSingletonsInstantiated();
 * 		
 * 
 
 class AbstractApplicationContext��
 protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// Initialize conversion service for this context.
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
				beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
			beanFactory.setConversionService(
					beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
		}

		// Register a default embedded value resolver if no bean post-processor
		// (such as a PropertyPlaceholderConfigurer bean) registered any before:
		// at this point, primarily for resolution in annotation attribute values.
		if (!beanFactory.hasEmbeddedValueResolver()) {
			beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
		}

		// Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
		for (String weaverAwareName : weaverAwareNames) {
			getBean(weaverAwareName);
		}

		// Stop using the temporary ClassLoader for type matching.
		beanFactory.setTempClassLoader(null);

		// Allow for caching all bean definition metadata, not expecting further changes.
		beanFactory.freezeConfiguration();

		// Instantiate all remaining (non-lazy-init) singletons.
		beanFactory.preInstantiateSingletons();
	}

	
class DefaultListableBeanFactory:
	@Override
	public void preInstantiateSingletons() throws BeansException {
		if (logger.isTraceEnabled()) {
			logger.trace("Pre-instantiating singletons in " + this);
		}

		// Iterate over a copy to allow for init methods which in turn register new bean definitions.
		// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
		//��ȡ�����ж���õ�bean
		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

		// Trigger initialization of all non-lazy singleton beans...
		//�ȴ������еĵ�ʵ��bean
		for (String beanName : beanNames) {
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
				if (isFactoryBean(beanName)) {
					Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
					if (bean instanceof FactoryBean) {
						final FactoryBean<?> factory = (FactoryBean<?>) bean;
						boolean isEagerInit;
						if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
							isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
											((SmartFactoryBean<?>) factory)::isEagerInit,
									getAccessControlContext());
						}
						else {
							isEagerInit = (factory instanceof SmartFactoryBean &&
									((SmartFactoryBean<?>) factory).isEagerInit());
						}
						if (isEagerInit) {
							getBean(beanName);
						}
					}
				}
				else {
					getBean(beanName);
				}
			}
		}
		/*
		��ȡ���д����õĵ�ʵ��bean���ж��Ƿ���SmartInitializingSingleton���͵ģ�
 *   				����Ǿ͵���afterSingletonsInstantiated();
		*/
		 for (String beanName : beanNames) {
					Object singletonInstance = getSingleton(beanName);
					if (singletonInstance instanceof SmartInitializingSingleton) {
						final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
						if (System.getSecurityManager() != null) {
							AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
								smartSingleton.afterSingletonsInstantiated();
								return null;
							}, getAccessControlContext());
						}
						else {
							smartSingleton.afterSingletonsInstantiated();
						}
					}
				}
			}
 *
 class EventListenerMethodProcessor:
 @Override
	public void afterSingletonsInstantiated() {
		ConfigurableListableBeanFactory beanFactory = this.beanFactory;
		Assert.state(this.beanFactory != null, "No ConfigurableListableBeanFactory set");
		String[] beanNames = beanFactory.getBeanNamesForType(Object.class);
		for (String beanName : beanNames) {
			if (!ScopedProxyUtils.isScopedTarget(beanName)) {
				Class<?> type = null;
				try {
					type = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
				}
				catch (Throwable ex) {
					// An unresolvable bean type, probably from a lazy bean - let's ignore it.
					if (logger.isDebugEnabled()) {
						logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
					}
				}
				if (type != null) {
					if (ScopedObject.class.isAssignableFrom(type)) {
						try {
							Class<?> targetClass = AutoProxyUtils.determineTargetClass(
									beanFactory, ScopedProxyUtils.getTargetBeanName(beanName));
							if (targetClass != null) {
								type = targetClass;
							}
						}
						catch (Throwable ex) {
							// An invalid scoped proxy arrangement - let's ignore it.
							if (logger.isDebugEnabled()) {
								logger.debug("Could not resolve target bean for scoped proxy '" + beanName + "'", ex);
							}
						}
					}
					try {
						processBean(beanName, type);
					}
					catch (Throwable ex) {
						throw new BeanInitializationException("Failed to process @EventListener " +
								"annotation on bean with name '" + beanName + "'", ex);
					}
				}
			}
		}
	}
 */
@ComponentScan("com.atguigu.ext")
@Configuration
public class ExtConfig {
	
	@Bean
	public Blue blue(){
		return new Blue();
	}

}

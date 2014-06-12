package spring.trace.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ObjectUtils;
import spring.trace.TraceLogManager;
import spring.trace.TraceLogManagerImpl;

/**
 * spring.trace.TraceLogManager 가 등록되어 있지 않으면 기본으로 등록합니다.
 * @author holyeye
 */
@Configuration
public class TraceLogManagerConfiguration implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

    private static final Logger log = LoggerFactory.getLogger(TraceLogManagerConfiguration.class);

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        if (isRegisteredBean(TraceLogManager.class)) return;

        log.debug("register default traceLogManager");
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(TraceLogManagerImpl.class);
        registry.registerBeanDefinition("traceLogManager", beanDefinition);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    private boolean isRegisteredBean(Class<?> beanClass) {
        return !ObjectUtils.isEmpty(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, beanClass, true, false));
    }

}

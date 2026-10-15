package uk.gov.communities.prsdb.webapp.config

import org.ff4j.aop.FeatureAutoProxy
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.PriorityOrdered
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent

@PrsdbWebComponent
class FeatureFlagProxyBeanPostProcessor : BeanPostProcessor, PriorityOrdered {
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    override fun postProcessBeforeInitialization(
        bean: Any,
        beanName: String,
    ): Any {
        if (bean is FeatureAutoProxy) {
            bean.isProxyTargetClass = false
        }
        return bean
    }
}

package uk.gov.communities.prsdb.webapp.config

import org.ff4j.aop.FeatureAutoProxy
import org.junit.jupiter.api.Test
import org.springframework.core.Ordered
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

class FeatureFlagProxyBeanPostProcessorTests {
    private val processor = FeatureFlagProxyBeanPostProcessor()

    @Test
    fun `FF4J auto proxy creator uses interface proxies`() {
        val creator = FeatureAutoProxy().apply { isProxyTargetClass = true }

        assertSame(creator, processor.postProcessBeforeInitialization(creator, "ff.autoproxy"))
        assertFalse(creator.isProxyTargetClass)
    }

    @Test
    fun `unrelated beans are unchanged`() {
        val bean = Any()

        assertSame(bean, processor.postProcessBeforeInitialization(bean, "unrelated"))
    }

    @Test
    fun `processor runs before ordered auto proxy creators`() {
        assertEquals(Ordered.HIGHEST_PRECEDENCE, processor.getOrder())
    }
}

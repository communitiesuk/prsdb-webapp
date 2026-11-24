package uk.gov.communities.prsdb.webapp.config

import org.hibernate.envers.configuration.EnversSettings
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing
class AuditingConfig {
    @Bean
    fun enversPropertiesCustomizer() =
        HibernatePropertiesCustomizer { properties ->
            properties[EnversSettings.STORE_DATA_AT_DELETE] = true
        }
}

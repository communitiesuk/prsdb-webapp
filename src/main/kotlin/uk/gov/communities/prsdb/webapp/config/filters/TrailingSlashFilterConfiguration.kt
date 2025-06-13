package uk.gov.communities.prsdb.webapp.config.filters

import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.filter.UrlHandlerFilter
import uk.gov.communities.prsdb.webapp.annotations.WebConfiguration

@WebConfiguration
class TrailingSlashFilterConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun trailingSlashFilter() = UrlHandlerFilter.trailingSlashHandler("/**").redirect(HttpStatus.PERMANENT_REDIRECT).build()
}

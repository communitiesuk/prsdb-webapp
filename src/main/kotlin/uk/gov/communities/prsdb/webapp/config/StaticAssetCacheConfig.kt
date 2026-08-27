package uk.gov.communities.prsdb.webapp.config

import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.constants.ASSETS_PATH_SEGMENT
import java.time.Duration

@PrsdbWebConfiguration
class StaticAssetCacheConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
            .addResourceHandler("/$ASSETS_PATH_SEGMENT/fonts/**")
            .addResourceLocations("classpath:/static/$ASSETS_PATH_SEGMENT/fonts/")
            .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())

        registry
            .addResourceHandler("/$ASSETS_PATH_SEGMENT/**")
            .addResourceLocations("classpath:/static/$ASSETS_PATH_SEGMENT/")
            .setCacheControl(CacheControl.maxAge(Duration.ofSeconds(60)).cachePublic())
    }
}

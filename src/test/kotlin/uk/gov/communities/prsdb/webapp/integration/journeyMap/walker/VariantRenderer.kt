package uk.gov.communities.prsdb.webapp.integration.journeyMap.walker

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.ScreenshotType
import uk.gov.communities.prsdb.webapp.journeyMap.render.MermaidRenderer
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.min

data class CapturedImages(
    val image: String?,
    val thumbnail: String?,
    val mobileImage: String?,
)

fun interface VariantRenderer {
    fun capture(
        nodeId: String,
        variantNumber: Int,
    ): CapturedImages

    companion object {
        val NONE = VariantRenderer { _, _ -> CapturedImages(null, null, null) }

        fun safeName(id: String) = id.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_').ifEmpty { "page" }
    }
}

class ScreenshotRenderer(
    private val page: Page,
    private val outputDir: Path,
    private val mobile: Boolean,
) : VariantRenderer {
    override fun capture(
        nodeId: String,
        variantNumber: Int,
    ): CapturedImages {
        val folder = "img/${VariantRenderer.safeName(nodeId)}"
        Files.createDirectories(outputDir.resolve(folder))
        val image = "$folder/v$variantNumber.jpg"
        val thumbnail = "$folder/v$variantNumber.thumb.jpg"
        screenshot(outputDir.resolve(image))
        writeThumbnail(outputDir.resolve(image), outputDir.resolve(thumbnail))

        val mobileImage =
            if (mobile) {
                val desktopSize = page.viewportSize()
                page.setViewportSize(MOBILE_WIDTH, MOBILE_HEIGHT)
                try {
                    "$folder/v$variantNumber.mobile.jpg".also { screenshot(outputDir.resolve(it)) }
                } finally {
                    page.setViewportSize(desktopSize.width, desktopSize.height)
                }
            } else {
                null
            }
        return CapturedImages(image, thumbnail, mobileImage)
    }

    private fun screenshot(path: Path) {
        page.screenshot(
            Page
                .ScreenshotOptions()
                .setPath(path)
                .setFullPage(true)
                .setType(ScreenshotType.JPEG)
                .setQuality(JPEG_QUALITY),
        )
    }

    private fun writeThumbnail(
        source: Path,
        target: Path,
    ) {
        val width = MermaidRenderer.THUMBNAIL_WIDTH
        val height = MermaidRenderer.THUMBNAIL_HEIGHT
        val original = ImageIO.read(source.toFile())
        val visibleHeight = min(original.height, original.width * height / width)
        val thumbnail = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = thumbnail.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, width, height)
            graphics.drawImage(original, 0, 0, width, visibleHeight * width / original.width, 0, 0, original.width, visibleHeight, null)
        } finally {
            graphics.dispose()
        }
        ImageIO.write(thumbnail, "jpg", target.toFile())
    }

    companion object {
        private const val JPEG_QUALITY = 80
        private const val MOBILE_WIDTH = 375
        private const val MOBILE_HEIGHT = 812
    }
}

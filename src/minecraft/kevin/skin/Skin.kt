package kevin.skin

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.util.*

class Skin(val name: String,val image: BufferedImage) {
    val resource = ResourceLocation("kevin/skin/${name.lowercase(Locale.getDefault()).replace(" ", "_")}")
    init {
        val mc = Minecraft.getMinecraft()
        mc.addScheduledTask {
            mc.textureManager.loadTexture(resource, DynamicTexture(image))
        }
    }
}
package kevin.module.modules.misc

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.WorldEvent
import kevin.hud.element.elements.ConnectNotificationType
import kevin.hud.element.elements.Notification
import kevin.main.KevinClient
import kevin.module.BooleanValue
import kevin.module.ListValue
import kevin.module.Module
import kevin.utils.ChatUtils
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.pow
import kotlin.math.sqrt

class LagBackDetector : Module("LagBackDetector", "Detect lag back from server.") {
    private val countValue = BooleanValue("Count", true)
    private val distanceCheckValue = BooleanValue("DistanceCheck", true)
    private val modeValue = ListValue("Mode", arrayOf("Chat", "Notification"), "Notification")
    var count = 0
    override fun onDisable() {
        count = 0
    }

    override fun onEnable() {
        count = 0
    }

    @Suppress("UNUSED_PARAMETER")
    @EventTarget fun onWorld(event: WorldEvent) {
        count = 0
    }
    @EventTarget fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        mc.theWorld  ?: return
        val packet = if (event.packet is S08PacketPlayerPosLook) event.packet else return
        if (distanceCheckValue.get()) {
            if (sqrt((packet.x - mc.thePlayer.posX).pow(2) + (packet.z - mc.thePlayer.posZ).pow(2)) > 10) return
        }
        val message = "Flag detected${if (countValue.get()) ", count: ${++count}" else ""}"
        when (modeValue.get()) {
            "Chat" -> ChatUtils.messageWithStart("[LagBackDetector] $message")
            "Notification" -> KevinClient.hud.addNotification(Notification(message, "LagBackDetector", ConnectNotificationType.Warn))
        }
    }
}
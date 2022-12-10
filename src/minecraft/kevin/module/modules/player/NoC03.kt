package kevin.module.modules.player

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.module.Module
import kevin.module.ModuleCategory
import net.minecraft.network.play.client.C03PacketPlayer

class NoC03 : Module("NoC03", "Cancel C03 packets", category=ModuleCategory.PLAYER) {
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            if (packet.isMoving || packet.rotating || mc.thePlayer.isUsingItem) return
            event.cancelEvent()
        }
    }
}
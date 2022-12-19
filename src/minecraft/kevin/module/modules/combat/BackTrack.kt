package kevin.module.modules.combat

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.module.ModuleCategory
import net.minecraft.network.play.server.S00PacketKeepAlive
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S32PacketConfirmTransaction

class BackTrack: kevin.module.Module("BackTrack", "IN TEST", category = ModuleCategory.COMBAT) {
    @EventTarget fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet.javaClass.name.contains("net.minecraft.network.play.server.", true)) {
            if (packet is S14PacketEntity) {

            } else {

            }
        }
    }
}
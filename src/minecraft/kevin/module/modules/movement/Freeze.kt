/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kevin.module.modules.movement

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.UpdateEvent
import kevin.module.BooleanValue
import kevin.module.ListValue
import kevin.module.Module
import kevin.module.ModuleCategory
import net.minecraft.network.play.client.C03PacketPlayer

class Freeze : Module("Freeze", "Allows you to stay stuck in mid air.", category = ModuleCategory.MOVEMENT) {
    private val mode = ListValue("Mode", arrayOf("SetDead","NoMove"),"SetDead")
    private val resetMotionValue = BooleanValue("ResetMotion",false)
    private val lockRotation = BooleanValue("LockRotation",true)

    private var motionX = .0
    private var motionY = .0
    private var motionZ = .0
    private var rotationYaw = .0F
    private var rotationPitch = .0F

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer!!

        when(mode.get()){
            "SetDead" -> {
                thePlayer.isDead = true
                thePlayer.rotationYaw = thePlayer.cameraYaw
                thePlayer.rotationPitch = thePlayer.cameraPitch
            }
            "NoMove" -> {
                mc.thePlayer.motionX = .0
                mc.thePlayer.motionY = .0
                mc.thePlayer.motionZ = .0
                mc.thePlayer.speedInAir = .0F
            }
        }
    }
    @EventTarget fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer&&(packet is C03PacketPlayer.C05PacketPlayerLook || packet is C03PacketPlayer.C06PacketPlayerPosLook)) {
            if (!lockRotation.get()) return
            when(mode.get()){
                "NoMove" -> {
                    packet.yaw = rotationYaw
                    packet.pitch = rotationPitch
                    packet.rotating = false
                }
            }
        }
    }

    override fun onDisable() {
        when(mode.get()){
            "SetDead" -> mc.thePlayer?.isDead = false
            "NoMove" -> mc.thePlayer.speedInAir = .02F
        }
        if (resetMotionValue.get()) {
            mc.thePlayer.motionX = .0
            mc.thePlayer.motionY = .0
            mc.thePlayer.motionZ = .0
        } else {
            mc.thePlayer.motionX = motionX
            mc.thePlayer.motionY = motionY
            mc.thePlayer.motionZ = motionZ
        }
    }
    override fun onEnable() {
        val thePlayer = mc.thePlayer!!
        motionX = thePlayer.motionX
        motionY = thePlayer.motionY
        motionZ = thePlayer.motionZ
        rotationYaw = thePlayer.rotationYaw
        rotationPitch = thePlayer.rotationPitch
    }
}
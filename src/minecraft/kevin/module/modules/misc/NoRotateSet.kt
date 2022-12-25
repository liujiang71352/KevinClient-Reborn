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
package kevin.module.modules.misc

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.module.BooleanValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.RotationUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class NoRotateSet : Module("NoRotateSet", "Prevents the server from rotating your head.", category = ModuleCategory.MISC) {
    private val confirmValue = BooleanValue("Confirm", true)
    private val illegalRotationValue = BooleanValue("ConfirmIllegalRotation", false)
    private val noZeroValue = BooleanValue("NoZero", false)

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer ?: return

        if ((event.packet)is S08PacketPlayerPosLook) {
            val packet = event.packet

            if (noZeroValue.get() && packet.yaw == 0F && packet.pitch == 0F)
                return

            if (illegalRotationValue.get() || packet.pitch <= 90 && packet.pitch >= -90 &&
                RotationUtils.serverRotation != null && packet.yaw != RotationUtils.serverRotation.yaw &&
                packet.pitch != RotationUtils.serverRotation.pitch) {

                if (confirmValue.get())
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C05PacketPlayerLook(packet.yaw, packet.pitch, thePlayer.onGround))
            }

            packet.yaw = thePlayer.rotationYaw
            packet.pitch = thePlayer.rotationPitch
        }
    }

}
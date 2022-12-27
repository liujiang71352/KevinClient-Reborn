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
package kevin.module.modules.render

import kevin.event.EventTarget
import kevin.event.PacketEvent
import kevin.event.Render3DEvent
import kevin.main.KevinClient
import kevin.module.BooleanValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.module.modules.combat.KillAura
import kevin.utils.RotationUtils
import net.minecraft.network.play.client.C03PacketPlayer

class Rotations : Module("Rotations", description = "Allows you to see server-sided head and body rotations.", category = ModuleCategory.RENDER){
    private val bodyValue = BooleanValue("Body", true)

    private var playerYaw: Float? = null

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (RotationUtils.serverRotation != null && !bodyValue.get())
            mc.thePlayer?.rotationYawHead = RotationUtils.serverRotation.yaw
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer

        if (!bodyValue.get()/* || !shouldRotate()*/ || thePlayer == null)
            return

        val packet = event.packet

        if ((packet) is C03PacketPlayer.C06PacketPlayerPosLook || (packet) is C03PacketPlayer.C05PacketPlayerLook) {
            val packetPlayer = packet as C03PacketPlayer

            playerYaw = packetPlayer.yaw

            thePlayer.renderYawOffset = packetPlayer.yaw
            thePlayer.rotationYawHead = packetPlayer.yaw
        } else {
            if (playerYaw != null)
                thePlayer.renderYawOffset = this.playerYaw!!

            thePlayer.rotationYawHead = thePlayer.renderYawOffset
        }
    }

    private fun getState(module: String) = KevinClient.moduleManager.getModuleByName(module)!!.state

    private fun shouldRotate(): Boolean {
        val killAura = KevinClient.moduleManager.getModule(KillAura::class.java)
        return (getState("KillAura") && (killAura.target != null || killAura.sTarget != null))
                || getState("Scaffold") || getState("Breaker") || getState("Nuker")/**getState(Tower::class.java) ||
                getState(BowAimbot::class.java) ||
                 || getState(CivBreak::class.java)  ||
                getState(ChestAura::class.java)**/
    }
}
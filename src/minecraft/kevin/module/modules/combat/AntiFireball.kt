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
package kevin.module.modules.combat

import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.module.*
import kevin.utils.MSTimer
import kevin.utils.RotationUtils
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation

class AntiFireball : Module("AntiFireball", "", category = ModuleCategory.COMBAT) {
    private val timer = MSTimer()

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val rotationValue = BooleanValue("Rotation", true)
    private val radius = FloatValue("Radius", 3f, 3f, 6f)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityFireball && mc.thePlayer.getDistanceToEntity(entity) < radius.get() && timer.hasTimePassed(300)) {
                if(rotationValue.get()) {
                    RotationUtils.setTargetRotation(RotationUtils.getRotations(entity))
                }

                if (swingValue.get() == "Normal") {
                    mc.thePlayer.swingItem()
                } else if (swingValue.get() == "Packet") {
                    mc.netHandler.addToSendQueue(C0APacketAnimation())
                }
                mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

                timer.reset()
                break
            }
        }
    }
}
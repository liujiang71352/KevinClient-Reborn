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
package kevin.module.modules.movement.flys.other

import kevin.event.BlockBBEvent
import kevin.event.UpdateEvent
import kevin.module.ListValue
import kevin.module.modules.movement.flys.FlyMode
import kevin.utils.MovementUtils
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.AxisAlignedBB
import kotlin.math.cos
import kotlin.math.sin

object Buzz : FlyMode("Buzz") {
    private val mode = ListValue("BuzzMode", arrayOf("test1", "test2", "test3", "test4"), "test1")
    private var boost = false

    override fun onEnable() {
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.42, mc.thePlayer.posZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1E-12, mc.thePlayer.posZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        boost = false
        if (mode equal "test3") {
            MovementUtils.forward(3.0)
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer!!
        when (mode.get()) {
            "test1" -> if (thePlayer.hurtTime > 0) MovementUtils.strafe(fly.speed.get())
            "test2" -> {
                if (thePlayer.onGround) thePlayer.jump()
                if (thePlayer.hurtTime > 8) {
                    thePlayer.motionY = 0.73
                    MovementUtils.strafe(fly.speed.get())
                }
            }
            "test3" -> {
                if (boost) {
                    MovementUtils.strafe(MovementUtils.speed * 1.08F)
                } else {
                    MovementUtils.setMotion(0.0)
                    thePlayer.setPosition(thePlayer.posX, thePlayer.posY, thePlayer.posZ)
                    if (thePlayer.hurtTime > 8) {
                        boost = true
                        MovementUtils.strafe(fly.speed.get())
                    }
                }
            }
            "test4" -> {
                if (thePlayer.hurtTime > 8) {
                    thePlayer.motionY = 0.0
                    thePlayer.motionX = 0.0
                    thePlayer.motionZ = 0.0
                    val yaw = Math.toRadians(thePlayer.rotationYaw.toDouble())
                    thePlayer.setPosition(thePlayer.posX + -sin(yaw) * fly.speed.get(), fly.launchY + 3.8, thePlayer.posZ + cos(yaw) * fly.speed.get())
                }
                if (thePlayer.onGround) thePlayer.jump()
            }
        }
    }
    override fun onBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}
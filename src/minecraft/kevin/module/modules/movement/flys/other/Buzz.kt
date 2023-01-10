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
import kevin.event.PacketEvent
import kevin.event.UpdateEvent
import kevin.module.ListValue
import kevin.module.modules.movement.flys.FlyMode
import kevin.utils.MovementUtils
import net.minecraft.block.BlockAir
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S00PacketKeepAlive
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.optifine.util.LinkedList
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.cos
import kotlin.math.sin

object Buzz : FlyMode("Buzz") {
    private val mode = ListValue("BuzzMode", arrayOf("test1", "test2", "test3", "test4", "test5"), "test1")
    private var boost = false
    private val packets = LinkedBlockingQueue<Packet<*>>()
    private val serverPackets = LinkedBlockingQueue<Packet<*>>()
    private var speed = 1.237F
    private var enabledTicks = 0

    override fun onEnable() {
        if (mode equal "test5") {
            if (!mc.thePlayer.onGround) fly.state = false
            speed = if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                2.001542F
            } else {
                1.62F
            } * MovementUtils.getBaseMoveSpeed().toFloat()
            return
        }
        if (!mc.thePlayer.onGround) return
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
            "test5" -> {
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
                MovementUtils.strafe(speed)
                speed -= speed / 128f
                mc.thePlayer.motionY = 0.0
                if (enabledTicks == 1) {
                    speed *= 2.16F
                    speed *= speed * if (mc.thePlayer.ticksExisted % 2 == 0) 1.575F else 1.725F
                }
                if (enabledTicks == 5) {
                    mc.thePlayer.handleStatusUpdate(2)
                    mc.thePlayer.hurtTime = 0
                }
            }
        }
        ++enabledTicks
    }

    override fun onDisable() {
        super.onDisable()
        mc.thePlayer.motionX *= 0.6
        mc.thePlayer.motionZ *= 0.6
        mc.thePlayer.motionY = 0.0
        while (packets.isNotEmpty()) {
            mc.netHandler.addToSendQueue(packets.take())
        }
        while (serverPackets.isNotEmpty()) {
            @Suppress("UNCHECKED_CAST")
            (serverPackets.take() as Packet<INetHandlerPlayClient>).processPacket(mc.netHandler)
        }
    }

    override fun onPacket(event: PacketEvent) {
        if (!(mode equal "test5")) return
        if (event.isCancelled) return
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            event.cancelEvent()
            if (packet.isMoving) {
                if (enabledTicks > 1) {
                    packet.onGround = false
                }
                if (mc.thePlayer.ticksExisted % 3 != 0) packets.add(packet)
            }
        } else if (packet is C0APacketAnimation || packet is C02PacketUseEntity || packet is C07PacketPlayerDigging || packet is C08PacketPlayerBlockPlacement || packet is C0BPacketEntityAction) {
            event.cancelEvent()
        } else if (packet is S32PacketConfirmTransaction || packet is S00PacketKeepAlive || packet is S12PacketEntityVelocity) {
            event.cancelEvent()
            serverPackets.add(packet)
        }
    }

    override fun onBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}
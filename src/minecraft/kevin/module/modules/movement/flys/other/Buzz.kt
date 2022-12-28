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
                    MovementUtils.strafe(MovementUtils.speed * 1.025F)
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
                    thePlayer.motionY = 0.42
                    thePlayer.motionX = 0.0
                    thePlayer.motionZ = 0.0
                    val yaw = Math.toRadians(thePlayer.rotationYaw.toDouble())
                    thePlayer.setPosition(thePlayer.posX + -sin(yaw) * fly.speed.get(), fly.launchY + 3.5, thePlayer.posZ + cos(yaw) * fly.speed.get())
                }
            }
        }
    }
    override fun onBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}
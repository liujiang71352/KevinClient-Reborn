package kevin.module.modules.movement.flys.other

import kevin.event.MoveEvent
import kevin.event.UpdateEvent
import kevin.module.modules.movement.flys.FlyMode
import kotlin.math.cos
import kotlin.math.sin

@Suppress("SameParameterValue")
object Sparky : FlyMode("Sparky") {
    private var state = 0
    override fun onEnable() {
        state = -2
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        mc.timer.timerSpeed = 0.1f
        if (++state > 1) {
            if (mc.gameSettings.keyBindJump.isKeyDown) {
                vClip(9.0)
            } else if (mc.gameSettings.keyBindSneak.isKeyDown) {
                vClip(5.0)
            } else {
                hClip(5.0)
            }
            mc.timer.timerSpeed = 1f
            state = 0
        }
    }

    override fun onDisable() {
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        mc.timer.timerSpeed = 1f
    }

    override fun onMove(event: MoveEvent) {
        event.zero()
    }

    private fun hClip(d: Double) {
        val playerYaw = mc.thePlayer.rotationYaw * Math.PI / 180
        mc.thePlayer.setPosition(mc.thePlayer.posX + d * -sin(playerYaw), mc.thePlayer.posY, mc.thePlayer.posZ + d * cos(playerYaw))
    }
    private fun vClip(d: Double) {
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + d, mc.thePlayer.posZ)
    }
}
package kevin.module.modules.movement.speeds.other

import kevin.module.modules.movement.speeds.SpeedMode
import kevin.utils.MovementUtils

object IntaveHop: SpeedMode("IntaveHop") { // from FDP

    private var jumpTicks = 0

    override fun onPreMotion() {
        jumpTicks++
        if (!MovementUtils.isMoving) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            jumpTicks = 0
            return
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving) {
            MovementUtils.strafe(0.588f)
            mc.thePlayer.jump()
            jumpTicks = 0
        } else if (jumpTicks == 5) {
            mc.thePlayer.motionY = -0.0784000015258789
        }
        MovementUtils.strafe()
    }
}
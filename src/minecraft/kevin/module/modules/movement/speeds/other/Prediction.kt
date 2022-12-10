package kevin.module.modules.movement.speeds.other

import kevin.module.modules.movement.speeds.SpeedMode
import kevin.utils.MovementUtils

object Prediction : SpeedMode("Prediction") {
    override fun onPreMotion() {
        if (!MovementUtils.isMoving
            || mc.thePlayer.isInLava
            || mc.thePlayer.isInWater
            || mc.thePlayer.inWeb
            || mc.thePlayer.isOnLadder) {
            return
        }
        if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown)
            mc.thePlayer.jump()

        if (mc.thePlayer.motionY >= 0 && mc.thePlayer.motionY < 9.0E-4) {
            mc.thePlayer.motionY = (mc.thePlayer.motionY - 0.08) * 0.98F
        }
    }
}
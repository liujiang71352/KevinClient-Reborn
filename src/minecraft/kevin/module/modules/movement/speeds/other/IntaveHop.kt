package kevin.module.modules.movement.speeds.other

import kevin.module.modules.movement.speeds.SpeedMode
import kevin.utils.MovementUtils

object IntaveHop: SpeedMode("IntaveHop") { // from Rise

    private var offGroundTicks = 0

    override fun onPreMotion() {
        offGroundTicks++
        if (mc.thePlayer.onGround) {
            MovementUtils.strafe((MovementUtils.getBaseMoveSpeed() * 1.2).toFloat());
            offGroundTicks = 0
            mc.thePlayer.jump();
        } else if (offGroundTicks == 4) {
            mc.thePlayer.motionY = -0.0784000015258789;
        }

        MovementUtils.strafe();
    }
}
package kevin.module.modules.movement.speeds.other

import kevin.module.BooleanValue
import kevin.module.modules.movement.speeds.SpeedMode
import kevin.utils.MovementUtils

object IntaveHop: SpeedMode("IntaveHop") { // Rise base
    private val strafeValue = BooleanValue("IntaveStrafe", true)
    private var offGroundTicks = 0

    override fun onPreMotion() {
//        offGroundTicks++
        if (mc.thePlayer.onGround) {
            MovementUtils.strafe((MovementUtils.getBaseMoveSpeed() * 1.2).toFloat())
            offGroundTicks = 0
//            mc.thePlayer.jump()
        } else if (strafeValue.get()) {
            MovementUtils.strafe()
        }
//        else if (offGroundTicks == 4) { mc.thePlayer.motionY = -0.0784000015258789 }
    }
}
package kevin.module.modules.player.nofalls.normal

import kevin.event.UpdateEvent
import kevin.module.modules.player.nofalls.NoFallMode
import kevin.utils.FallingPlayer

object LegitNoFall: NoFallMode("Legit") {
    var working = false
    override fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return
        if (mc.thePlayer.fallDistance > 3) {
            val fallingPlayer = FallingPlayer(
                thePlayer.posX,
                thePlayer.posY,
                thePlayer.posZ,
                thePlayer.motionX,
                thePlayer.motionY,
                thePlayer.motionZ,
                thePlayer.rotationYaw,
                thePlayer.moveStrafing,
                thePlayer.moveForward
            )

            if (fallingPlayer.findCollision(10) != null) {
                working = true
            }
        }
        if (mc.thePlayer.onGround && working && mc.thePlayer.isSneaking) {
            mc.thePlayer.isSneaking = false
            working = false
        }
        if (working) mc.thePlayer.isSneaking = true
    }
}
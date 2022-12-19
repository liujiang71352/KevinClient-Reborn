package kevin.module.modules.combat

import kevin.event.AttackEvent
import kevin.event.EventTarget
import kevin.event.StrafeEvent
import kevin.event.UpdateEvent
import kevin.module.*
import kevin.utils.getDistanceToEntityBox
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.player.EntityPlayer

class KeepRange: Module("KeepDistance", "Keep yourself out of a range with your target", category=ModuleCategory.COMBAT) {
    private val mode = ListValue("Mode", arrayOf("ReleaseKey", "CancelMove"), "ReleaseKey")
    private val minDistance = FloatValue("MinDistance", 2.3F, 0F, 4F)
    private val maxDistance = FloatValue("MaxDistance", 4.0F, 3F, 7F)
    private val onlyForward = BooleanValue("OnlyForward", true)
    var target: EntityPlayer? = null
    private val binds = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindLeft
    )

    @EventTarget fun onAttack(event: AttackEvent) {
        target = if (event.targetEntity is EntityPlayer) event.targetEntity else return
    }
    @EventTarget fun onStrafe(event: StrafeEvent) {
        if (mode equal "CancelMove") {
            target?.let {
                if (mc.thePlayer.getDistanceToEntityBox(it) <= minDistance.get()) {
                    if (!onlyForward.get() || event.forward == 1.0F) {
                        event.cancelEvent()
                    }
                }
            }
        }
    }
    @EventTarget fun onUpdate(event: UpdateEvent) {
        if (target == null) return
        val distance = mc.thePlayer.getDistanceToEntityBox(target!!)
        if (target!!.isDead || distance >= maxDistance.get()) {
            target = null
            return
        }
        if (distance <= minDistance.get()) {
            if (onlyForward.get()) mc.gameSettings.keyBindForward.pressed = false
            else for (bind in binds) bind.pressed = false
        }
    }
}
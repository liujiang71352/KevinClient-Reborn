package kevin.module.modules.combat

import kevin.event.EventState
import kevin.event.EventTarget
import kevin.event.MotionEvent
import kevin.main.KevinClient
import kevin.module.*
import kevin.module.Module
import kevin.utils.expands
import kevin.utils.getNearestPointBB
import net.minecraft.entity.player.EntityPlayer

/* Modified class:
 * Minecraft.java
 */
object TimerRange: Module("TimerRange", "(IN TEST) Make you walk to target faster", category = ModuleCategory.COMBAT) {
    private val minDistance = FloatValue("MinDistance", 3F, 0F, 4F)
    private val maxDistance = FloatValue("MaxDistance", 4F, 3F, 7F)
    private val maxTimeValue = IntegerValue("MaxTime", 3, 1, 20)
    private val delayValue = IntegerValue("Delay", 5, 0, 20)
    private val onlyKillAura = BooleanValue("OnlyKillAura", true)
    private val onlyPlayer = BooleanValue("OnlyPlayer", true)
    private val killAura: KillAura by lazy { KevinClient.moduleManager.getModule(KillAura::class.java) }

    @JvmStatic
    private var working = false
    private var cooldown = 0
    private var freezeTicks = 0
    private var needStop = false

    @EventTarget fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) return
        val thePlayer = mc.thePlayer ?: return
        if (onlyKillAura.get() && !killAura.state) return
        val entityList = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            thePlayer,
            thePlayer.entityBoundingBox.expands(maxDistance.get() + 1.0)
        )
        if (entityList.isNotEmpty()) {
            val vecEyes = thePlayer.getPositionEyes(1f)
            var targetFound = false
            var targetInRange = false
            for (entity in entityList) {
                if (onlyPlayer.get() && entity !is EntityPlayer) continue
                val nearest = getNearestPointBB(vecEyes, entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())).distanceTo(vecEyes)
                if (nearest <= minDistance.get()) {
                    targetInRange = true
                    break
                } else if (nearest <= maxDistance.get()) {
                    targetFound = true
                }
            }
            if (targetInRange) {
                needStop = true
            } else if (targetFound) {
                haveTarget()
            }
        }
    }

    fun haveTarget() {
        if (cooldown > 0 || freezeTicks > 0) return
        cooldown = delayValue.get()
        working = true
        freezeTicks = 0
        while (freezeTicks <= maxTimeValue.get() && !needStop) {
            ++freezeTicks
            mc.runTick()
        }
        needStop = false
        working = false
    }

    @JvmStatic
    fun handleTick(): Boolean {
        if (working) return true
        if (state && freezeTicks > 0) {
            --freezeTicks
            return true
        }
        if (cooldown > 0) --cooldown
        return false
    }

    @JvmStatic
    fun freezeAnimation(): Boolean = freezeTicks > 0
}
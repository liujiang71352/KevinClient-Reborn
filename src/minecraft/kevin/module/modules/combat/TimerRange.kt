package kevin.module.modules.combat

import kevin.event.EventTarget
import kevin.event.MotionEvent
import kevin.main.KevinClient
import kevin.module.*
import kevin.module.Module
import kevin.utils.expands
import kevin.utils.getNearestPointBB

/* Modified class:
 * Minecraft.java
 */
object TimerRange: Module("TimerRange", "(IN TEST) Make you walk to target faster", category = ModuleCategory.COMBAT) {
    private val minDistance = FloatValue("MinDistance", 3F, 0F, 4F)
    private val maxDistance = FloatValue("MaxDistance", 4F, 3F, 7F)
    private val maxTimeValue = IntegerValue("MaxTime", 3, 1, 20)
    private val delayValue = IntegerValue("Delay", 5, 0, 20)
    private val onlyKillAura = BooleanValue("OnlyKillAura", true)
    private val killAura: KillAura by lazy { KevinClient.moduleManager.getModule(KillAura::class.java) }

    @EventTarget fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return
        if (onlyKillAura.get() && !killAura.state) return
        val entityList = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            thePlayer,
            thePlayer.entityBoundingBox.expands(maxDistance.get() + 1.0)
        )
        if (entityList.isNotEmpty()) {
            val vecEyes = thePlayer.getPositionEyes(1f)
            var targetFound = false
            for (entity in entityList) {
                val nearest = getNearestPointBB(vecEyes, entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())).distanceTo(vecEyes)
                if (nearest in minDistance.get()..maxDistance.get()) {
                    targetFound = true
                    break
                }
            }
            if (targetFound) {
                haveTarget()
            }
        }
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
    fun haveTarget() {
        if (cooldown > 0 || freezeTicks > 0) return
        cooldown = delayValue.get()
        working = true
        freezeTicks = 0
        while (freezeTicks <= maxTimeValue.get()) {
            ++freezeTicks
            mc.runTick()
        }
        working = false
    }
    private var cooldown = 0
    private var freezeTicks = 0
    @JvmStatic
    private var working = false
}
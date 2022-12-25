package kevin.module.modules.combat

import kevin.event.EventState
import kevin.event.EventTarget
import kevin.event.MotionEvent
import kevin.main.KevinClient
import kevin.module.*
import kevin.module.Module
import kevin.utils.RaycastUtils
import kevin.utils.expands
import kevin.utils.getNearestPointBB
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import kotlin.math.min

/* Modified class:
 * Minecraft.java
 */
object TimerRange: Module("TimerRange", "(IN TEST) Make you walk to target faster", category = ModuleCategory.COMBAT) {
    private val mode = ListValue("Mode", arrayOf("RayCast", "Radius"), "RayCast")
    private val minDistance = FloatValue("MinDistance", 3F, 0F, 4F)
    private val maxDistance = FloatValue("MaxDistance", 4F, 3F, 7F)
    private val maxTimeValue = IntegerValue("MaxTime", 3, 1, 20)
    private val delayValue = IntegerValue("Delay", 5, 0, 20)
    private val onlyKillAura = BooleanValue("OnlyKillAura", true)
    private val auraClick = BooleanValue("AuraClick", true)
    private val onlyPlayer = BooleanValue("OnlyPlayer", true)
    private val killAura: KillAura by lazy { KevinClient.moduleManager.getModule(KillAura::class.java) }

    @JvmStatic
    private var working = false
    private var stopWorking = false
    private var lastNearest = 10.0
    private var cooldown = 0
    private var freezeTicks = 0

    @EventTarget fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) return
        val thePlayer = mc.thePlayer ?: return
        if (onlyKillAura.get() && !killAura.state) return
        if (mode equal "RayCast") {
            val entity = RaycastUtils.raycastEntity(maxDistance.get() + 1.0, object : RaycastUtils.EntityFilter {
                override fun canRaycast(entity: Entity?): Boolean {
                    return entity != null && entity is EntityLivingBase && (!onlyPlayer.get() || entity is EntityPlayer)
                }
            })
            if (entity == null) {
                lastNearest = 10.0
                return
            }
            val vecEyes = thePlayer.getPositionEyes(1f)
            val range = getNearestPointBB(
                vecEyes,
                entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())
            ).distanceTo(vecEyes)
            if (range < minDistance.get()) {
                stopWorking = true
            } else if (range <= maxDistance.get() && range < lastNearest) {
                stopWorking = false
                foundTarget()
            }
            lastNearest = range
        } else {
            val entityList = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                thePlayer,
                thePlayer.entityBoundingBox.expands(maxDistance.get() + 1.0)
            )
            if (entityList.isNotEmpty()) {
                val vecEyes = thePlayer.getPositionEyes(1f)
                var targetFound = false
                var targetInRange = false
                var nearest = 10.0
                for (entity in entityList) {
                    if (onlyPlayer.get() && entity !is EntityPlayer) continue
                    val range = getNearestPointBB(
                        vecEyes,
                        entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())
                    ).distanceTo(vecEyes)
                    if (range < minDistance.get()) {
                        targetInRange = true
                        break
                    } else if (range <= maxDistance.get()) {
                        targetFound = true
                    }
                    nearest = min(nearest, range)
                }
                if (targetInRange) {
                    stopWorking = true
                } else if (targetFound && nearest < lastNearest) {
                    stopWorking = false
                    foundTarget()
                }
                lastNearest = nearest
            } else {
                lastNearest = 10.0
            }
        }
    }

    fun foundTarget() {
        if (cooldown > 0 || freezeTicks != 0) return
        cooldown = delayValue.get()
        working = true
        freezeTicks = 0
        while (freezeTicks <= maxTimeValue.get() && !stopWorking) {
            ++freezeTicks
            mc.runTick()
        }
        if (auraClick.get()) {
            killAura.clicks++
            ++freezeTicks
            mc.runTick()
        }
        stopWorking = false
        working = false
    }

    @JvmStatic
    fun handleTick(): Boolean {
        if (working || freezeTicks < 0) return true
        if (state && freezeTicks > 0) {
            --freezeTicks
            return true
        }
        if (cooldown > 0) --cooldown
        return false
    }

    @JvmStatic
    fun freezeAnimation(): Boolean = freezeTicks != 0
}
/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kevin.module.modules.combat

import kevin.event.EventState
import kevin.event.EventTarget
import kevin.event.MotionEvent
import kevin.main.KevinClient
import kevin.module.*
import kevin.module.Module
import kevin.utils.ChatUtils
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
    private val minDistance: FloatValue = object : FloatValue("MinDistance", 3F, 0F, 4F) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > maxDistance.get()) set(maxDistance.get())
        }
    }
    private val maxDistance: FloatValue = object : FloatValue("MaxDistance", 4F, 3F, 7F) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue < minDistance.get()) set(minDistance.get())
        }
    }
    private val rangeMode = ListValue("RangeMode", arrayOf("Setting", "Smart"), "Smart")
    private val maxTimeValue = IntegerValue("MaxTime", 3, 1, 20)
    private val delayValue = IntegerValue("Delay", 5, 0, 20)
    private val maxHurtTimeValue = IntegerValue("TargetMaxHurtTime", 2, 0, 10)
    private val onlyKillAura = BooleanValue("OnlyKillAura", true)
    private val auraClick = BooleanValue("AuraClick", true)
    private val onlyPlayer = BooleanValue("OnlyPlayer", true)
    private val debug = BooleanValue("Debug", false)
    private val betterAnimation = BooleanValue("BetterAnimation", true)
//    private val bypassValue = BooleanValue("Test", false)

    private val killAura: KillAura by lazy { KevinClient.moduleManager.getModule(KillAura::class.java) }

    @JvmStatic
    private var working = false
    private var stopWorking = false
    private var lastNearest = 10.0
    private var cooldown = 0
    private var freezeTicks = 0
    private var bypassTick = 0
    private var firstAnimation = true

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
            if (entity == null || entity !is EntityLivingBase) {
                lastNearest = 10.0
                return
            }
            val vecEyes = thePlayer.getPositionEyes(1f)
            val box = getNearestPointBB(
                vecEyes,
                entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())
            )
            val range = box.distanceTo(vecEyes)
            val predictEyes = if (rangeMode equal "Smart") {
                thePlayer.getPositionEyes(maxTimeValue.get() + 1f)
            } else thePlayer.getPositionEyes(3f)
            val afterRange = box.distanceTo(predictEyes)
            if (range < minDistance.get()) {
                stopWorking = true
            } else if (((rangeMode equal "Smart" && range > minDistance.get() && afterRange < minDistance.get() && afterRange < range) || (rangeMode equal "Setting" && range <= maxDistance.get() && range < lastNearest && afterRange < range)) && entity.hurtTime <= maxHurtTimeValue.get()) {
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
                val afterEyes = if (rangeMode equal "Smart") {
                    thePlayer.getPositionEyes(maxTimeValue.get() + 1f)
                } else thePlayer.getPositionEyes(3f)
                var targetFound = false
                var targetInRange = false
                var nearest = 10.0
                for (entity in entityList) {
                    if (entity !is EntityLivingBase) continue
                    if (onlyPlayer.get() && entity !is EntityPlayer) continue
                    val box = getNearestPointBB(
                        vecEyes,
                        entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())
                    )
                    val range = box.distanceTo(vecEyes)
                    val afterRange = box.distanceTo(afterEyes)
                    if (range < minDistance.get()) {
                        targetInRange = true
                        break
                    } else if (range <= maxDistance.get() && afterRange < range && entity.hurtTime <= maxHurtTimeValue.get()) {
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
        if (betterAnimation.get()) firstAnimation = false
        while (freezeTicks <= maxTimeValue.get() && !stopWorking) {
            ++freezeTicks
            mc.runTick()
        }
        if (debug.get()) ChatUtils.messageWithStart("Timed")
        if (auraClick.get()) {
            killAura.clicks++
            ++freezeTicks
            mc.runTick()
            if (debug.get()) ChatUtils.messageWithStart("Clicked")
        }
        stopWorking = false
        working = false
    }

    @JvmStatic
    fun handleTick(): Boolean {
        if (bypassTick == 1) {
            ++bypassTick
            return false
        }
        if (working || freezeTicks < 0) return true
        if (state && freezeTicks > 0) {
            --freezeTicks
            return true
        }
        if (cooldown > 0) --cooldown
        return false
    }

    @JvmStatic
    fun freezeAnimation(): Boolean {
        if (freezeTicks != 0) {
            if (!firstAnimation) {
                firstAnimation = true
                return false
            }
            return true
        }
        return false
    }
}
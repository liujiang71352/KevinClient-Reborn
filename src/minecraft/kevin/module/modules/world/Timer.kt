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
package kevin.module.modules.world

import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.event.WorldEvent
import kevin.module.BooleanValue
import kevin.module.FloatValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.MovementUtils

class Timer : Module("Timer", "Changes the speed of the entire game.", category = ModuleCategory.WORLD) {
    private val speedValue = FloatValue("Speed", 2F, 0.1F, 30F)
    private val onMoveValue = BooleanValue("OnlyOnMove", true)
    //private val autoDisable = BooleanValue("AutoDisable",true)

    override fun onDisable() {
        if (mc.thePlayer == null)
            return
        mc.timer.timerSpeed = 1F
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(MovementUtils.isMoving || !onMoveValue.get()) {
            mc.timer.timerSpeed = speedValue.get()
            return
        }
        mc.timer.timerSpeed = 1F
    }
/**
    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (event.worldClient != null) return
        if (!autoDisable.get()) return
        this.toggle(false)
    }
**/
    override val tag: String
        get() = "Speed:${speedValue.get()}"
}
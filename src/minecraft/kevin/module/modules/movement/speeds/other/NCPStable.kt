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
package kevin.module.modules.movement.speeds.other

import kevin.event.UpdateEvent
import kevin.module.modules.movement.speeds.SpeedMode
import kevin.utils.MovementUtils
import kotlin.math.max

object NCPStable : SpeedMode("NCPStable") { // from FDP
    override fun onUpdate(event: UpdateEvent) {
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.onGround) mc.thePlayer.jump()
            MovementUtils.strafe(max(MovementUtils.speed.toDouble(), MovementUtils.getMoveSpeed(0.27)).toFloat())
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
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

import kevin.module.modules.movement.speeds.SpeedMode
import kevin.utils.MovementUtils
import kotlin.math.abs

object Prediction : SpeedMode("Prediction") {
    override fun onPreMotion() {
        if (!MovementUtils.isMoving
            || mc.thePlayer.isInLava
            || mc.thePlayer.isInWater
            || mc.thePlayer.inWeb
            || mc.thePlayer.isOnLadder) {
            return
        }
        if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown)
            mc.thePlayer.jump()

        if (abs(mc.thePlayer.motionY) < 0.005) {
            mc.thePlayer.motionY = (mc.thePlayer.motionY - 0.08) * 0.98F
        }
    }
}
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
package kevin.module.modules.movement.flys.ncp

import kevin.event.UpdateEvent
import kevin.module.modules.movement.flys.FlyMode

object NCPNew : FlyMode("NCPNew") {
    override fun onEnable() {
        if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,mc.thePlayer.entityBoundingBox.offset(.0, .2, .0).expand(.0, .0, .0)).isEmpty()) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + .2, mc.thePlayer.posZ)
        }
        mc.thePlayer.motionX = .0
        mc.thePlayer.motionY = .0
        mc.thePlayer.motionZ = .0
        mc.thePlayer.speedInAir = 0F
    }

    override fun onDisable() {
        mc.thePlayer.speedInAir = .02F
    }
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionX = .0
        mc.thePlayer.motionY = .0
        mc.thePlayer.motionZ = .0
    }
}
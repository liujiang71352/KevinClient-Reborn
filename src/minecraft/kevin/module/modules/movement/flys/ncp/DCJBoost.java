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
package kevin.module.modules.movement.flys.ncp;

import kevin.event.UpdateEvent;
import kevin.module.modules.movement.flys.FlyMode;
import kevin.utils.MovementUtils;
import org.jetbrains.annotations.NotNull;

public class DCJBoost extends FlyMode {
    public DCJBoost() {
        super("DCJBoost");
    }

    public void onUpdate(@NotNull UpdateEvent event) {
        mc.thePlayer.motionY = 0.0;
        mc.thePlayer.motionX = 0.0;
        mc.thePlayer.motionZ = 0.0;
        mc.thePlayer.capabilities.isFlying = false;
        MovementUtils.strafe(0.9F * ((mc.thePlayer.ticksExisted % 40) / 120F + 0.7666F));
    }
}

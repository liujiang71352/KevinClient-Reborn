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
package kevin.module.modules.misc

import kevin.module.BooleanValue
import kevin.module.Module

object PerformanceBooster : Module("PerformanceBooster", "Optimize to improve performance.") {
    private val staticParticleColorValue = BooleanValue("StaticParticleColor", false)
    private val fastEntityLightningValue = BooleanValue("FastEntityLightning", false)
    private val fastBlockLightningValue = BooleanValue("FastBlockLightning", false)
    val staticParticleColor
        get() = this.state && staticParticleColorValue.get()
    val fastEntityLightning
        get() = this.state && fastEntityLightningValue.get()
    val fastBlockLightning
        get() = this.state && fastBlockLightningValue.get()
}
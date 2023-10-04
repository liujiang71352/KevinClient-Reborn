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

import kevin.module.BooleanValue
import kevin.module.IntegerValue
import kevin.module.Module
import kevin.module.ModuleCategory

class FastPlace : Module("FastPlace", "Allows you to place blocks faster.", category = ModuleCategory.WORLD) {
    val speedValue = IntegerValue("Speed", 0, 0, 4)
    @JvmField
    val onlyAimingBlock = BooleanValue("OnlyAimingBlock", true)
    @JvmField
    val onlyHandingBlock = BooleanValue("OnlyHandingBlock", true)
}
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

import kevin.event.AttackEvent
import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.event.WorldEvent
import kevin.main.KevinClient
import kevin.module.ListValue
import kevin.module.Module
import kevin.module.TextValue
import net.minecraft.entity.player.EntityPlayer
import java.io.File
import java.io.FileFilter

class AutoL : Module("KillMessage","Send messages automatically when you kill a player.") {
    //从文件夹加载
    private val modeList = arrayListOf("Single")
    private val fileSuffix = ".txt"
    private val messageFiles: Array<out File>?
    init {
        val files = KevinClient.fileManager.killMessages
        messageFiles = files.listFiles(FileFilter { it.name.endsWith(fileSuffix) })
        if (messageFiles != null) for (i in messageFiles) modeList.add(i.name.split(fileSuffix)[0])
    }
    private val modeValue = ListValue("Mode", modeList.toTypedArray(),"Single")
    private val prefix = ListValue("Prefix", arrayOf("None","/shout",".","@","!","Custom"), "None")
    private val customPrefix = TextValue("CustomPrefix", "")
    private val singleMessage = TextValue("SingleMessage","L %name")
    //攻击目标列表
    private val entityList = arrayListOf<EntityPlayer>()
    //在世界切换时清空攻击目标列表
    @EventTarget fun onWorld(event: WorldEvent) = entityList.clear()
    //在攻击时 如果 攻击目标是玩家 且 攻击目标不在列表内 将目标添加进列表
    @EventTarget fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityPlayer && event.targetEntity !in entityList)
            entityList.add(event.targetEntity)
    }
    @EventTarget fun onUpdate(event: UpdateEvent) {
        //如果玩家死亡 发送消息 从列表移除
        entityList.filter { it.isDead }.forEach { entityPlayer ->
            val text = if (modeValue equal "Single") singleMessage.get() else messageFiles!!.first { it.name.replace(fileSuffix,"") == modeValue.get() }.readLines().random()
            mc.thePlayer.sendChatMessage(addPrefix(text).replace("%MyName",mc.thePlayer.name).replace("%name",entityPlayer.name))
            entityList.remove(entityPlayer)
        }
    }
    private fun addPrefix(message: String) =
        when(prefix.get()) {
            "/shout" -> "/shout $message"
            "." -> ".say .$message"
            "@" -> "@$message"
            "!" -> "!$message"
            "Custom" -> "${customPrefix.get()}$message"
            else -> message
        }
}
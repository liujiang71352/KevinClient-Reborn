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
package kevin.main

import kevin.cape.CapeManager
import kevin.command.CommandManager
import kevin.command.bind.BindCommandManager
import kevin.event.ClientShutdownEvent
import kevin.event.EventManager
import kevin.file.ConfigManager
import kevin.file.FileManager
import kevin.file.ImageManager
import kevin.file.ResourceManager
import kevin.font.FontGC
import kevin.hud.HUD
import kevin.hud.HUD.Companion.createDefault
import kevin.module.ModuleManager
import kevin.module.modules.render.ClickGui.ClickGUI
import kevin.module.modules.render.ClickGui.NewClickGui
import kevin.module.modules.render.Renderer
import kevin.script.ScriptManager
import kevin.skin.SkinManager
import kevin.utils.CombatManager
import kevin.font.FontManager
import kevin.module.modules.misc.ConfigsManager
import kevin.utils.RotationUtils
import kevin.via.ViaVersion
import org.lwjgl.opengl.Display

object KevinClient {
    var name = "Kevin"
    var version = "u2.4.1" // u - updated

    var isStarting = true

    val debug = false

    lateinit var moduleManager: ModuleManager
    lateinit var fileManager: FileManager
    lateinit var eventManager: EventManager
    lateinit var commandManager: CommandManager
    lateinit var fontManager: FontManager
    lateinit var clickGUI: ClickGUI
    lateinit var newClickGui: NewClickGui
    lateinit var hud: HUD
    lateinit var capeManager: CapeManager
    lateinit var combatManager: CombatManager

    var cStart = "§l§7[§l§9Kevin§l§7]"

    fun run() {
        Display.setTitle("$name $version | Minecraft 1.8.9")
        moduleManager = ModuleManager()
        fileManager = FileManager()
        fileManager.load()
        commandManager = CommandManager()
        eventManager = EventManager()
        fontManager = FontManager()
        fontManager.loadFonts()
        eventManager.registerListener(FontGC)
        Renderer.load()
        moduleManager.load()
        ScriptManager.load()
        fileManager.loadConfig(fileManager.modulesConfig)
        fileManager.loadConfig(fileManager.bindCommandConfig)
        eventManager.registerListener(BindCommandManager)
        eventManager.registerListener(RotationUtils())
        hud = createDefault()
        fileManager.loadConfig(fileManager.hudConfig)
        commandManager.load()
        clickGUI = ClickGUI()
        newClickGui = NewClickGui()
        capeManager = CapeManager()
        capeManager.load()
        SkinManager.load()
        ImageManager.load()
        ResourceManager.init()
        ConfigManager.load()
        ConfigsManager.updateValue()
        combatManager = CombatManager()
        ViaVersion.start()
        isStarting = false
    }

    fun stop() {
        eventManager.callEvent(ClientShutdownEvent())
        fileManager.saveAllConfigs()
        capeManager.save()
        SkinManager.save()
        ImageManager.save()
    }
}
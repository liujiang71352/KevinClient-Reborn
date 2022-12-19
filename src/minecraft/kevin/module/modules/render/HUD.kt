package kevin.module.modules.render

import kevin.event.*
import kevin.hud.designer.GuiHudDesigner
import kevin.hud.element.elements.ScoreboardElement
import kevin.main.KevinClient
import kevin.module.BooleanValue
import kevin.module.Module
import kevin.module.ModuleCategory
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S09PacketHeldItemChange

class HUD(currentSlot: Int) : Module("HUD","Toggles visibility of the HUD.",category = ModuleCategory.RENDER) {
    var keepScoreboard = BooleanValue("KeepScoreboard", true)
    private var hotBarShowCurrentSlot = BooleanValue("HotBarShowCurrentSlot", true)
    private var currentPacketSlot = 0

    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if ((mc.currentScreen) is GuiHudDesigner)
            return

        KevinClient.hud.render(false)
    }

    @EventTarget(true)
    fun renderScoreboard(event: Render2DEvent) {
        if (!this.state && keepScoreboard.get() && KevinClient.hud.elements.filterIsInstance<ScoreboardElement>().isNotEmpty()) {
            KevinClient.hud.renderScoreboardOnly()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {

        //if (event!!.eventState == UpdateState.OnUpdate) return

        KevinClient.hud.update()
    }

    @EventTarget(true) fun onPacket(event: PacketEvent) {
        currentPacketSlot =
            when (event.packet) {
                is C09PacketHeldItemChange -> event.packet.slotId
                is S09PacketHeldItemChange -> event.packet.heldItemHotbarIndex
                else -> return
            }
    }

    @EventTarget(true)
    fun updateScoreboard(event: UpdateEvent) {
        if (!this.state && keepScoreboard.get() && KevinClient.hud.elements.filterIsInstance<ScoreboardElement>().isNotEmpty()) {
            KevinClient.hud.updateScoreboardOnly()
        }
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        KevinClient.hud.handleKey('a', event.key)
    }

    val currentSlot: Int
        get() = if (hotBarShowCurrentSlot.get()) currentPacketSlot else mc.thePlayer.inventory.currentItem
}
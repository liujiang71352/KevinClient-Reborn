@file:Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")

package kevin.module.modules.combat

import kevin.event.*
import kevin.main.KevinClient
import kevin.module.*
import kevin.utils.*
import kevin.utils.PacketUtils.packetList
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.ThreadQuickExitException
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11.*

class BackTrack: Module("BackTrack", "(IN TEST) Lets you attack people in their previous locations", category = ModuleCategory.COMBAT) {
    private val minDistance = FloatValue("MinDistance", 2.99f, 2f, 4f)
    private val maxDistance = FloatValue("MaxDistance", 5f, 2f, 6f)
    private val maxTime = IntegerValue("MaxTime", 200, 0, 1000)
    private val onlyKillAura = BooleanValue("OnlyKillAura", true)
    private val onlyPlayer = BooleanValue("OnlyPlayer", true)
    private val smartPacket = BooleanValue("Smart", true)

    private val espMode = ListValue("ESPMode", arrayOf("FullBox", "OutlineBox", "NormalBox", "None"), "Box")

    val storagePackets = ArrayList<Packet<INetHandlerPlayClient>>()
    private val storageEntities = ArrayList<Entity>()

    private val killAura: KillAura by lazy { KevinClient.moduleManager.getModule(KillAura::class.java) }
//    private var currentTarget : EntityLivingBase? = null
    private var timer = MSTimer()
    var needFreeze = false

//    @EventTarget
    // for safety, see in met.minecraft.network.NetworkManager
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        val packet = event.packet
        if (packet.javaClass.name.contains("net.minecraft.network.play.server.", true)) {
            if (packet is S14PacketEntity) {
                val entity = packet.getEntity(mc.theWorld!!)?: return
                if (onlyPlayer.get() && entity !is EntityPlayer) return
                entity.serverPosX += packet.func_149062_c().toInt()
                entity.serverPosY += packet.func_149061_d().toInt()
                entity.serverPosZ += packet.func_149064_e().toInt()
                val x = entity.serverPosX.toDouble() / 32.0
                val y = entity.serverPosY.toDouble() / 32.0
                val z = entity.serverPosZ.toDouble() / 32.0
                if (!onlyKillAura.get() || killAura.state || needFreeze) {
                    val afterBB = AxisAlignedBB(x - 0.3F, y, z - 0.3F, x + 0.3F, y + 1.8F, z + 0.3F)
                    var afterRange = afterBB.getLookingTargetVec(mc.thePlayer!!)
                    var beforeRange = entity.getLookDistanceToEntityBox()
                    if (afterRange == Double.MAX_VALUE) {
                        val eyes = mc.thePlayer!!.getPositionEyes(1F)
                        afterRange = getNearestPointBB(eyes, afterBB).distanceTo(eyes) + 0.075
                    }
                    if (beforeRange == Double.MAX_VALUE) beforeRange = mc.thePlayer!!.getDistanceToEntityBox(entity) + 0.075

                    if (beforeRange < minDistance.get()) {
                        if (afterRange in minDistance.get()..maxDistance.get()) {
                            if (!needFreeze) {
                                timer.reset()
                                needFreeze = true
                            }
                            if (!storageEntities.contains(entity)) storageEntities.add(entity)
                            event.cancelEvent()
                            return
                        }
                        else {
                            if (smartPacket.get()) {
                                if (afterRange < beforeRange) {
                                    if (needFreeze) releasePackets()
                                }
                            }
                        }
                    }
                }
                if (needFreeze) {
                    if (!storageEntities.contains(entity)) storageEntities.add(entity)
                    event.cancelEvent()
                    return
                }
                if (!event.isCancelled && !needFreeze) {
                    KevinClient.eventManager.callEvent(EntityMovementEvent(entity))
                    val f = if (packet.func_149060_h()) (packet.func_149066_f() * 360).toFloat() / 256.0f else entity.rotationYaw
                    val f1 = if (packet.func_149060_h()) (packet.func_149063_g() * 360).toFloat() / 256.0f else entity.rotationPitch
                    entity.setPositionAndRotation2(x, y, z, f, f1, 3, false)
                    entity.onGround = packet.onGround
                }
                event.cancelEvent()
//                storageEntities.add(entity)
            } else {
                if (needFreeze && !event.isCancelled) {
                    storagePackets.add(packet as Packet<INetHandlerPlayClient>)
                    event.cancelEvent()
                }
            }
        }
    }

    @EventTarget fun onMotion(event: MotionEvent) {
        if (needFreeze && timer.hasTimePassed(maxTime.get().toLong())) {
            releasePackets()
        }
    }

    @EventTarget fun onWorld(event: WorldEvent) {
        storageEntities.clear()
        releasePackets()
    }

    @EventTarget fun onRender3D(event: Render3DEvent) {
        if (espMode equal "None" || !needFreeze) return

        var outline = false
        var filled = false
        when (espMode.get()) {
            "NormalBox" -> {
                outline = true
                filled = true
            }
            "FullBox" -> {
                filled = true
            }
            else -> {
                outline = true
            }
        }

        // pre draw
        glPushMatrix()
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)

        glDepthMask(false)

        if (outline) {
            glLineWidth(1f)
            glEnable(GL_LINE_SMOOTH)
        }
        // drawing
        val renderManager = mc.renderManager
        for (entity in storageEntities) {
            val x = entity.serverPosX.toDouble() / 32.0 - renderManager.renderPosX
            val y = entity.serverPosY.toDouble() / 32.0 - renderManager.renderPosY
            val z = entity.serverPosZ.toDouble() / 32.0 - renderManager.renderPosZ
            val bb = AxisAlignedBB(x - 0.4F, y, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F)
            if (outline) {
                RenderUtils.glColor(32, 200, 32, 255)
                RenderUtils.drawSelectionBoundingBox(bb)
            }
            if (filled) {
                RenderUtils.glColor(32, 255, 32, if (outline) 26 else 35)
                RenderUtils.drawFilledBox(bb)
            }
        }

        // post draw
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        glDepthMask(true)
        if (outline) {
            glDisable(GL_LINE_SMOOTH)
        }
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glPopMatrix()
    }

    fun releasePackets() {
        val netHandler: INetHandlerPlayClient = mc.netHandler
        while (storagePackets.isNotEmpty()) {
            storagePackets.removeAt(0).let{
                try {
                    val packetEvent = PacketEvent(it)
                    if (!packetList.contains(it)) KevinClient.eventManager.callEvent(packetEvent)
                    if (packetEvent.isCancelled) return@let
                    it.processPacket(netHandler)
                } catch (_: ThreadQuickExitException) { }
            }
        }
        while (storageEntities.isNotEmpty()) {
            storageEntities.removeAt(0).let { entity ->
                if (!entity.isDead) {
                    val x = entity.serverPosX.toDouble() / 32.0
                    val y = entity.serverPosY.toDouble() / 32.0
                    val z = entity.serverPosZ.toDouble() / 32.0
                    entity.setPosition(x, y, z)
                }
            }
        }
        needFreeze = false
    }

    init {
        NetworkManager.backTrack = this
    }
//    val target: EntityLivingBase?
//    get() = if (onlyKillAura.get()) {
//        if (killAura.target is EntityLivingBase) killAura.target as EntityLivingBase?
//        else null
//    } else currentTarget
}

//data class DataEntityPosStorage(val entity: EntityLivingBase, var modifiedTick: Int = 0)
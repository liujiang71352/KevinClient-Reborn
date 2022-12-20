@file:Suppress("UNCHECKED_CAST")

package kevin.module.modules.combat

import kevin.event.*
import kevin.main.KevinClient
import kevin.module.*
import kevin.utils.MSTimer
import kevin.utils.RenderUtils
import kevin.utils.getLookDistanceToEntityBox
import kevin.utils.getLookingTargetVec
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11

class BackTrack: kevin.module.Module("BackTrack", "(IN TEST) Lets you attack people in their previous locations", category = ModuleCategory.COMBAT) {
    private val minDistance = FloatValue("MinDistance", 2.99f, 2f, 4f)
    private val maxDistance = FloatValue("MaxDistance", 5f, 2f, 6f)
    private val maxTime = IntegerValue("MaxTime", 200, 0, 1000)
    private val onlyKillAura = BooleanValue("OnlyKillAura", true)
    private val onlyPlayer = BooleanValue("OnlyPlayer", true)

    private val espMode = ListValue("ESPMode", arrayOf("FullBox", "OutlineBox", "NormalBox", "None"), "Box")

    private val storagePackets = ArrayList<Packet<INetHandlerPlayClient>>()
    private val storageEntities = ArrayList<Entity>()

    private val killAura: KillAura by lazy { KevinClient.moduleManager.getModule(KillAura::class.java) }
    private var currentTarget : EntityLivingBase? = null
    private var timer = MSTimer()
    var needFreeze = false

    @EventTarget fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet.javaClass.name.contains("net.minecraft.network.play.server.", true)) {
            if (packet is S14PacketEntity) {
                val entity = packet.getEntity(mc.theWorld!!)?: return
                entity.serverPosX += packet.func_149062_c().toInt()
                entity.serverPosY += packet.func_149061_d().toInt()
                entity.serverPosZ += packet.func_149064_e().toInt()
                val x = entity.serverPosX.toDouble() / 32.0
                val y = entity.serverPosY.toDouble() / 32.0
                val z = entity.serverPosZ.toDouble() / 32.0
                if (!onlyKillAura.get() || killAura.state || needFreeze) {
                    val afterBB = AxisAlignedBB(x - 0.3F, y, z - 0.3F, x + 0.3F, y + 1.8F, z + 0.3F)
                    val afterRange = afterBB.getLookingTargetVec(mc.thePlayer!!)
                    val beforeRange = entity.getLookDistanceToEntityBox()
                    if (beforeRange < minDistance.get()) {
                        if (afterRange in minDistance.get()..maxDistance.get()) {
                            if (!needFreeze) {
                                timer.reset()
                                needFreeze = true
                            }
                            event.cancelEvent()
                            return
                        }
//                        else {
//                            if (needFreeze) {
//                                releasePackets()
//                            }
//                        }
                    }
                }
                if (!event.isCancelled && !needFreeze) {
                    KevinClient.eventManager.callEvent(EntityMovementEvent(entity))
                    val f = if (packet.func_149060_h()) (packet.func_149066_f() * 360).toFloat() / 256.0f else entity.rotationYaw
                    val f1 = if (packet.func_149060_h()) (packet.func_149063_g() * 360).toFloat() / 256.0f else entity.rotationPitch
                    entity.setPositionAndRotation2(x, y, z, f, f1, 3, false)
                    entity.onGround = packet.onGround
                }
                if (needFreeze) {
                    if (!storageEntities.contains(entity)) {
                        storageEntities.add(entity)
                    }
                }
                event.cancelEvent()
//                storageEntities.add(entity)
            } else {
                if (needFreeze) {
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

    fun releasePackets() {
        val netHandler: INetHandlerPlayClient = mc.netHandler
        while (storagePackets.isNotEmpty()) {
            storagePackets.removeAt(0).processPacket(netHandler)
        }
        needFreeze = false
    }

    @EventTarget fun onAttack(event: AttackEvent) {
        val attackTarget = event.targetEntity ?: return
        if (onlyKillAura.get()) return
    }

    @EventTarget fun onRender3D(event: Render3DEvent) {
        if (espMode equal "None" || !needFreeze) return

        var outline = false
        var filled = false
        when (espMode.get()) {
            "OutlineBox" -> {
                outline = true
            }
            "FullBox" -> {
                filled = true
            }
            else -> {
                outline = true
                filled = true
            }
        }

        // pre draw
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        GL11.glDepthMask(false)
        if (outline) {
            GL11.glLineWidth(1f)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
        }
        // drawing
        for (entity in storageEntities) {
            val x = entity.serverPosX.toDouble() / 32.0
            val y = entity.serverPosY.toDouble() / 32.0
            val z = entity.serverPosZ.toDouble() / 32.0
            val bb = AxisAlignedBB(x - 0.3F, y, z - 0.3F, x + 0.3F, y + 1.8F, z + 0.3F)
            if (outline) {
                RenderUtils.glColor(32, 255, 32, 150)
                RenderUtils.drawSelectionBoundingBox(bb)
            }
            if (filled) {
                RenderUtils.glColor(32, 255, 32, if (outline) 26 else 35)
                RenderUtils.drawFilledBox(bb)
            }
        }

        // post draw
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GL11.glDepthMask(true)
        if (outline) {
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
        }
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }
//    val target: EntityLivingBase?
//    get() = if (onlyKillAura.get()) {
//        if (killAura.target is EntityLivingBase) killAura.target as EntityLivingBase?
//        else null
//    } else currentTarget
}

//data class DataEntityPosStorage(val entity: EntityLivingBase, var modifiedTick: Int = 0)
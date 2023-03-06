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
package kevin.module.modules.movement

import kevin.event.*
import kevin.module.*
import kevin.module.modules.movement.flys.verus.VerusAuto
import kevin.utils.MovementUtils
import kevin.utils.PacketUtils
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import kotlin.math.floor

@Suppress("unused_parameter")
class LongJump : Module("LongJump", "Allows you to jump further.", category = ModuleCategory.MOVEMENT) {
    private val modeValue = ListValue("Mode", arrayOf("NCP", "AACv1", "AACv2", "Buzz", "BuzzBoost", "PikaNew", "AACv3", "Mineplex", "Mineplex2", "Mineplex3", "Redesky", "Vulcan", "BlocksMCBlockOver", "ExplosionBoost"), "NCP")
    private val ncpBoostValue = FloatValue("NCPBoost", 4.25f, 1f, 10f)
    private val autoJumpValue = BooleanValue("AutoJump", false)
    private val explosionBoostHigh = FloatValue("ExplosionBoostHigh",0.00F,0.01F,1F)
    private val explosionBoostLong = FloatValue("ExplosionBoostLong",0.25F,0.01F,1F)
    private val visualSpoofY = BooleanValue("visualSpoofY", false)
    private val autoDisableValue = BooleanValue("AutoDisable", true)
    private var jumped = false
    private var canBoost = false
    private var teleported = false
    private var canMineplexBoost = false
    private var explosion = false
    private var jumpPositionY = 0.0

    override fun onEnable() {
        canBoost = false
    }
    override fun onDisable() {

    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val thePlayer = mc.thePlayer ?: return

        if(modeValue.get() == "Buzz" && thePlayer.onGround && canBoost){
            this.state = false
            canBoost = false
        }
        if (jumped) {
            val mode = modeValue.get()

            if (thePlayer.onGround || thePlayer.capabilities.isFlying) {
                jumped = false
                canMineplexBoost = false

                if (mode.equals("NCP", ignoreCase = true)) {
                    thePlayer.motionX = 0.0
                    thePlayer.motionZ = 0.0
                }
                if (modeValue equal "Vulcan") {
                    if (canBoost) {
                        if (thePlayer.onGround) {
                            thePlayer.jump()
                            canBoost = false
                        }
                    }
                }
                if (autoDisableValue.get()) state = false
                return
            }
            run {
                when (mode.lowercase()) {
                    "ncp" -> {
                        MovementUtils.strafe(MovementUtils.speed * if (canBoost) ncpBoostValue.get() else 1f)
                        canBoost = false
                    }
                    "buzz" -> {
                        canBoost = true
                        MovementUtils.strafe(MovementUtils.speed * 1.00f)
                    }
                    "buzzboost" -> {
                        if (thePlayer.hurtTime > 8) {
                            MovementUtils.move(0.7578698f)
                            thePlayer.motionY = 0.4679942989799998
                        }
                    }
                    "aacv1" -> {
                        thePlayer.motionY += 0.05999
                        MovementUtils.strafe(MovementUtils.speed * 1.08f)
                    }
                    "aacv2", "mineplex3" -> {
                        thePlayer.jumpMovementFactor = 0.09f
                        thePlayer.motionY += 0.0132099999999999999999999999999
                        thePlayer.jumpMovementFactor = 0.08f
                        MovementUtils.strafe()
                    }
                    "aacv3" -> {
                        if (thePlayer.fallDistance > 0.5f && !teleported) {
                            val value = 3.0
                            val horizontalFacing = thePlayer.horizontalFacing
                            var x = 0.0
                            var z = 0.0

                            when {
                                horizontalFacing==EnumFacing.NORTH -> z = -value
                                horizontalFacing==EnumFacing.EAST -> x = +value
                                horizontalFacing==EnumFacing.SOUTH -> z = +value
                                horizontalFacing==EnumFacing.WEST -> x = -value
                                else -> {
                                }
                            }

                            thePlayer.setPosition(thePlayer.posX + x, thePlayer.posY, thePlayer.posZ + z)
                            teleported = true
                        }
                    }
                    "mineplex" -> {
                        thePlayer.motionY += 0.0132099999999999999999999999999
                        thePlayer.jumpMovementFactor = 0.08f
                        MovementUtils.strafe()
                    }
                    "mineplex2" -> {
                        if (!canMineplexBoost)
                            return@run

                        thePlayer.jumpMovementFactor = 0.1f
                        if (thePlayer.fallDistance > 1.5f) {
                            thePlayer.jumpMovementFactor = 0f
                            thePlayer.motionY = (-10f).toDouble()
                        }

                        MovementUtils.strafe()
                    }
                    "redesky" -> {
                        thePlayer.jumpMovementFactor = 0.15f
                        thePlayer.motionY += 0.05f
                    }
                    "blocksmcblockover" -> {
                        if (mc.theWorld.isBlockFullCube(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 2.1, mc.thePlayer.posZ))) {
                            canBoost = true
                            mc.timer.timerSpeed = 0.2F
                            return
                        } else if (canBoost && thePlayer.onGround) {
                            thePlayer.motionY = 0.2
                            mc.timer.timerSpeed = 1F
                            MovementUtils.strafe(5.0f)
                            canBoost = false
                            return
                        }
                    }
                }
            }
        }
        if (autoJumpValue.get() && thePlayer.onGround && MovementUtils.isMoving) {
            jumped = true
            thePlayer.jump()
        }
        if (modeValue.get().equals("ExplosionBoost",true)){
            if (explosion){
                mc.thePlayer.motionX *= 1F + explosionBoostLong.get()
                mc.thePlayer.motionY *= 1F + explosionBoostHigh.get()
                mc.thePlayer.motionZ *= 1F + explosionBoostLong.get()
                explosion = false
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        val thePlayer = mc.thePlayer ?: return
        val mode = modeValue.get()

        if (mode.equals("mineplex3", ignoreCase = true)) {
            if (thePlayer.fallDistance != 0.0f)
                thePlayer.motionY += 0.037
        } else if (mode.equals("ncp", ignoreCase = true) && !MovementUtils.isMoving && jumped) {
            thePlayer.motionX = 0.0
            thePlayer.motionZ = 0.0
            event.zeroXZ()
        } else if (modeValue equal "PikaNew" && canBoost) {
            event.zeroXZ()
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent) {
        jumped = true
        canBoost = true
        teleported = false
        jumpPositionY = mc.thePlayer!!.posY

        if (state) {
            when (modeValue.get().lowercase()) {
                "mineplex" -> event.motion = event.motion * 4.08f
                "mineplex2" -> {
                    if (mc.thePlayer!!.isCollidedHorizontally) {
                        event.motion = 2.31f
                        canMineplexBoost = true
                        mc.thePlayer!!.onGround = false
                    }
                }
                "buzzboost" -> {
                    // lowVL self damage
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.42, mc.thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1E-10, mc.thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                }
                "pikanew" -> {
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.07 - Math.random(), mc.thePlayer.posZ, true))
                    event.cancelEvent()
                }
                "buzz" -> {
                    val thePlayer = mc.thePlayer ?: return
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(
                        thePlayer.posX,
                        thePlayer.posY + 0.23F,
                        thePlayer.posZ,
                        false
                    ))
                    thePlayer.handleStatusUpdate(2)
                    event.motion = 0.4955111f
                }
                "vulcan" -> {}
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if (event.packet is S27PacketExplosion) {
            if (event.packet.func_149149_c() != 0F ||
                event.packet.func_149144_d() != 0F ||
                event.packet.func_149147_e() != 0F
            ) explosion = true
        }else if (event.packet is S08PacketPlayerPosLook && modeValue equal "pikanew") {
            val packet = event.packet
            if (!mc.netHandler.isDoneLoadingTerrain || !canBoost) return
            canBoost = false
            event.cancelEvent()
            mc.thePlayer.setPositionAndRotation(packet.x, packet.y, packet.z, packet.yaw, packet.pitch)
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(packet.x, packet.y, packet.z, packet.yaw, packet.pitch, false))
            mc.thePlayer.handleStatusUpdate(2)
            mc.thePlayer.motionY = MovementUtils.getJumpMotion(0.701f + 0.0) + Math.random() / 50.0
            MovementUtils.strafe(MovementUtils.getBaseMoveSpeed().toFloat() + 0.22f + (Math.random() / 700f).toFloat())
        }
    }

    @EventTarget fun onMotion(event: MotionEvent) {
        if (visualSpoofY.get()) mc.thePlayer.posY = minOf(mc.thePlayer.lastTickPosY, mc.thePlayer.posY)
    }

    @EventTarget fun onBB(event: BlockBBEvent) {
        if (modeValue equal "Vulcan" && canBoost) {
            if (event.block is BlockAir && event.y <= floor(jumpPositionY)) {
                event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, floor(jumpPositionY), event.z + 1.0)
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
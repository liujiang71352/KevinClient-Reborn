package kevin.module.modules.world;

import com.google.common.base.Predicates;
import kevin.event.*;
import kevin.module.*;
import kevin.module.modules.render.HUD;
import kevin.utils.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.security.SecureRandom;
import java.util.List;
import java.util.*;

public class BlockFly extends Module {
    public final FloatValue yawSpeed = new FloatValue("YawSpeed", 60.0f, 0.0f, 180.0f);
    public final FloatValue pitchSpeed = new FloatValue("PitchSpeed", 60.0f, 0.0f, 180.0f);
    public final BooleanValue rayCast = new BooleanValue("RayCast", false);
    public final BooleanValue playerYaw = new BooleanValue("PlayerYaw", false);
    public final BooleanValue blockSafe = new BooleanValue("BlockSafe", false);
    public final BooleanValue moonWalk = new BooleanValue("MoonWalk", false);
    public final BooleanValue latestRotate = new BooleanValue("LatestRotate", false);
    public final BooleanValue latestPlace = new BooleanValue("LatestPlace", false);
    public final BooleanValue predict = new BooleanValue("Predict", false);
    public final FloatValue backupTicks = new FloatValue("Backup", 1.0f, 0.0f, 3.0f);
    public final BooleanValue adStrafe = new BooleanValue("AdStrafe", true);
    public final BooleanValue adStrafeLegit = new BooleanValue("AdStrafeLegit", true);
    public final BooleanValue sprint = new BooleanValue("Sprint", false);
//    public final BooleanValue moveFix = new BooleanValue("MoveFix", false);
    public final BooleanValue spamClick = new BooleanValue("SpamClick", false);
    public final FloatValue spamClickDelay = new FloatValue("ClickDelay", 0.0f, 0.0f, 200.0f);
    public final BooleanValue intaveHit = new BooleanValue("IntaveHit", false);
    public final BooleanValue rotateToBlock = new BooleanValue("Rotate", true);
    public final BooleanValue correctSide = new BooleanValue("CorrectSide", true);
    public final BooleanValue sameY = new BooleanValue("SameY", false);
    public final BooleanValue esp = new BooleanValue("ESP", true);
    public final BooleanValue noSwing = new BooleanValue("NoSwing", false);
    public final BooleanValue startSneak = new BooleanValue("StartSneak", false);
    public final BooleanValue sneak = new BooleanValue( "Sneak", false);
    public final BooleanValue sneakOnPlace = new BooleanValue("SneakOnPlace", false);
    public final FloatValue sneakTicks = new FloatValue("SneakTicks", 1.0f, 1.0f, 10.0f);
    public final FloatValue sneakBlocks = new FloatValue("SneakBlocksF", 1.0f, 1.0f, 15.0f);
    public final FloatValue sneakBlocksDiagonal = new FloatValue("SneakBlocksD", 1.0f, 1.0f, 15.0f);
    public final BooleanValue sneakDelayBool = new BooleanValue("SneakDelay", false);
    public final IntegerValue sneakDelay = new IntegerValue("SneakDelay", 1000, 0, 4000);
    public final FloatValue timerSpeed = new FloatValue("Timer", 1.0f, 0.1f, 4.0f);
    private final MSTimer startTimeHelper = new MSTimer();
    private final MSTimer sneakTimeHelper = new MSTimer();
    private final MSTimer hitTimeHelper = new MSTimer();
    private final ArrayList<Vec3> lastPositions = new ArrayList<>();
    private final HashMap<Rotation, MovingObjectPosition> map = new HashMap<>();
    public ListValue silentMode = new ListValue("SilentMode", new String[]{"Switch", "Spoof", "None"}, "Spoof");
    public Rotation rots = new Rotation(0f, 0f);
    public Rotation lastRots = new Rotation(0f, 0f);
    private int slotID;
    private BlockPos b;
    private Vec3 aimPos;
    private long lastTime;
    private int blockCounter = 0;
    private Block playerBlock;
    private double[] xyz = new double[3];
    private MovingObjectPosition objectPosition = null;
    private int sneakCounter = 4;
    private int isSneakingTicks = 0;
    private int randomDelay = 0;

    public BlockFly() {
        super("BlockFly", "BlockFly skidded from Augustus b2.6 because I can't write a new scaffold by myself", Keyboard.KEY_NONE, ModuleCategory.WORLD);
    }

    public void onEnable() {
        super.onEnable();
        this.sneakCounter = 4;
        this.blockCounter = 0;
        if (mc.thePlayer != null) {
            mc.getTimer().timerSpeed = 1.0f;
            this.rots = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            this.lastRots = new Rotation(mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch);
            this.b = null;
            this.startTimeHelper.reset();
        }
    }

    public void onDisable() {
        super.onDisable();
        mc.getTimer().timerSpeed = 1.0f;
        this.rots = this.lastRots;
        if (HUD.Companion.getPacketSlot() != mc.thePlayer.inventory.currentItem) mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
    }

    @EventTarget
    public void onEventEarlyTick(TickEvent eventEarlyTick) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        this.objectPosition = null;
        if (this.shouldScaffold()) {
            this.b = this.getBlockPos();
            if (this.playerYaw.get() && this.rayCast.get()) {
                if (this.b != null) {
                    if (this.lastPositions.size() > 20) {
                        this.lastPositions.remove(0);
                    }
                    Vec3 playerPosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                    this.lastPositions.add(playerPosition);
                    this.lastRots = this.rots.cloneSelf();
                    this.rots = this.getPlayerYawRotation();
                    if (mc.thePlayer.hurtResistantTime > 0 && this.blockSafe.get()) {
                        this.rots = this.getRayCastRots();
                    }
                    if (this.objectPosition != null) {
                        this.aimPos = this.objectPosition.hitVec;
                    }
                } else {
                    this.lastRots = this.rots.cloneSelf();
                }
            } else if (!this.rayCast.get()) {
                Vec3 pos;
                this.aimPos = pos = this.getAimPosBasic();
                if (this.rotateToBlock.get()) {
                    if (this.correctSide.get()) {
                        if (this.b != null && this.shouldBuild() && pos != null) {
                            float yawSpeed = MathHelper.clamp_float((float)(this.yawSpeed.get() + (double) RandomUtils.nextFloat(0.0f, 15.0f)), 0.0f, 180.0f);
                            float pitchSpeed = MathHelper.clamp_float((float)(this.pitchSpeed.get() + (double)RandomUtils.nextFloat(0.0f, 15.0f)), 0.0f, 180.0f);
                            this.lastRots = this.rots.cloneSelf();
                            this.rots = RotationUtils.positionRotation(pos.xCoord, pos.yCoord, pos.zCoord, this.lastRots, yawSpeed, pitchSpeed, false);
                        } else {
                            this.lastRots = this.rots.cloneSelf();
                        }
                    } else if (this.b != null && this.shouldBuild()) {
                        MovingObjectPosition objectPosition = playerRayTrace(4.5, 1.0f, this.rots.getYaw(), this.rots.getPitch());
                        if (objectPosition == null || objectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !objectPosition.getBlockPos().equals(this.b)) {
                            float yawSpeed = MathHelper.clamp_float((float)(this.yawSpeed.get() + (double)RandomUtils.nextFloat(0.0f, 15.0f)), 0.0f, 180.0f);
                            float pitchSpeed = MathHelper.clamp_float((float)(this.pitchSpeed.get() + (double)RandomUtils.nextFloat(0.0f, 15.0f)), 0.0f, 180.0f);
                            this.lastRots = this.rots.cloneSelf();
                            this.rots = RotationUtils.positionRotation((double)this.b.getX() + 0.5, (double)this.b.getY() + 0.5, (double)this.b.getZ() + 0.5, this.lastRots, yawSpeed, pitchSpeed, false);
                            this.aimPos = new Vec3((double)this.b.getX() + 0.5, this.b.getY() + 1, (double)this.b.getZ() + 0.5);
                        } else {
                            this.lastRots = this.rots.cloneSelf();
                        }
                    } else {
                        this.lastRots = this.rots.cloneSelf();
                    }
                } else {
                    this.rots = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                    this.lastRots = new Rotation(mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch);
                    this.aimPos = new Vec3((double)this.b.getX() + 0.5, (double)this.b.getY() + 0.5, (double)this.b.getZ() + 0.5);
                }
            } else if (this.rayCast.get() && !this.playerYaw.get()) {
                if (this.b != null) {
                    if (this.lastPositions.size() > 20) {
                        this.lastPositions.remove(0);
                    }
                    Vec3 playerPosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                    this.lastPositions.add(playerPosition);
                    this.lastRots = this.rots.cloneSelf();
                    this.rots = this.getRayCastRots();
                    if (this.objectPosition != null) {
                        this.aimPos = this.objectPosition.hitVec;
                    }
                } else {
                    this.lastRots = this.rots.cloneSelf();
                }
            }
            this.setRotation();
        }
        this.playerBlock = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)).getBlock();
    }

    @EventTarget
    public void onEventClick(ClickUpdateEvent eventClick) {
        eventClick.cancelEvent();
        if (this.shouldScaffold() && this.b != null) {
            ItemStack itemStack = this.getItemStack();
            if(this.silentMode.equal("Spoof") && HUD.Companion.getPacketSlot() != this.slotID) {
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(slotID));
            }
            ItemStack lastItem = mc.thePlayer.inventory.getCurrentItem();
            int slot = mc.thePlayer.inventory.currentItem;
            if (!this.rayCast.get()) {
                EnumFacing enumFacing;
                boolean flag = this.hitTimeHelper.hasTimePassed(this.randomDelay);
                if (flag) {
                    this.hitTimeHelper.reset();
                }
                if (this.shouldBuild() && (enumFacing = this.getPlaceSide(this.b)) != null) {
                    if (this.aimPos == null) {
                        this.aimPos = new Vec3((double)this.b.getX() + 0.5, (double)this.b.getY() + 0.5, (double)this.b.getZ() + 0.5);
                    }
                    if (this.silentMode.equal("Switch")) {
                        mc.thePlayer.inventory.setCurrentItem(itemStack.getItem(), 0, false, false);
                    }
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, this.b, enumFacing, this.aimPos)) {
                        mc.thePlayer.swingItem();
                        this.sneakCounter = 0;
                        ++this.blockCounter;
                        flag = false;
                    }
                }
                if (flag && itemStack != null && this.spamClick.get() && mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, itemStack)) {
                    mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                }
            } else {
                MovingObjectPosition objectPosition = this.objectPosition;
                if (this.objectPosition != null) {
                    objectPosition = this.objectPosition;
                }
                if (objectPosition != null) {
                    boolean flag = this.hitTimeHelper.hasTimePassed(this.randomDelay);
                    if (flag) {
                        this.hitTimeHelper.reset();
                    }
                    switch (objectPosition.typeOfHit) {
                        case ENTITY: {
                            if (mc.playerController.isPlayerRightClickingOnEntity(mc.thePlayer, objectPosition.entityHit, objectPosition)) {
                                flag = false;
                                break;
                            }
                            if (!mc.playerController.interactWithEntitySendPacket(mc.thePlayer, objectPosition.entityHit)) break;
                            flag = false;
                            break;
                        }
                        case BLOCK: {
                            if (objectPosition.getBlockPos().equals(this.b)) {
                                if (objectPosition.sideHit == EnumFacing.UP) {
                                    if (this.sameY.get() && (!mc.gameSettings.keyBindJump.isKeyDown() || !Mouse.isButtonDown(1))) break;
                                    if (this.silentMode.equal("Switch")) {
                                        mc.thePlayer.inventory.setCurrentItem(itemStack.getItem(), 0, false, false);
                                    }
                                    if (!mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) break;
                                    mc.thePlayer.swingItem();
                                    this.sneakCounter = 0;
                                    ++this.blockCounter;
                                    flag = false;
                                    break;
                                }
                                if (this.silentMode.equal("Switch")) {
                                    mc.thePlayer.inventory.setCurrentItem(itemStack.getItem(), 0, false, false);
                                }
                                if (!this.shouldBuild() && this.latestPlace.get() && (!this.latestPlace.get() || this.latestRotate.get()) || !mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) break;
                                mc.thePlayer.swingItem();
                                this.sneakCounter = 0;
                                ++this.blockCounter;
                                flag = false;
                                break;
                            }
                            if (!this.isNearbyBlockPos(objectPosition.getBlockPos()) || objectPosition.sideHit == EnumFacing.UP) break;
                            if (this.silentMode.equal("Switch")) {
                                mc.thePlayer.inventory.setCurrentItem(itemStack.getItem(), 0, false, false);
                            }
                            if (!this.shouldBuild() && this.latestPlace.get() && (!this.latestPlace.get() || this.latestRotate.get()) || !mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) break;
                            mc.thePlayer.swingItem();
                            this.sneakCounter = 0;
                            ++this.blockCounter;
                            flag = false;
                        }
                    }
                    if (flag && itemStack != null && this.spamClick.get() && mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, itemStack)) {
                        mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                    }
                }
            }
            if (itemStack != null && itemStack.stackSize == 0) {
                mc.thePlayer.inventory.mainInventory[this.slotID] = null;
            }
            if (this.silentMode.equal("Switch")) {
                if (lastItem != null) {
                    mc.thePlayer.inventory.setCurrentItem(lastItem.getItem(), 0, false, false);
                } else {
                    mc.thePlayer.inventory.currentItem = slot;
                }
            }
        }
        mc.sendClickBlockToController(false);
        this.setRandomDelay();
    }

    @EventTarget
    private void onUpdate(UpdateEvent event) {
        setRotation();
    }

    private void setRandomDelay() {
        if (this.intaveHit.get()) {
            this.randomDelay = 50;
        } else if (this.spamClickDelay.get() == 0.0) {
            this.randomDelay = 0;
        } else {
            SecureRandom secureRandom = new SecureRandom();
            this.randomDelay = (int)(this.spamClickDelay.get() + (double)secureRandom.nextInt(60));
        }
    }

    @EventTarget
    public void onEventMove(StrafeEvent eventMove) {
//        if (this.moveFix.get()) eventMove.setYaw(mc.thePlayer.rotationYaw);
    }

    @EventTarget
    public void onEventJump(JumpEvent eventJump) {
//        if (this.moveFix.get()) {eventJump.setYaw(mc.thePlayer.rotationYaw);}
    }

    @EventTarget
    public void onEventSaveWalk(MoveEvent eventSaveWalk) {
        if (this.sneak.get() && this.sneakOnPlace.get()) {
            eventSaveWalk.setSafeWalk(true);
        }
    }

    @EventTarget
    public void onEventSilentMove(MovementInputUpdateEvent event) {
//        if (this.moveFix.get()) {
//            eventSilentMove.setSilent(true);
//        }
        if (this.startSneak.get() && (!this.startTimeHelper.hasTimePassed(200L) || mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0 && mc.thePlayer.onGround)) {
            event.setSneak(true);
        }
        Block playerBlock = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)).getBlock();
        if (this.sneak.get() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            if (!this.sneakOnPlace.get()) {
                if (this.sneakDelayBool.get()) {
                    if (this.sneakTimeHelper.hasTimePassed((long)this.sneakDelay.get()) && mc.thePlayer.onGround && this.shouldSneak()) {
                        this.isSneakingTicks = 0;
                        this.sneakTimeHelper.reset();
                    }
                } else if (this.buildForward()) {
                    if ((double)this.blockCounter >= this.sneakBlocks.get() && mc.thePlayer.onGround && this.shouldSneak()) {
                        this.isSneakingTicks = 0;
                    }
                } else if ((double)this.blockCounter >= this.sneakBlocksDiagonal.get() && mc.thePlayer.onGround && this.shouldSneak()) {
                    this.isSneakingTicks = 0;
                }
                if ((double)this.isSneakingTicks < this.sneakTicks.get()) {
                    this.blockCounter = 0;
                    event.setSneak(true);
                    this.sneakTimeHelper.reset();
                    ++this.isSneakingTicks;
                }
            } else if (!event.getJump()) {
                playerBlock = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX - mc.thePlayer.motionX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ - mc.thePlayer.motionZ)).getBlock();
                int random = RandomUtils.nextInt(2, 3);
                if (this.sneakCounter == 1 || this.sneakCounter <= random) {
                    event.setSneak(true);
                    if (this.sneakCounter == random) {
                        this.sneakCounter = 10;
                    }
                }
            }
        }
        ++this.sneakCounter;
        if (this.isTower()) {
            return;
        }
        if (this.rayCast.get() && this.adStrafe.get() && this.b != null && (playerBlock.getMaterial() == Material.air || this.adStrafeLegit.get()) && this.shouldScaffold() && !mc.gameSettings.keyBindJump.isKeyDown() && MovementUtils.isMoving() && this.buildForward() && (!this.moonWalk.get() || !this.playerYaw.get())) {
            if (mc.thePlayer.getHorizontalFacing(this.rots.getYaw()) == EnumFacing.EAST) {
                if ((double)this.b.getZ() + 0.5 > mc.thePlayer.posZ) {
                    this.ad1(event);
                } else {
                    this.ad2(event);
                }
            } else if (mc.thePlayer.getHorizontalFacing(this.rots.getYaw()) == EnumFacing.WEST) {
                if ((double)this.b.getZ() + 0.5 < mc.thePlayer.posZ) {
                    this.ad1(event);
                } else {
                    this.ad2(event);
                }
            } else if (mc.thePlayer.getHorizontalFacing(this.rots.getYaw()) == EnumFacing.SOUTH) {
                if ((double)this.b.getX() + 0.5 < mc.thePlayer.posX) {
                    this.ad1(event);
                } else {
                    this.ad2(event);
                }
            } else if ((double)this.b.getX() + 0.5 > mc.thePlayer.posX) {
                this.ad1(event);
            } else {
                this.ad2(event);
            }
        }
        if (this.moonWalk.get() && this.playerYaw.get() && this.b != null && this.rayCast.get() && this.buildForward() && MovementUtils.isMoving()) {
            if (mc.thePlayer.getHorizontalFacing(this.rots.getYaw() - 18.6f) == EnumFacing.EAST) {
                if ((double)this.b.getZ() + 0.5 > mc.thePlayer.posZ) {
                    event.setStrafe(1.0f);
                }
            } else if (mc.thePlayer.getHorizontalFacing(this.rots.getYaw() - 18.6f) == EnumFacing.WEST) {
                if ((double)this.b.getZ() + 0.5 < mc.thePlayer.posZ) {
                    event.setStrafe(1.0f);
                }
            } else if (mc.thePlayer.getHorizontalFacing(this.rots.getYaw() - 18.6f) == EnumFacing.SOUTH) {
                if ((double)this.b.getX() + 0.5 < mc.thePlayer.posX) {
                    event.setStrafe(1.0f);
                }
            } else if ((double)this.b.getX() + 0.5 > mc.thePlayer.posX) {
                event.setStrafe(1.0f);
            }
        }
    }

    @EventTarget
    public void onEventPreMotion(MotionEvent eventPreMotion) {
        mc.getTimer().timerSpeed = this.timerSpeed.get();
    }

//    @EventTarget
//    public void onEventPostMouseOver(EventPostMouseOver eventPostMouseOver) {
//        if (this.objectPosition != null) {
//            BlockFly.mc.objectMouseOver = this.objectPosition;
//        }
//    }
//
//    @EventTarget
//    public void onEventSwingItemClientSide(EventSwingItemClientSide eventSwingItemClientSide) {
//        if (this.noSwing.getBoolean()) {
//            eventSwingItemClientSide.cancel = true;
//        }
//    }

    private void ad1(MovementInputUpdateEvent event) {
        if (mc.thePlayer.movementInput.moveForward != 0.0f) {
            mc.thePlayer.movementInput.moveStrafe = mc.thePlayer.movementInput.moveForward > 0.0f ? 1.0f : -1.0f;
        } else if (mc.thePlayer.movementInput.moveStrafe != 0.0f) {
            mc.thePlayer.movementInput.moveForward = mc.thePlayer.movementInput.moveStrafe > 0.0f ? -1.0f : 1.0f;
        }
    }

    private void ad2(MovementInputUpdateEvent event) {
        if (mc.thePlayer.movementInput.moveForward != 0.0f) {
            mc.thePlayer.movementInput.moveStrafe = mc.thePlayer.movementInput.moveForward > 0.0f ? -1.0f : 1.0f;
        } else if (mc.thePlayer.movementInput.moveStrafe != 0.0f) {
            mc.thePlayer.movementInput.moveForward = mc.thePlayer.movementInput.moveStrafe > 0.0f ? 1.0f : -1.0f;
        }
    }

    private Rotation getRayCastRots() {
        float yawSpeed = MathHelper.clamp_float((float)(this.yawSpeed.get() + (double)RandomUtils.nextFloat(0.0f, 15.0f)), 0.0f, 180.0f);
        float pitchSpeed = MathHelper.clamp_float((float)(this.pitchSpeed.get() + (double)RandomUtils.nextFloat(0.0f, 15.0f)), 0.0f, 180.0f);
        if (this.isTower()) {
            Vec3 pos = this.getAimPosBasic();
            MovingObjectPosition objectPosition = this.objectPosition;
            if (objectPosition != null) {
                if (objectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !objectPosition.getBlockPos().equals(this.b) || objectPosition.sideHit != EnumFacing.UP) {
                    if (pos != null) {
                        return RotationUtils.positionRotation(pos.xCoord, pos.yCoord, pos.zCoord, this.lastRots, yawSpeed, pitchSpeed, false);
                    }
                    return this.rots;
                }
                return this.rots;
            }
            if (pos != null) {
                return RotationUtils.positionRotation(pos.xCoord, pos.yCoord, pos.zCoord, this.lastRots, yawSpeed, pitchSpeed, false);
            }
        }
        float yaw = this.rots.getYaw();
        Rotation rotations = new Rotation(yaw, this.rots.getPitch());
        if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0 && mc.thePlayer.onGround || !this.startTimeHelper.hasTimePassed(200L)) {
            rotations = RotationUtils.limitAngleChange(this.rots, new Rotation(mc.thePlayer.rotationYaw - 180.0f, 80.34f), yawSpeed, pitchSpeed);
            rotations.fixedSensitivity();
        } else if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0 && mc.thePlayer.onGround) {
            this.startTimeHelper.reset();
        }
        if (this.shouldBuild()) {
            ArrayList<Rotation> rots = new ArrayList<>();
            float difference = 0.1f;
            float currentX = this.rots.getYaw();
            float currentY = this.rots.getPitch();
            for (float dist = 0.0f; dist < 180.0f; dist += difference) {
                Rotation f;
                float maxX = this.rots.getYaw() + dist;
                float minX = this.rots.getYaw() - dist;
                float maxY = Math.min(this.rots.getPitch() + dist, 90.0f);
                float minY = Math.max(this.rots.getPitch() - dist, -90.0f);
                while (currentY < maxY) {
                    f = new Rotation(currentX, currentY);
                    f.fixedSensitivity();
                    if (this.canPlace(f)) {
                        rots.add(f);
                    }
                    currentY += difference;
                }
                while (currentX <= maxX) {
                    f = new Rotation(currentX, currentY);
                    f.fixedSensitivity();
                    if (this.canPlace(f)) {
                        rots.add(f);
                    }
                    currentX += difference;
                }
                while (currentY >= minY) {
                    f = new Rotation(currentX, currentY);
                    f.fixedSensitivity();
                    if (this.canPlace(f)) {
                        rots.add(f);
                    }
                    currentY -= difference;
                }
                while (currentX >= minX) {
                    f = new Rotation(currentX, currentY);
                    f.fixedSensitivity();
                    if (this.canPlace(f)) {
                        rots.add(f);
                    }
                    currentX -= difference;
                }
                if (dist > 5.0f && dist <= 10.0f) {
                    difference = 0.3f;
                }
                if (!(dist > 10.0f)) continue;
                difference = (float)((double)difference + ((double)(dist / 500.0f) + 0.01));
            }
            rots.sort(Comparator.comparingDouble(this::distanceToLastRots));
            if (!rots.isEmpty()) {
                rotations = rots.get(0);
                this.objectPosition = this.map.get(rotations);
            }
        }
        return rotations;
    }

    private boolean canPlace(Rotation yawPitch) {
        BlockPos b = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
        MovingObjectPosition m4 = rayCast(1.0f, yawPitch.cloneSelf());
        if (m4.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && isBlockSolid(m4.getBlockPos()) && m4.getBlockPos().equals(this.b) && m4.sideHit != EnumFacing.DOWN && m4.sideHit != EnumFacing.UP && m4.getBlockPos().getY() <= b.getY()) {
            this.map.put(yawPitch, m4);
            return true;
        }
        return false;
    }

    private boolean isBlockSolid(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return ((block.getMaterial().isSolid() || !block.isTranslucent() || block.isBlockNormalCube() || block instanceof BlockLadder || block instanceof BlockCarpet
                || block instanceof BlockSnow || block instanceof BlockSkull)
                && !(block.getMaterial().isLiquid() || block instanceof BlockContainer));
    }

    private double distanceToLastRots(Rotation predictRots) {
        float diff1 = Math.abs(predictRots.getYaw() - this.rots.getYaw());
        float diff2 = Math.abs(predictRots.getPitch() - this.rots.getPitch());
        return diff1 * diff1 + diff1 * diff1 + diff2 * diff2;
    }

    private boolean buildForward() {
        float realYaw;
        float f = realYaw = this.moonWalk.get() ? MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - 180.0f) : MathHelper.wrapAngleTo180_float(this.rots.getYaw());
        if ((double)realYaw > 77.5 && (double)realYaw < 102.5) {
            return true;
        }
        if ((double)realYaw > 167.5 || realYaw < -167.0f) {
            return true;
        }
        if ((double)realYaw < -77.5 && (double)realYaw > -102.5) {
            return true;
        }
        return (double)realYaw > -12.5 && (double)realYaw < 12.5;
    }

    private boolean prediction() {
        Vec3 predictedPosition = this.getPredictedPosition(1);
        BlockPos blockPos = this.getPredictedBlockPos();
        if (blockPos != null && predictedPosition != null) {
            double maX = (double)blockPos.getX() + 1.285;
            double miX = (double)blockPos.getX() - 0.285;
            double maZ = (double)blockPos.getZ() + 1.285;
            double miZ = (double)blockPos.getZ() - 0.285;
            return predictedPosition.xCoord > maX || predictedPosition.xCoord < miX || predictedPosition.zCoord > maZ || predictedPosition.zCoord < miZ;
        }
        return false;
    }

    private Vec3 getPredictedPosition(int predictTicks) {
        Vec3 playerPosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        Vec3 vec3 = null;
        if (!this.lastPositions.isEmpty() && this.lastPositions.size() > 10 && this.lastPositions.size() > this.lastPositions.size() - predictTicks - 1) {
            vec3 = playerPosition.add(playerPosition.subtract(this.lastPositions.get(this.lastPositions.size() - predictTicks - 1)));
        }
        return vec3;
    }

    private BlockPos getPredictedBlockPos() {
        ArrayList<Float> pitchs = new ArrayList<>();
        for (float i = Math.max(this.rots.getPitch() - 30.0f, -90.0f); i < Math.min(this.rots.getPitch() + 20.0f, 90.0f); i += 0.05f) {
            Rotation f = new Rotation(this.rots.getYaw(), i);
            f.fixedSensitivity();
            MovingObjectPosition m4 = playerRayTrace(4.5, 2.0f, this.rots.getYaw(), f.getPitch());
            if (m4.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !isBlockSolid(m4.getBlockPos()) || !this.isNearbyBlockPos(m4.getBlockPos()) || m4.sideHit == EnumFacing.DOWN || m4.sideHit == EnumFacing.UP) continue;
            pitchs.add(f.getPitch());
        }
        float[] rotations = new float[2];
        if (!pitchs.isEmpty()) {
            pitchs.sort(Comparator.comparingDouble(this::distanceToLastPitch));
            if (!pitchs.isEmpty()) {
                rotations[1] = pitchs.get(0);
                rotations[0] = this.rots.getYaw();
            }
            MovingObjectPosition movingObjectPosition = playerRayTrace(4.5, 2.0f, rotations[0], rotations[1]);
            if (movingObjectPosition != null) {
                EnumFacing enumFacing = movingObjectPosition.sideHit;
                BlockPos blockPos = movingObjectPosition.getBlockPos();
                if (enumFacing == EnumFacing.EAST) {
                    return blockPos.add(1, 0, 0);
                }
                if (enumFacing == EnumFacing.WEST) {
                    return blockPos.add(-1, 0, 0);
                }
                if (enumFacing == EnumFacing.NORTH) {
                    return blockPos.add(0, 0, -1);
                }
                if (enumFacing == EnumFacing.SOUTH) {
                    return blockPos.add(0, 0, 1);
                }
            }
        }
        return null;
    }

    private MovingObjectPosition playerRayTrace(double range, float pTicks, float yaw, float pitch) {
        Vec3 eyes = mc.thePlayer.getPositionEyes(pTicks);
        Vec3 target = OtherExtensionsKt.multiply(new Rotation(yaw, pitch).toDirection(), range).add(eyes);
        return mc.theWorld.rayTraceBlocks(eyes, target, false, false, true);
    }

    private Rotation getPlayerYawRotation() {
        MovingObjectPosition objectPosition;
        boolean moonWalk = this.moonWalk.get() && this.buildForward();
        float yaw = this.rots.getYaw();
        float yawSpeed = MathHelper.clamp_float((float)(this.yawSpeed.get() + (double)RandomUtils.nextFloat(0.0f, 15.0f)), 0.0f, 180.0f);
        float pitchSpeed = MathHelper.clamp_float((float)(this.pitchSpeed.get() + (double)RandomUtils.nextFloat(0.0f, 15.0f)), 0.0f, 180.0f);
        if (this.isTower() && (objectPosition = this.objectPosition) != null) {
            return RotationUtils.limitAngleChange(this.rots, new Rotation(moonWalk ? mc.thePlayer.rotationYaw - 180.0f + 18.5f : mc.thePlayer.rotationYaw - 180.0f, 90f), yawSpeed, pitchSpeed);
        }
        Rotation rotation = new Rotation(yaw, this.rots.getPitch());
        if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0 && mc.thePlayer.onGround || !this.startTimeHelper.hasTimePassed(200L)) {
            rotation = RotationUtils.limitAngleChange(rotation, new Rotation(moonWalk ? mc.thePlayer.rotationYaw - 180.0f + 18.5f : mc.thePlayer.rotationYaw - 180.0f, moonWalk ? 80.0f : 80.34f), yawSpeed, pitchSpeed);
        } else if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0 && mc.thePlayer.onGround) {
            this.startTimeHelper.reset();
        }
        float realYaw = mc.thePlayer.rotationYaw;
        if (mc.gameSettings.keyBindBack.pressed) {
            realYaw += 180.0f;
            if (mc.gameSettings.keyBindLeft.pressed) {
                realYaw += 45.0f;
            } else if (mc.gameSettings.keyBindRight.pressed) {
                realYaw -= 45.0f;
            }
        } else if (mc.gameSettings.keyBindForward.pressed) {
            if (mc.gameSettings.keyBindLeft.pressed) {
                realYaw -= 45.0f;
            } else if (mc.gameSettings.keyBindRight.pressed) {
                realYaw += 45.0f;
            }
        } else if (mc.gameSettings.keyBindRight.pressed) {
            realYaw += 90.0f;
        } else if (mc.gameSettings.keyBindLeft.pressed) {
            realYaw -= 90.0f;
        }
        rotation = RotationUtils.limitAngleChange(this.rots, new Rotation(moonWalk ? mc.thePlayer.rotationYaw - 180.0f + 18.5f : realYaw - 180.0f, rots.getPitch()), yawSpeed);
        yaw = rotation.getYaw();
        if (this.shouldBuild()) {
            MovingObjectPosition m1 = rayCast(1.0f, new Rotation(rotation.getYaw(), rotation.getPitch()));
            if (m1.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && isBlockSolid(m1.getBlockPos()) && this.isNearbyBlockPos(m1.getBlockPos()) && m1.sideHit != EnumFacing.DOWN && m1.sideHit != EnumFacing.UP) {
                this.objectPosition = m1;
                return rotation;
            }
            HashMap<Float, MovingObjectPosition> hashMap = new HashMap<>();
            ArrayList<Float> pitchs = new ArrayList<>();
            for (float i = Math.max(this.rots.getPitch() - 30.0f, -90.0f); i < Math.min(this.rots.getPitch() + 20.0f, 90.0f); i += 0.05f) {
                Rotation f = new Rotation(yaw, i);
                f.fixedSensitivity();
                MovingObjectPosition m4 = rayCast(1.0f, new Rotation(yaw, f.getPitch()));
                if (m4 == null || m4.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !isBlockSolid(m4.getBlockPos()) || !this.isNearbyBlockPos(m4.getBlockPos()) || m4.sideHit == EnumFacing.DOWN || m4.sideHit == EnumFacing.UP) continue;
                hashMap.put(f.getPitch(), m4);
                pitchs.add(f.getPitch());
            }
            if (!pitchs.isEmpty()) {
                pitchs.sort(Comparator.comparingDouble(this::distanceToLastPitch));
                if (!pitchs.isEmpty()) {
                    rotation.setPitch(pitchs.get(0));
                    this.objectPosition = hashMap.get(rotation.getPitch());
                }
            } else {
                if (!this.blockSafe.get()) {
                    return rotation;
                }
                int add = 1;
                for (int yawLoops = 0; yawLoops < 180; ++yawLoops) {
                    float yaw1 = yaw + (float)(yawLoops * add);
                    float yaw2 = yaw - (float)(yawLoops * add);
                    float pitch = this.rots.getPitch();
                    for (int pitchLoops = 0; pitchLoops < 25; ++pitchLoops) {
                        float pitch1 = MathHelper.clamp_float(pitch + (float)(pitchLoops * add), -90.0f, 90.0f);
                        float pitch2 = MathHelper.clamp_float(pitch - (float)(pitchLoops * add), -90.0f, 90.0f);
                        Rotation ffff = new Rotation(yaw2, pitch2);
                        ffff.fixedSensitivity();
                        MovingObjectPosition m7 = rayCast(1.0f, ffff);
                        if (m7 != null && m7.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && isBlockSolid(m7.getBlockPos()) && this.isNearbyBlockPos(m7.getBlockPos()) && m7.sideHit != EnumFacing.DOWN && m7.sideHit != EnumFacing.UP) {
                            this.objectPosition = m7;
                            return ffff;
                        }
                        Rotation fff = new Rotation(yaw2, pitch1);
                        fff.fixedSensitivity();
                        MovingObjectPosition m6 = rayCast(1.0f, fff);
                        if (m6 != null && m6.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && isBlockSolid(m6.getBlockPos()) && this.isNearbyBlockPos(m6.getBlockPos()) && m6.sideHit != EnumFacing.DOWN && m6.sideHit != EnumFacing.UP) {
                            this.objectPosition = m6;
                            return fff;
                        }
                        Rotation ff = new Rotation(yaw1, pitch2);
                        ff.fixedSensitivity();
                        MovingObjectPosition m5 = rayCast(1.0f, ff);
                        if (m5 != null && m5.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && isBlockSolid(m5.getBlockPos()) && this.isNearbyBlockPos(m5.getBlockPos()) && m5.sideHit != EnumFacing.DOWN && m5.sideHit != EnumFacing.UP) {
                            this.objectPosition = m5;
                            return ff;
                        }
                        Rotation f = new Rotation(yaw1, pitch1);
                        f.fixedSensitivity();
                        MovingObjectPosition m4 = rayCast(1.0f, f);
                        if (m4 != null && (m4.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !isBlockSolid(m4.getBlockPos()) || !this.isNearbyBlockPos(m4.getBlockPos()) || m4.sideHit == EnumFacing.DOWN || m4.sideHit == EnumFacing.UP))
                            continue;
                        this.objectPosition = m4;
                        return f;
                    }
                }
            }
        }
        return rotation;
    }

    private MovingObjectPosition rayCast(float partialTicks, Rotation rots) {
        MovingObjectPosition objectMouseOver = null;
        Entity entity = mc.getRenderViewEntity();
        if (entity != null && mc.theWorld != null) {
            mc.mcProfiler.startSection("pick");
            mc.pointedEntity = null;
            double d0 = mc.playerController.getBlockReachDistance();
            objectMouseOver = playerRayTrace(d0, partialTicks, rots.getYaw(), rots.getPitch());
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            boolean flag1 = true;
            if (mc.playerController.extendedReach()) {
                d0 = 6.0D;
                d1 = 6.0D;
            } else {
                if (d0 > 3.0D) {
                    flag = true;
                }

                d0 = d0;
            }

            if (objectMouseOver != null) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = rots.toDirection();
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING));
            double d2 = d1;
            AxisAlignedBB realBB = null;

            for(int i = 0; i < list.size(); ++i) {
                Entity entity1 = list.get(i);
                float f1 = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                    if (d3 < d2 || d2 == 0.0D) {
                        boolean flag2 = false;

                        if (entity1 == entity.ridingEntity && !flag2) {
                            if (d2 == 0.0D) {
                                pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > 3.0D) {
                pointedEntity = null;
                objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
                objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
//                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {}
            }
        }

        return objectMouseOver;

    }

    private double distanceToLastPitch(float pitch) {
        return Math.abs(pitch - this.rots.getPitch());
    }

    private double distanceToLastPitch(float[] pitch) {
        return Math.abs(pitch[0] - this.rots.getPitch());
    }

    private boolean isNearbyBlockPos(BlockPos blockPos) {
        if (mc.thePlayer.onGround) {
            for (int x = this.b.getX() - 1; x <= this.b.getX() + 1; ++x) {
                for (int z = this.b.getZ() - 1; z <= this.b.getZ() + 1; ++z) {
                    if (!blockPos.equals(new BlockPos(x, this.b.getY(), z))) continue;
                    return true;
                }
            }
            return false;
        }
        return blockPos.equals(this.b);
    }

    private Vec3 getAimPosBasic() {
        if (this.b == null) {
            return null;
        }
        EnumFacing enumFacing = this.getPlaceSide(this.b);
        Block block = mc.theWorld.getBlockState(this.b).getBlock();
        double add = 0.01f;
        Vec3 min = null;
        Vec3 max = null;
        if (enumFacing != null) {
            if (enumFacing == EnumFacing.UP) {
                min = new Vec3((double)this.b.getX() + add, (double)this.b.getY() + block.getBlockBoundsMaxY(), (double)this.b.getZ() + add);
                max = new Vec3((double)this.b.getX() + block.getBlockBoundsMaxX() - add, (double)this.b.getY() + block.getBlockBoundsMaxY(), (double)this.b.getZ() + block.getBlockBoundsMaxZ() - add);
            } else if (enumFacing == EnumFacing.WEST) {
                min = new Vec3(this.b.getX(), (double)this.b.getY() + add, (double)this.b.getZ() + add);
                max = new Vec3(this.b.getX(), (double)this.b.getY() + block.getBlockBoundsMaxY() - add, (double)this.b.getZ() + block.getBlockBoundsMaxZ() - add);
            } else if (enumFacing == EnumFacing.EAST) {
                min = new Vec3((double)this.b.getX() + block.getBlockBoundsMaxX(), (double)this.b.getY() + add, (double)this.b.getZ() + add);
                max = new Vec3((double)this.b.getX() + block.getBlockBoundsMaxX(), (double)this.b.getY() + block.getBlockBoundsMaxY() - add, (double)this.b.getZ() + block.getBlockBoundsMaxZ() - add);
            } else if (enumFacing == EnumFacing.SOUTH) {
                min = new Vec3((double)this.b.getX() + add, (double)this.b.getY() + add, (double)this.b.getZ() + block.getBlockBoundsMaxZ());
                max = new Vec3((double)this.b.getX() + block.getBlockBoundsMaxX() - add, (double)this.b.getY() + block.getBlockBoundsMaxY() - add, (double)this.b.getZ() + block.getBlockBoundsMaxZ());
            } else if (enumFacing == EnumFacing.NORTH) {
                min = new Vec3((double)this.b.getX() + add, (double)this.b.getY() + add, this.b.getZ());
                max = new Vec3((double)this.b.getX() + block.getBlockBoundsMaxX() - add, (double)this.b.getY() + block.getBlockBoundsMaxY() - add, this.b.getZ());
            } else if (enumFacing == EnumFacing.DOWN) {
                min = new Vec3((double)this.b.getX() + add, this.b.getY(), (double)this.b.getZ() + add);
                max = new Vec3((double)this.b.getX() + block.getBlockBoundsMaxX() - add, this.b.getY(), (double)this.b.getZ() + block.getBlockBoundsMaxZ() - add);
            }
        }
        if (min != null) {
            return this.getBestHit(min, max);
        }
        return null;
    }

    private EnumFacing getPlaceSide(BlockPos blockPos) {
        Vec3 vec32;
        BlockPos bp;
        ArrayList<Vec3> positions = new ArrayList<>();
        HashMap<Vec3, EnumFacing> hashMap = new HashMap<>();
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        if (isAirBlock(blockPos.add(0, 1, 0)) && !blockPos.add(0, 1, 0).equals(playerPos) && !mc.thePlayer.onGround) {
            bp = blockPos.add(0, 1, 0);
            vec32 = this.getBestHitFeet(bp);
            positions.add(vec32);
            hashMap.put(vec32, EnumFacing.UP);
        }
        if (isAirBlock(blockPos.add(1, 0, 0)) && !blockPos.add(1, 0, 0).equals(playerPos)) {
            bp = blockPos.add(1, 0, 0);
            vec32 = this.getBestHitFeet(bp);
            positions.add(vec32);
            hashMap.put(vec32, EnumFacing.EAST);
        }
        if (isAirBlock(blockPos.add(-1, 0, 0)) && !blockPos.add(-1, 0, 0).equals(playerPos)) {
            bp = blockPos.add(-1, 0, 0);
            vec32 = this.getBestHitFeet(bp);
            positions.add(vec32);
            hashMap.put(vec32, EnumFacing.WEST);
        }
        if (isAirBlock(blockPos.add(0, 0, 1)) && !blockPos.add(0, 0, 1).equals(playerPos)) {
            bp = blockPos.add(0, 0, 1);
            vec32 = this.getBestHitFeet(bp);
            positions.add(vec32);
            hashMap.put(vec32, EnumFacing.SOUTH);
        }
        if (isAirBlock(blockPos.add(0, 0, -1)) && !blockPos.add(0, 0, -1).equals(playerPos)) {
            bp = blockPos.add(0, 0, -1);
            vec32 = this.getBestHitFeet(bp);
            positions.add(vec32);
            hashMap.put(vec32, EnumFacing.NORTH);
        }
        positions.sort(Comparator.comparingDouble(vec3 -> mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord)));
        if (!positions.isEmpty()) {
            Vec3 vec33 = this.getBestHitFeet(this.b);
            if (mc.thePlayer.getDistance(vec33.xCoord, vec33.yCoord, vec33.zCoord) >= mc.thePlayer.getDistance(positions.get(0).xCoord, positions.get(0).yCoord, positions.get(0).zCoord)) {
                return hashMap.get(positions.get(0));
            }
        }
        return null;
    }

    private boolean isAirBlock(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return block.getMaterial() == Material.air;
    }

    private Vec3 getBestHitFeet(BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        double ex = MathHelper.clamp_double(mc.thePlayer.posX, blockPos.getX(), (double)blockPos.getX() + block.getBlockBoundsMaxX());
        double ey = MathHelper.clamp_double(mc.thePlayer.posY, blockPos.getY(), (double)blockPos.getY() + block.getBlockBoundsMaxY());
        double ez = MathHelper.clamp_double(mc.thePlayer.posZ, blockPos.getZ(), (double)blockPos.getZ() + block.getBlockBoundsMaxZ());
        return new Vec3(ex, ey, ez);
    }

    private Vec3 getBestHit(Vec3 min, Vec3 max) {
        Vec3 positionEyes = mc.thePlayer.getPositionEyes(1.0f);
        double x = MathHelper.clamp_double(mc.thePlayer.posX, min.xCoord, max.xCoord);
        double y = MathHelper.clamp_double(mc.thePlayer.posY, min.yCoord, max.yCoord);
        double z = MathHelper.clamp_double(mc.thePlayer.posZ, min.zCoord, max.zCoord);
        return new Vec3(x, y, z);
    }

    private ItemStack getItemStack() {
        ItemStack itemStack = mc.thePlayer.getCurrentEquippedItem();
        if (!this.silentMode.get().equals("None")) {
            for (int i = 36; i < mc.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (stack == null || !(stack.getItem() instanceof ItemBlock) || stack.stackSize <= 0 || !isValidStack(stack)) continue;
                this.slotID = i - 36;
                break;
            }
            itemStack = mc.thePlayer.inventoryContainer.getSlot(this.slotID + 36).getStack();
        } else {
            this.slotID = mc.thePlayer.inventory.currentItem;
        }
        return itemStack;
    }
    private static final int[] INVALID_IDS = {30, 58, 116, 158, 23, 6, 54, 146, 130, 26, 50, 76, 46, 37, 38};

    private boolean isValidStack(ItemStack stack) {
        return Arrays.stream(INVALID_IDS).noneMatch(i -> i == Item.getIdFromItem(stack.getItem()));
    }

    private BlockPos getBlockPos() {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ);
        ArrayList<Vec3> positions = new ArrayList<>();
        HashMap<Vec3, BlockPos> hashMap = new HashMap<>();
        for (int x = playerPos.getX() - 5; x <= playerPos.getX() + 5; ++x) {
            for (int y = playerPos.getY() - 1; y <= playerPos.getY(); ++y) {
                for (int z = playerPos.getZ() - 5; z <= playerPos.getZ() + 5; ++z) {
                    if (!isBlockSolid(new BlockPos(x, y, z))) continue;
                    BlockPos blockPos = new BlockPos(x, y, z);
                    Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                    double ex = MathHelper.clamp_double(mc.thePlayer.posX, blockPos.getX(), (double)blockPos.getX() + block.getBlockBoundsMaxX());
                    double ey = MathHelper.clamp_double(mc.thePlayer.posY, blockPos.getY(), (double)blockPos.getY() + block.getBlockBoundsMaxY());
                    double ez = MathHelper.clamp_double(mc.thePlayer.posZ, blockPos.getZ(), (double)blockPos.getZ() + block.getBlockBoundsMaxZ());
                    Vec3 vec3 = new Vec3(ex, ey, ez);
                    positions.add(vec3);
                    hashMap.put(vec3, blockPos);
                }
            }
        }
        if (!positions.isEmpty()) {
            positions.sort(Comparator.comparingDouble(this::getBestBlock));
            if (this.isTower() && (double) hashMap.get(positions.get(0)).getY() != mc.thePlayer.posY - 1.5) {
                return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.5, mc.thePlayer.posZ);
            }
            return hashMap.get(positions.get(0));
        }
        return null;
    }

    private boolean shouldScaffold() {
        return mc.currentScreen == null;
    }

    private boolean shouldBuild() {
        if (this.latestRotate.get() && this.rayCast.get()) {
            double add1 = 1.282;
            double add2 = 0.282;
            double x = mc.thePlayer.posX;
            double z = mc.thePlayer.posZ;
            this.xyz = new double[]{mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ};
            double maX = (double)this.b.getX() + add1;
            double miX = (double)this.b.getX() - add2;
            double maZ = (double)this.b.getZ() + add1;
            double miZ = (double)this.b.getZ() - add2;
            return (x += (mc.thePlayer.posX - this.xyz[0]) * this.backupTicks.get()) > maX || x < miX || (z += (mc.thePlayer.posZ - this.xyz[2]) * this.backupTicks.get()) > maZ || z < miZ || this.predict.get() && this.rayCast.get() && this.prediction();
        }
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
        return mc.theWorld.isAirBlock(playerPos);
    }

    private boolean shouldSneak() {
        if (this.latestRotate.get() && this.rayCast.get()) {
            double add1 = 1.15;
            double add2 = 0.15;
            double x = mc.thePlayer.posX;
            double z = mc.thePlayer.posZ;
            this.xyz = new double[]{mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ};
            double maX = (double)this.b.getX() + add1;
            double miX = (double)this.b.getX() - add2;
            double maZ = (double)this.b.getZ() + add1;
            double miZ = (double)this.b.getZ() - add2;
            return (x += (mc.thePlayer.posX - this.xyz[0]) * this.backupTicks.get()) > maX || x < miX || (z += (mc.thePlayer.posZ - this.xyz[2]) * this.backupTicks.get()) > maZ || z < miZ || this.predict.get() && this.rayCast.get() && this.prediction();
        }
        Block playerBlock = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + mc.thePlayer.posX - this.xyz[0], mc.thePlayer.posY - 0.5, mc.thePlayer.posZ + mc.thePlayer.posZ - this.xyz[2])).getBlock();
        this.xyz = new double[]{mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ};
        return playerBlock instanceof BlockAir;
    }

    private double getBestBlock(Vec3 vec3) {
        return mc.thePlayer.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    private double[] getAdvancedDiagonalExpandXZ(BlockPos blockPos) {
        double[] xz = new double[2];
        double[] difference = new double[]{(double)blockPos.getX() - mc.thePlayer.posX, (double)blockPos.getZ() - mc.thePlayer.posZ};
        if (difference[0] > -1.0 && difference[0] < 0.0 && difference[1] < -1.0) {
            xz[0] = difference[0] * -1.0;
            xz[1] = 1.0;
        }
        if (difference[1] < 0.0 && difference[1] > -1.0 && difference[0] < -1.0) {
            xz[0] = 1.0;
            xz[1] = difference[1] * -1.0;
        }
        if (difference[0] > -1.0 && difference[0] < 0.0 && difference[1] > 0.0) {
            xz[0] = difference[0] * -1.0;
            xz[1] = 0.0;
        }
        if (difference[1] < 0.0 && difference[1] > -1.0 && difference[0] > 0.0) {
            xz[0] = 0.0;
            xz[1] = difference[1] * -1.0;
        }
        if (difference[0] >= 0.0 && difference[1] < -1.0) {
            xz[1] = 1.0;
        }
        if (difference[1] >= 0.0 & difference[0] < -1.0) {
            xz[0] = 1.0;
        }
        if (!(difference[0] >= 0.0) || difference[1] > 0.0) {
            // empty if block
        }
        if (difference[1] <= -1.0 && difference[0] < -1.0) {
            xz[0] = 1.0;
            xz[1] = 1.0;
        }
        return xz;
    }

    public int getSlotID() {
        return this.slotID;
    }

    private void setRotation() {
        if (mc.currentScreen != null) {
            return;
        }
        RotationUtils.setTargetRotation(this.rots, 1);
    }

    private boolean isTower() {
        return mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0;
    }

    @EventTarget
    public void onEventRender3D(Render3DEvent eventRender3D) {
        if (this.esp.get()) {
            float red = 1.0f;
            float green = 1.0f;
            float blue = 1.0f;
//            float lineWidth = 0.0f;
            if (this.b != null) {
                if (mc.thePlayer.getDistance(this.b.getX(), this.b.getY(), this.b.getZ()) > 1.0) {
                    double d0 = 1.0 - mc.thePlayer.getDistance(this.b.getX(), this.b.getY(), this.b.getZ()) / 20.0;
                    if (d0 < 0.3) {
                        d0 = 0.3;
                    }
//                    lineWidth = (float)((double)lineWidth * d0);
                }
                RenderUtils.drawBlockBox(this.b, new Color(red, green, blue, 0.3137255f), false);
            }
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
package kevin.utils

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3

fun Vec3.multiply(value: Double): Vec3 {
    return Vec3(this.xCoord * value, this.yCoord * value, this.zCoord * value)
}

fun AxisAlignedBB.getLookingTargetVec(thePlayer: EntityPlayerSP, rotation: Rotation?= null, range: Double=6.0): Double {
    val eyes = thePlayer.getPositionEyes(1F)
    return this.calculateIntercept(eyes, (rotation ?: RotationUtils.bestServerRotation()).toDirection().multiply(range))?.hitVec?.distanceTo(eyes) ?: Double.MAX_VALUE
}

fun AxisAlignedBB.expands(v: Double, modifyYDown: Boolean=true, modifyYUp: Boolean=true): AxisAlignedBB {
    return AxisAlignedBB(this.minX - v, this.minY - (if (modifyYDown) v else 0.0), this.minZ - v, this.maxX + v, this.maxY + (if (modifyYUp) v else 0.0), this.maxZ + v)
}
package kevin.hackChecks.checks.move;

import kevin.hackChecks.Check;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

// FlightCheck detect the player who is floating / flying / gliding
// It check for motionY
// All players' motionY is changed as (lastMotionY - 0.08D) * 0.98F  in air
// but in client, we cannot get others' accurate location
// we just make a simple check for this problem
public class FlightCheck extends Check {
    // use buffer to prevent false positives.
    // we are not the antiCheat for server, we haven't promised to mark all cheaters.
    // it likes VerusAC, work silent and accurate.
    // there is no meaningful of this check if we have lots of false.
    short buffer = 0, jumpBuffer = 0;
    boolean updated = false;
    public FlightCheck(EntityOtherPlayerMP playerMP) {
        super(playerMP);
        name = "Flight";
        checkViolationLevel = 20;
    }
    private double lastDeltaY = 0;

    @Override
    public void onLivingUpdate() {
        double deltaY = handlePlayer.posY - handlePlayer.prevPosY;
        if (handlePlayer.capabilities.isFlying) {
            lastDeltaY = deltaY;
            return;
        }
        if (updated) {
            // Float
            if (deltaY >= lastDeltaY) {
                BlockPos bp = new BlockPos(handlePlayer.prevPosX, handlePlayer.prevPosY - 0.25, handlePlayer.prevPosZ);
                if (!mc.theWorld.isBlockLoaded(bp) || (deltaY == lastDeltaY && Math.abs(deltaY - 3.2) < 1e-5)) return;
                if (mc.theWorld.isAirBlock(bp)) {
                    if (++buffer > 5) flag(String.format("glide/fly, d=(%.5f, %.5f)", lastDeltaY, deltaY), 2.3);
                } else {
                    --buffer;
                }
            }

            // Fast down
            if (deltaY < -3.2 && lastDeltaY > -3.2) {
                flag("fast down, mY=" + deltaY, 5);
            }

            // High jump
            double jumpMotion = 0.42;
            if (handlePlayer.isPotionActive(Potion.jump)) {
                jumpMotion += (handlePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1;
            }
            if (deltaY > jumpMotion) {
                // A buffer to prevent false
                // yes, it will only flag which player is over 0.5 motion jump or 0.42 jump but slow down,
                // so it will no false any player unless the connection is trash
                // false when step? yes if we don't use buffer, but we buffer it
                if (++jumpBuffer > 1) {
                    flag("high jump, p=(" + jumpMotion + "," + deltaY + ")", 4.5);
                    jumpBuffer = 0;
                }
            } else jumpBuffer = 0;
            reward(0.1);
            updated = false;
        }
        lastDeltaY = deltaY;
    }

    @Override
    public void positionUpdate(double x, double y, double z) {
        updated = true;
    }

    @Override
    public String description() {
        return "move vertical suspiciously";
    }
}

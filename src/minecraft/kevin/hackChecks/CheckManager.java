package kevin.hackChecks;

import kevin.hackChecks.checks.combat.AutoBlockCheck;
import kevin.hackChecks.checks.combat.KillAuraCheck;
import kevin.hackChecks.checks.move.FlightCheck;
import kevin.hackChecks.checks.move.NoSlowCheck;
import kevin.utils.ChatUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

public class CheckManager {
    private static final Class<?>[] checksClz = {
            AutoBlockCheck.class,
            KillAuraCheck.class,

            FlightCheck.class,
            NoSlowCheck.class
    };
    private final LinkedList<Check> checks = new LinkedList<>();
    public CheckManager(EntityOtherPlayerMP target) {
        for (Class<?> clz : checksClz) {
            try {
                checks.add((Check) clz.getConstructor(EntityOtherPlayerMP.class).newInstance(target));
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void livingUpdate() {
        for (Check check : checks) {
            try {
                check.onLivingUpdate();
                if (check.wasFailed()) {
                    ChatUtils.INSTANCE.message(String.format("§l§7[§l§9HackDetector§l§7]§r §4%s§7 maybe using §c%s§7 hack§8: §7%s", check.handlePlayer.getName(), check.name, check.description()));
                    check.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void positionUpdate(double x, double y, double z) {
        for (Check check : checks) {
            try {
                check.positionUpdate(x, y, z);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

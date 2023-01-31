package kevin.module.modules.misc;

import kevin.event.EventTarget;
import kevin.event.PacketEvent;
import kevin.event.UpdateEvent;
import kevin.hackChecks.Check;
import kevin.hackChecks.CheckManager;
import kevin.module.BooleanValue;
import kevin.module.Module;
import kevin.module.ModuleCategory;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import org.lwjgl.input.Keyboard;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HackDetector extends Module {
    public static final HackDetector INSTANCE = new HackDetector();
    public final ConcurrentHashMap<Integer, CheckManager> playersChecks = new ConcurrentHashMap<>();
    public ExecutorService es = Executors.newCachedThreadPool();
    private final BooleanValue debugValue = new BooleanValue("Debug", false) {
        @Override
        protected void onChanged(Boolean oldValue, Boolean newValue) {
            Check.debug = newValue;
        }
    };
    public HackDetector() {
        super("HackDetector", "Detect any player who is hacking in this game.", Keyboard.KEY_NONE, ModuleCategory.MISC);
    }

    @EventTarget
    public final void onUpdate(UpdateEvent ignored) {
        // async
        es.execute(() -> {
            // process check
            for (CheckManager manager : playersChecks.values()) {
                manager.livingUpdate();
            }

            // shit-code to remove unnecessary entity
            Enumeration<Integer> iter = playersChecks.keys();
            LinkedList<Integer> cache = new LinkedList<>();
            while (iter.hasMoreElements()) {
                Integer i = iter.nextElement();
                Entity e = mc.theWorld.getEntityByID(i);
                if (e == null || e.isDead) {
                    cache.add(i);
                }
            }
            for (Integer i : cache) {
                playersChecks.remove(i);
            }

            // add new player
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player instanceof EntityOtherPlayerMP && !playersChecks.containsKey(player.getEntityId()) && !player.isDead && player.getEntityId() != mc.thePlayer.getEntityId()) {
                    playersChecks.put(player.getEntityId(), new CheckManager((EntityOtherPlayerMP) player));
                }
            }
        });
    }

    @EventTarget
    public final void onPacket(PacketEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacket() instanceof S14PacketEntity || event.getPacket() instanceof S18PacketEntityTeleport) {
            // async
            es.execute(() -> {
                int x, y, z, id;
                if (event.getPacket() instanceof S14PacketEntity) {
                    S14PacketEntity packet = (S14PacketEntity) event.getPacket();
                    x = packet.func_149062_c();
                    y = packet.func_149061_d();
                    z = packet.func_149064_e();
                    id = packet.getEntityId();
                } else {
                    S18PacketEntityTeleport packet = (S18PacketEntityTeleport) event.getPacket();
                    Entity entityIn = mc.theWorld.getEntityByID(packet.getEntityId());
                    x = packet.getX() - entityIn.serverPosX;
                    y = packet.getY() - entityIn.serverPosY;
                    z = packet.getZ() - entityIn.serverPosZ;
                    id = packet.getEntityId();
                }
                playersChecks.get(id).positionUpdate(x / 32.0, y / 32.0, z / 32.0);
            });
        }
    }

    @Override
    public void onEnable() {
        Check.debug = debugValue.get();
    }

    @Override
    public void onDisable() {
        playersChecks.clear();
    }

    public static void removeCheck(int id) {
        INSTANCE.playersChecks.remove(id);
    }
}

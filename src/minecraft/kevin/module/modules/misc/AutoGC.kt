package kevin.module.modules.misc

import kevin.event.EventTarget
import kevin.event.TickEvent
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.MSTimer

object AutoGC: Module("AutoGC", "Automatic garbage collection", category = ModuleCategory.MISC) {
    private val msTimer = MSTimer()
    @EventTarget fun onTick(t: TickEvent) {
        if (msTimer.hasTimePassed(180000)) {
            System.gc()
            msTimer.reset()
        }
    }
}
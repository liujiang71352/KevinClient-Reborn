package kevin.event

import kevin.hud.element.elements.ConnectNotificationType
import kevin.hud.element.elements.Notification
import kevin.main.KevinClient
import net.minecraft.client.Minecraft
import java.lang.reflect.InvocationTargetException

class EventManager {
    private val registry = HashMap<Class<out Event>, MutableList<EventHook>>()
    fun registerListener(listener: Listenable) {
        for (method in listener.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(EventTarget::class.java) && method.parameterTypes.size == 1) {
                if (!method.isAccessible)
                    method.isAccessible = true

                val eventClass = method.parameterTypes[0] as Class<out Event>
                val eventTarget = method.getAnnotation(EventTarget::class.java)

                val invokableEventTargets = registry.getOrDefault(eventClass, ArrayList())
                invokableEventTargets.add(EventHook(listener, method, eventTarget))
                registry[eventClass] = invokableEventTargets
            }
        }
    }
    fun unregisterListener(listenable: Listenable) {
        for ((key, targets) in registry) {
            targets.removeIf { it.eventClass == listenable }

            registry[key] = targets
        }
    }
    fun callEvent(event: Event) {
        val targets = registry[event.javaClass] ?: return

        for (invokableEventTarget in targets) {
            try {
                if (!invokableEventTarget.eventClass.handleEvents() && !invokableEventTarget.isIgnoreCondition)
                    continue

                invokableEventTarget.method.invoke(invokableEventTarget.eventClass, event)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                if (KevinClient.debug) {
                    if (throwable is InvocationTargetException) {
                        KevinClient.hud.addNotification(
                            Notification(
                                "Exception caught when calling ${event.javaClass.simpleName} in ${invokableEventTarget.eventClass.javaClass.simpleName}: ${throwable.targetException.message}",
                                "Debug",
                                ConnectNotificationType.Error
                            )
                        )
                        Minecraft.logger.warn("Exception caught when calling ${event.javaClass.simpleName} in listener ${invokableEventTarget.eventClass.javaClass.simpleName}: ${throwable.targetException.stackTraceToString()}")
                    }
                }
            }
        }
    }
}
package dev.zwander.samsungflashlight

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentLinkedQueue

class EventManager private constructor(private val context: Context) {
    companion object {
        @Suppress("ObjectPropertyName")
        @SuppressLint("StaticFieldLeak")
        private var _instance: EventManager? = null

        fun getInstance(context: Context): EventManager {
            return _instance ?: EventManager(context.applicationContext ?: context).apply {
                _instance = this
            }
        }
    }

    private val listeners: MutableCollection<ListenerInfo<Event>> = ConcurrentLinkedQueue()
    private val observers: MutableCollection<EventObserver> = ConcurrentLinkedQueue()

    inline fun <reified T : Event> LifecycleOwner.registerListener(noinline listener: (T) -> Unit) {
        addListener(listener)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                removeListener(listener)
                lifecycle.removeObserver(this)
            }
        })
    }

    inline fun <reified T : Event> addListener(noinline listener: (T) -> Unit) {
        addListener(
            ListenerInfo(
                T::class.java,
                listener
            )
        )
    }

    fun <T : Event> addListener(listenerInfo: ListenerInfo<T>) {
        @Suppress("UNCHECKED_CAST")
        listeners.add(listenerInfo as ListenerInfo<Event>)
    }

    fun LifecycleOwner.registerObserver(observer: EventObserver) {
        addObserver(observer)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                removeObserver(observer)
                lifecycle.removeObserver(this)
            }
        })
    }

    fun addObserver(observer: EventObserver) {
        observers.add(observer)
    }

    inline fun <reified T : Event> removeListener(noinline listener: (T) -> Unit) {
        removeListener(
            ListenerInfo(
                T::class.java,
                listener
            )
        )
    }

    fun <T : Event> removeListener(listenerInfo: ListenerInfo<T>) {
        @Suppress("UNCHECKED_CAST")
        listeners.remove(listenerInfo as ListenerInfo<Event>)
    }

    fun removeObserver(observer: EventObserver) {
        observers.remove(observer)
    }

    fun sendEvent(event: Event) {
        observers.forEach {
            it.onEvent(event)
        }

        listeners.filter { it.listenerClass == event::class.java }
            .forEach {
                it.listener.invoke(event)
            }
    }
}

sealed class Event {
    data class FlashlightModeChange(val enabled: Boolean) : Event()
}

interface EventObserver {
    fun onEvent(event: Event)
}

data class ListenerInfo<T : Event>(
    val listenerClass: Class<T>,
    val listener: (T) -> Unit
)
package xyz.kukiteam.cubic.managers

import gnu.trove.map.TIntObjectMap
import gnu.trove.map.hash.TIntObjectHashMap
import xyz.kukiteam.cubic.objects.CallbackParameters
import xyz.kukiteam.cubic.objects.KeyEvent
import java.util.function.Consumer
import java.util.function.Predicate

class KeyManager {
    private var cache: TIntObjectMap<MutableList<KeyEvent>> = TIntObjectHashMap()

    fun add(key: Int, condition: Predicate<CallbackParameters>, function: Consumer<CallbackParameters>, expectedCallbackParameters: CallbackParameters) {
        add(key, KeyEvent(condition, function, expectedCallbackParameters))
    }

    /**
     * The task that should be registered on KEY with matching condition
     * @param key the key
     */
    fun add(key: Int, event: KeyEvent) {
        if (cache.containsKey(key)) {
            if (!cache[key].any { it.expectedCallbackParameters == event.expectedCallbackParameters })
                cache[key].add(event)
        } else {
            cache.put(key, mutableListOf(event))
        }
    }

    fun remove(key: Int, event: KeyEvent) {
        if (cache.containsKey(key))
            cache[key].remove(cache[key].find { it.expectedCallbackParameters == event.expectedCallbackParameters })
    }

    fun getBoundEvents(key: Int): List<KeyEvent> {
        return getBoundEvents(key, null)
    }

    fun getBoundEvents(key: Int, condition: Predicate<KeyEvent>?): List<KeyEvent> {
        var list = when {
            cache.containsKey(key) && condition == null -> cache[key]
            cache.containsKey(key) && condition != null -> cache[key].filter { condition.test(it) }
            else                                         -> emptyList()
        }
        return list.map { KeyEvent(it.condition.and { p -> p.key == key }, it.task, it.expectedCallbackParameters) }
    }

    fun getBoundEvents(condition: Predicate<KeyEvent>?): List<KeyEvent> {
        var list = ArrayList<KeyEvent>()
        for (key in cache.keySet()) {
            list.addAll(getBoundEvents(key, condition))
        }

        return list
    }

    fun hasEvents(callbackParameters: CallbackParameters): Boolean {
        if (!cache.containsKey(callbackParameters.key))
            return false
        return true
    }

    fun runEvents(callbackParameters: CallbackParameters) {
        for (task in cache[callbackParameters.key].stream().filter { it.condition.test(callbackParameters) }.map { it.task }) {
            task.accept(callbackParameters)
        }
    }
}
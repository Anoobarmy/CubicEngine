package xyz.kukiteam.cubic.objects

import java.util.function.Consumer
import java.util.function.Predicate

/**
 * Extend this class for your own implementation
 */
data class KeyEvent(val condition: Predicate<CallbackParameters>, val task: Consumer<CallbackParameters>, val expectedCallbackParameters: CallbackParameters) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            is KeyEvent -> condition == other.condition && task == other.task
            is Consumer<*> -> task == other
            else -> false
        }
    }

    override fun hashCode(): Int {
        return condition.hashCode() and task.hashCode()
    }
}
package xyz.kukiteam.cubic.objects

data class CallbackParameters(val key: Int, val window: Long, val action: Int, val mods: Int, val additionalParams: List<*> = ArrayList<Any>()) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            is CallbackParameters -> {
                val keys = if (key == -1 || other.key == -1) true else key == other.key
                val window = if (window == -1L || other.window == -1L) true else window == other.window
                val action = if (action == -1 || other.action == -1) true else action == other.action
                val mods = if (mods == -1 || other.mods == -1) true else mods == other.mods

                keys && window && action && mods
            }
            else -> false
        }
    }
}
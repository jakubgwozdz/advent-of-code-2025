package helpers.collections

class Cached<K, V>(val op: (K) -> V) {
    private val cache = mutableMapOf<K, V>()

    operator fun invoke(key: K): V {
        return cache.getOrPut(key) { op(key) }
    }

    operator fun contains(key: K): Boolean {
        return key in cache
    }
}

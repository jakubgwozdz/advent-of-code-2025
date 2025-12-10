package helpers.collections

class PriorityQueue<E : Any>(val comparator: Comparator<E>, vararg initial: E) {

    private var backingList = mutableListOf<E>().apply { addAll(initial) }

    val size get() = backingList.size
    fun isNotEmpty(): Boolean = size > 0

    fun removeFirst(): E {
        check(size > 0)
        return backingList.removeAt(0)
    }

    fun add(e: E) {
        val index = backingList.binarySearch(e, comparator).let {
            if (it < 0) -it - 1 else it
        }
        backingList.add(index, e)
    }

    fun toList() = backingList.toList()
    override fun toString(): String = backingList.toString()
}

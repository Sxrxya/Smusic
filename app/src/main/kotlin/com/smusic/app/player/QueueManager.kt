package com.smusic.app.player

import com.smusic.app.domain.model.RepeatMode
import com.smusic.app.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueManager @Inject constructor() {

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private var originalQueue: List<Song> = emptyList()
    private val historyStack = mutableListOf<Int>()

    val currentSong: Song?
        get() {
            val idx = _currentIndex.value
            val q = _queue.value
            return if (idx in q.indices) q[idx] else null
        }

    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        originalQueue = songs.toList()
        if (_shuffleEnabled.value) {
            val shuffled = songs.toMutableList()
            val startSong = songs.getOrNull(startIndex)
            shuffled.removeAt(startIndex)
            shuffled.shuffle()
            if (startSong != null) {
                shuffled.add(0, startSong)
            }
            _queue.value = shuffled
            _currentIndex.value = 0
        } else {
            _queue.value = songs.toList()
            _currentIndex.value = startIndex.coerceIn(0, songs.size - 1)
        }
        historyStack.clear()
    }

    fun addToQueue(song: Song) {
        val current = _queue.value.toMutableList()
        current.add(song)
        _queue.value = current
        if (!_shuffleEnabled.value) {
            originalQueue = current.toList()
        }
    }

    fun addNext(song: Song) {
        val current = _queue.value.toMutableList()
        val insertIdx = (_currentIndex.value + 1).coerceAtMost(current.size)
        current.add(insertIdx, song)
        _queue.value = current
        if (!_shuffleEnabled.value) {
            originalQueue = current.toList()
        }
    }

    fun removeFromQueue(index: Int) {
        val current = _queue.value.toMutableList()
        if (index !in current.indices) return
        current.removeAt(index)
        _queue.value = current

        val currentIdx = _currentIndex.value
        if (index < currentIdx) {
            _currentIndex.value = currentIdx - 1
        } else if (index == currentIdx && currentIdx >= current.size) {
            _currentIndex.value = (current.size - 1).coerceAtLeast(0)
        }
    }

    fun moveItem(from: Int, to: Int) {
        val current = _queue.value.toMutableList()
        if (from !in current.indices || to !in current.indices) return
        val item = current.removeAt(from)
        current.add(to, item)
        _queue.value = current

        val currentIdx = _currentIndex.value
        _currentIndex.value = when (currentIdx) {
            from -> to
            in (minOf(from, to)..maxOf(from, to)) -> {
                if (from < to) currentIdx - 1 else currentIdx + 1
            }
            else -> currentIdx
        }
    }

    fun next(): Song? {
        val q = _queue.value
        if (q.isEmpty()) return null

        historyStack.add(_currentIndex.value)

        val nextIndex = when (_repeatMode.value) {
            RepeatMode.REPEAT_ONE -> _currentIndex.value
            RepeatMode.REPEAT_ALL -> (_currentIndex.value + 1) % q.size
            RepeatMode.OFF -> {
                val next = _currentIndex.value + 1
                if (next >= q.size) return null
                next
            }
        }

        _currentIndex.value = nextIndex
        return q.getOrNull(nextIndex)
    }

    fun previous(): Song? {
        val q = _queue.value
        if (q.isEmpty()) return null

        if (historyStack.isNotEmpty()) {
            _currentIndex.value = historyStack.removeAt(historyStack.lastIndex)
        } else {
            val prevIndex = when (_repeatMode.value) {
                RepeatMode.REPEAT_ALL -> {
                    if (_currentIndex.value - 1 < 0) q.size - 1
                    else _currentIndex.value - 1
                }
                else -> (_currentIndex.value - 1).coerceAtLeast(0)
            }
            _currentIndex.value = prevIndex
        }
        return q.getOrNull(_currentIndex.value)
    }

    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
        val q = _queue.value
        val currentSong = q.getOrNull(_currentIndex.value) ?: return

        if (_shuffleEnabled.value) {
            // Fisher-Yates shuffle, keeping current song first
            val shuffled = q.toMutableList()
            shuffled.remove(currentSong)
            shuffled.shuffle()
            shuffled.add(0, currentSong)
            _queue.value = shuffled
            _currentIndex.value = 0
        } else {
            // Restore original order
            _queue.value = originalQueue.toList()
            _currentIndex.value = originalQueue.indexOf(currentSong).coerceAtLeast(0)
        }
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.OFF
        }
    }

    fun hasNext(): Boolean {
        return when (_repeatMode.value) {
            RepeatMode.REPEAT_ALL, RepeatMode.REPEAT_ONE -> true
            RepeatMode.OFF -> _currentIndex.value < _queue.value.size - 1
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = -1
        originalQueue = emptyList()
        historyStack.clear()
    }

    fun skipTo(index: Int): Song? {
        val q = _queue.value
        if (index !in q.indices) return null
        historyStack.add(_currentIndex.value)
        _currentIndex.value = index
        return q[index]
    }
}

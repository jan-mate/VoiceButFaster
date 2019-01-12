@file:Suppress("EXPERIMENTAL_API_USAGE")

package de.ph1b.audiobook.misc

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import de.ph1b.audiobook.playback.utils.DataSourceConverter
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class DurationAnalyzer
@Inject constructor(
  private val dataSourceConverter: DataSourceConverter,
  private val player: SimpleExoPlayer
) {

  private val playbackState = ConflatedBroadcastChannel(player.playbackState)

  init {
    player.addListener(
      object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
          this@DurationAnalyzer.playbackState.offer(playbackState)
        }
      }
    )
  }

  suspend fun duration(file: File): Int? {
    waitForIdle()
    return withTimeoutOrNull(3000) {
      scan(file).takeIf { it != null && it > 0 }
    }
  }

  private suspend fun waitForIdle() {
    val playState = playbackState.value
    if (playState != Player.STATE_IDLE) {
      withContext(Main) {
        player.stop()
        Timber.v("will stop player. because state is ${stateName[playState]}")
      }
      playbackState.openSubscription()
        .first {
          Timber.v("state is ${stateName[it]}")
          it == Player.STATE_IDLE
        }
    }
  }

  private suspend fun scan(file: File): Int? {
    Timber.v("scan $file start")
    val mediaSource = dataSourceConverter.toMediaSource(file)
    withContext(Main) {
      player.prepare(mediaSource)
    }
    Timber.v("scan, prepared $file.")
    playbackState.openSubscription()
      .first {
        Timber.v("scan, state=${stateName[it]}")
        when (it) {
          Player.STATE_READY -> true
          Player.STATE_ENDED, Player.STATE_IDLE -> {
            Timber.e("Couldn't prepare. Return no duration.")
            return null
          }
          else -> false
        }
      }

    return withContext(Main) {
      if (!player.isCurrentWindowSeekable) {
        Timber.d("file $file is not seekable")
      }
      val duration = player.duration
      try {
        if (duration == C.TIME_UNSET) {
          null
        } else {
          duration.toInt()
        }
      } finally {
        Timber.v("scan $file stop")
        player.stop()
      }
    }
  }

  private val stateName = mapOf(
    Player.STATE_READY to "ready",
    Player.STATE_ENDED to "ended",
    Player.STATE_IDLE to "idle",
    Player.STATE_BUFFERING to "buffering"
  )
}

package de.ph1b.audiobook.playback

import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat.TransportControls
import android.support.v4.media.session.MediaSessionCompat
import de.ph1b.audiobook.BuildConfig
import de.ph1b.audiobook.features.bookSearch.BookSearchHandler
import de.ph1b.audiobook.features.bookSearch.BookSearchParser
import de.ph1b.audiobook.injection.PerService
import de.ph1b.audiobook.injection.PrefKeys
import de.ph1b.audiobook.persistence.pref.Pref
import de.ph1b.audiobook.playback.utils.BookUriConverter
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

/**
 * Media session callback that handles playback controls.
 */
@PerService
class MediaSessionCallback
@Inject constructor(
  private val bookUriConverter: BookUriConverter,
  @Named(PrefKeys.CURRENT_BOOK)
  private val currentBookIdPref: Pref<UUID>,
  private val bookSearchHandler: BookSearchHandler,
  private val autoConnection: AndroidAutoConnectedReceiver,
  private val bookSearchParser: BookSearchParser,
  private val player: MediaPlayer
) : MediaSessionCompat.Callback() {

  override fun onSkipToQueueItem(id: Long) {
    Timber.i("onSkipToQueueItem $id")
    val chapter = player.bookContent
      ?.chapters?.getOrNull(id.toInt()) ?: return
    player.changePosition(0, chapter.file)
    player.play()
  }

  override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
    Timber.i("onPlayFromMediaId $mediaId")
    mediaId ?: return
    val parsed = bookUriConverter.parse(mediaId)
    if (parsed is BookUriConverter.Parsed.Book) {
      currentBookIdPref.value = parsed.id
      onPlay()
    } else {
      Timber.e("Didn't handle $parsed")
    }
  }

  override fun onPlayFromSearch(query: String?, extras: Bundle?) {
    Timber.i("onPlayFromSearch $query")
    val bookSearch = bookSearchParser.parse(query, extras)
    bookSearchHandler.handle(bookSearch)
  }

  override fun onSkipToNext() {
    Timber.i("onSkipToNext")
    if (autoConnection.connected) {
      player.next()
    } else {
      onFastForward()
    }
  }

  override fun onRewind() {
    Timber.i("onRewind")
    player.skip(forward = false)
    player.play()
  }

  override fun onSkipToPrevious() {
    Timber.i("onSkipToPrevious")
    if (autoConnection.connected) {
      player.previous(toNullOfNewTrack = true)
    } else {
      onRewind()
    }
  }

  override fun onFastForward() {
    Timber.i("onFastForward")
    player.skip(forward = true)
  }

  override fun onStop() {
    Timber.i("onStop")
    player.stop()
  }

  override fun onPause() {
    Timber.i("onPause")
    player.pause(rewind = true)
  }

  override fun onPlay() {
    Timber.i("onPlay")
    player.play()
  }

  override fun onSeekTo(pos: Long) {
    player.changePosition(pos)
  }

  override fun onSetPlaybackSpeed(speed: Float) {
    player.setPlaybackSpeed(speed)
  }

  override fun onCustomAction(action: String?, extras: Bundle?) {
    Timber.i("onCustomAction $action")
    when (action) {
      ANDROID_AUTO_ACTION_NEXT -> onSkipToNext()
      ANDROID_AUTO_ACTION_PREVIOUS -> onSkipToPrevious()
      ANDROID_AUTO_ACTION_FAST_FORWARD -> onFastForward()
      ANDROID_AUTO_ACTION_REWIND -> onRewind()
      PLAY_PAUSE_ACTION -> player.playPause()
      SKIP_SILENCE_ACTION -> {
        val skip = extras!!.getBoolean(SKIP_SILENCE_EXTRA)
        player.setSkipSilences(skip)
      }
      SET_LOUDNESS_GAIN_ACTION -> {
        val mB = extras!!.getInt(SET_LOUDNESS_GAIN_EXTRA_MB)
        player.setLoudnessGain(mB)
      }
      SET_POSITION_ACTION -> {
        val file = File(extras!!.getString(SET_POSITION_EXTRA_FILE)!!)
        val time = extras.getLong(SET_POSITION_EXTRA_TIME)
        player.changePosition(time, file)
      }
      else -> if (BuildConfig.DEBUG) {
        error("Didn't handle $action")
      }
    }
  }
}

private inline fun TransportControls.sendCustomAction(action: String, fillBundle: Bundle.() -> Unit = {}) {
  sendCustomAction(action, Bundle().apply(fillBundle))
}

private const val PLAY_PAUSE_ACTION = "playPause"

fun TransportControls.playPause() = sendCustomAction(PLAY_PAUSE_ACTION)

private const val SKIP_SILENCE_ACTION = "skipSilence"
private const val SKIP_SILENCE_EXTRA = "$SKIP_SILENCE_ACTION#value"

fun TransportControls.skipSilence(skip: Boolean) = sendCustomAction(SKIP_SILENCE_ACTION) {
  putBoolean(SKIP_SILENCE_EXTRA, skip)
}

private const val SET_LOUDNESS_GAIN_ACTION = "setLoudnessGain"
private const val SET_LOUDNESS_GAIN_EXTRA_MB = "$SET_LOUDNESS_GAIN_ACTION#mb"

fun TransportControls.setLoudnessGain(mB: Int) = sendCustomAction(SET_LOUDNESS_GAIN_ACTION) {
  putInt(SET_LOUDNESS_GAIN_EXTRA_MB, mB)
}

private const val SET_POSITION_ACTION = "setPosition"
private const val SET_POSITION_EXTRA_TIME = "$SET_POSITION_ACTION#time"
private const val SET_POSITION_EXTRA_FILE = "$SET_POSITION_ACTION#file"

fun TransportControls.setPosition(time: Long, file: File) = sendCustomAction(SET_POSITION_ACTION) {
  putString(SET_POSITION_EXTRA_FILE, file.absolutePath)
  putLong(SET_POSITION_EXTRA_TIME, time)
}

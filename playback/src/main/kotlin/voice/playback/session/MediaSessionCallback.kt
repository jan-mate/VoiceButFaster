package voice.playback.session

import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat.TransportControls
import android.support.v4.media.session.MediaSessionCompat
import androidx.datastore.core.DataStore
import kotlinx.coroutines.runBlocking
import voice.common.pref.CurrentBook
import voice.data.Book
import voice.data.Chapter
import voice.logging.core.Logger
import voice.playback.BuildConfig
import voice.playback.androidauto.AndroidAutoConnectedReceiver
import voice.playback.di.PlaybackScope
import voice.playback.player.MediaPlayer
import voice.playback.session.search.BookSearchHandler
import voice.playback.session.search.BookSearchParser
import javax.inject.Inject

/**
 * Media session callback that handles playback controls.
 */
@PlaybackScope
class MediaSessionCallback
@Inject constructor(
  private val bookUriConverter: BookUriConverter,
  @CurrentBook
  private val currentBook: DataStore<Book.Id?>,
  private val bookSearchHandler: BookSearchHandler,
  private val autoConnection: AndroidAutoConnectedReceiver,
  private val bookSearchParser: BookSearchParser,
  private val player: MediaPlayer,
) : MediaSessionCompat.Callback() {

  override fun onSkipToQueueItem(id: Long) {
    Logger.i("onSkipToQueueItem $id")
    val chapter = player.book
      ?.chapters?.getOrNull(id.toInt()) ?: return
    player.changePosition(0, chapter.id)
    player.play()
  }

  override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
    Logger.i("onPlayFromMediaId $mediaId")
    mediaId ?: return
    when (val parsed = bookUriConverter.parse(mediaId)) {
      is BookUriConverter.Parsed.Book -> {
        runBlocking {
          currentBook.updateData { parsed.bookId }
        }
        onPlay()
      }
      is BookUriConverter.Parsed.Chapter -> {
        runBlocking {
          currentBook.updateData { parsed.bookId }
        }
        player.changePosition(parsed.chapterId)
        onPlay()
      }
      BookUriConverter.Parsed.AllBooks -> {
        Logger.w("Didn't handle $parsed")
      }
      null -> {}
    }
  }

  override fun onPlayFromSearch(query: String?, extras: Bundle?) {
    Logger.i("onPlayFromSearch $query")
    val bookSearch = bookSearchParser.parse(query, extras)
    runBlocking {
      bookSearchHandler.handle(bookSearch)
    }
  }

  override fun onSkipToNext() {
    Logger.i("onSkipToNext")
    if (autoConnection.connected) {
      player.next()
    } else {
      onFastForward()
    }
  }

  override fun onRewind() {
    Logger.i("onRewind")
    player.skip(forward = false)
  }

  override fun onSkipToPrevious() {
    Logger.i("onSkipToPrevious")
    if (autoConnection.connected) {
      player.previous(toNullOfNewTrack = true)
    } else {
      onRewind()
    }
  }

  override fun onFastForward() {
    Logger.i("onFastForward")
    player.skip(forward = true)
  }

  override fun onStop() {
    Logger.i("onStop")
    player.stop()
  }

  override fun onPause() {
    Logger.i("onPause")
    player.pause(rewind = true)
  }

  override fun onPlay() {
    Logger.i("onPlay")
    player.play()
  }

  override fun onSeekTo(pos: Long) {
    player.changePosition(pos)
  }

  override fun onSetPlaybackSpeed(speed: Float) {
    player.setPlaybackSpeed(speed)
  }

  override fun onCustomAction(action: String?, extras: Bundle?) {
    Logger.i("onCustomAction $action")
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
      SET_POSITION_ACTION -> {
        val id = Chapter.Id(extras!!.getString(SET_POSITION_EXTRA_CHAPTER)!!)
        val time = extras.getLong(SET_POSITION_EXTRA_TIME)
        player.changePosition(time, id)
      }
      FORCED_PREVIOUS -> {
        player.previous(toNullOfNewTrack = true)
      }
      FORCED_NEXT -> {
        player.next()
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

private const val SET_POSITION_ACTION = "setPosition"
private const val SET_POSITION_EXTRA_TIME = "$SET_POSITION_ACTION#time"
private const val SET_POSITION_EXTRA_CHAPTER = "$SET_POSITION_ACTION#uri"

fun TransportControls.setPosition(time: Long, id: Chapter.Id) = sendCustomAction(SET_POSITION_ACTION) {
  putString(SET_POSITION_EXTRA_CHAPTER, id.value)
  putLong(SET_POSITION_EXTRA_TIME, time)
}

const val ANDROID_AUTO_ACTION_FAST_FORWARD = "fast_forward"
const val ANDROID_AUTO_ACTION_REWIND = "rewind"
const val ANDROID_AUTO_ACTION_NEXT = "next"
const val ANDROID_AUTO_ACTION_PREVIOUS = "previous"

private const val FORCED_PREVIOUS = "forcedPrevious"
fun TransportControls.forcedPrevious() = sendCustomAction(FORCED_PREVIOUS)

private const val FORCED_NEXT = "forcedNext"
fun TransportControls.forcedNext() = sendCustomAction(FORCED_NEXT)

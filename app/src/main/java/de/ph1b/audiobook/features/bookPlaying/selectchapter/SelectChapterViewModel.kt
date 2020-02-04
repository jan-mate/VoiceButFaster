package de.ph1b.audiobook.features.bookPlaying.selectchapter

import de.ph1b.audiobook.data.currentMark
import de.ph1b.audiobook.data.repo.BookRepository
import de.ph1b.audiobook.playback.PlayerController
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class SelectChapterViewModel
@Inject constructor(
  private val bookRepository: BookRepository,
  private val player: PlayerController
) {

  private val _viewEffects = BroadcastChannel<SelectChapterViewEffect>(1)
  val viewEffects: Flow<SelectChapterViewEffect> get() = _viewEffects.asFlow()

  lateinit var bookId: UUID

  fun viewState(): SelectChapterViewState {
    val book = bookRepository.bookById(bookId)

    if (book == null) {
      Timber.d("no book found for $bookId. CloseScreen")
      _viewEffects.offer(SelectChapterViewEffect.CloseScreen)
      return SelectChapterViewState(emptyList(), null)
    }

    val chapterMarks = book.content.chapters.flatMap {
      it.chapterMarks
    }
    val currentMark = book.content.currentChapter.currentMark(book.content.positionInChapter)
    val selectedIndex = chapterMarks.indexOf(currentMark)
    return SelectChapterViewState(chapterMarks, selectedIndex.takeUnless { it == -1 })
  }

  fun chapterClicked(index: Int) {
    val book = bookRepository.bookById(bookId) ?: return
    var currentIndex = -1
    book.content.chapters.forEach { chapter ->
      chapter.chapterMarks.forEach { mark ->
        currentIndex++
        if (currentIndex == index) {
          player.setPosition(mark.startMs, chapter.file)
          _viewEffects.offer(SelectChapterViewEffect.CloseScreen)
          return
        }
      }
    }
  }
}

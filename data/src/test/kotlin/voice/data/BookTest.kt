package voice.data

import io.kotest.matchers.longs.shouldBeExactly
import org.junit.Test

class BookTest {

  @Test
  fun bookPositionForSingleFile() {
    val chapter = chapter(1000)
    val position = bookPosition(chapters = listOf(chapter), currentChapter = chapter.id, positionInChapter = 500)
    position shouldBeExactly 500
  }

  @Test
  fun bookPositionForFirstChapterInMultipleFiles() {
    val chapterOne = chapter(1000)
    val chapterTwo = chapter(500)
    val position = bookPosition(chapters = listOf(chapterOne, chapterTwo), currentChapter = chapterOne.id, positionInChapter = 500)
    position shouldBeExactly 500
  }

  @Test
  fun bookPositionForLastChapterInMultipleFiles() {
    val chapterOne = chapter(1000)
    val chapterTwo = chapter(500)
    val position = bookPosition(chapters = listOf(chapterOne, chapterTwo), currentChapter = chapterTwo.id, positionInChapter = 500)
    position shouldBeExactly 1500
  }


  @Test
  fun globalPositionWhenTimeIs0AndCurrentFileIsFirst() {
    val chapters = listOf(chapter(duration = 12345), chapter())
    val book = book(
      time = 0,
      chapters = chapters,
      currentChapter = chapters.first().id,
    )
    book.assertThat().positionIs(0)
  }

  @Test
  fun globalPositionWhenTimeIsNot0AndCurrentFileIsFirst() {
    val chapters = listOf(chapter(duration = 12345), chapter())
    val book = book(
      time = 23,
      chapters = chapters,
      currentChapter = chapters.first().id,
    )
    book.assertThat().positionIs(23)
  }

  @Test
  fun globalPositionWhenTimeIs0AndCurrentFileIsNotFirst() {
    val lastChapterId = Chapter.Id("lastChapter")
    val book = book(
      time = 0,
      chapters = listOf(
        chapter(duration = 123),
        chapter(duration = 234),
        chapter(duration = 345),
        chapter(duration = 456, id = lastChapterId)
      ),
      currentChapter = lastChapterId,
    )
    book.assertThat().positionIs(123 + 234 + 345)
  }

  @Test
  fun globalPositionWhenTimeIsNot0AndCurrentFileIsNotFirst() {
    val targetChapter = Chapter.Id("target")
    val book = book(
      time = 23,
      chapters = listOf(
        chapter(duration = 123),
        chapter(duration = 234),
        chapter(duration = 345, id = targetChapter),
        chapter(duration = 456)
      ),
      currentChapter = targetChapter,
    )
    book.assertThat().positionIs(123 + 234 + 23)
  }

  @Test
  fun totalDuration() {
    val book = book(
      chapters = listOf(
        chapter(duration = 123),
        chapter(duration = 234),
        chapter(duration = 345),
        chapter(duration = 456)
      ),
    )

    book.assertThat().durationIs(123 + 234 + 345 + 456)
  }

  @Test
  fun currentChapter() {
    val ch1 = chapter()
    val ch2 = chapter()
    val ch3 = chapter()
    val book = book(
      chapters = listOf(ch1, ch2, ch3),
      currentChapter = ch2.id,
    )
    book.assertThat().currentChapterIs(ch2)
  }

  @Test
  fun currentChapterIndex() {
    val ch1 = chapter()
    val ch2 = chapter()
    val ch3 = chapter()
    val book = book(
      chapters = listOf(ch1, ch2, ch3),
      currentChapter = ch2.id
    )

    book.assertThat().currentChapterIndexIs(1)
  }

  @Test
  fun nextChapterOnNonLastChapter() {
    val ch1 = chapter()
    val ch2 = chapter()
    val ch3 = chapter()
    val book = book(
      chapters = listOf(ch1, ch2, ch3),
      currentChapter = ch2.id
    )

    book.assertThat().nextChapterIs(ch3)
  }

  @Test
  fun nextChapterOnLastChapter() {
    val ch1 = chapter()
    val ch2 = chapter()
    val ch3 = chapter()
    val book = book(
      chapters = listOf(ch1, ch2, ch3),
      currentChapter = ch3.id
    )

    book.assertThat().nextChapterIs(null)
  }

  @Test
  fun previousChapterOnNonFirstChapter() {
    val ch1 = chapter()
    val ch2 = chapter()
    val ch3 = chapter()
    val book = book(
      chapters = listOf(ch1, ch2, ch3),
      currentChapter = ch2.id,
    )
    book.assertThat().previousChapterIs(ch1)
  }

  @Test
  fun previousChapterOnFirstChapter() {
    val ch1 = chapter()
    val ch2 = chapter()
    val ch3 = chapter()
    val book = book(
      chapters = listOf(ch1, ch2, ch3),
      currentChapter = ch1.id,
    )
    book.assertThat().previousChapterIs(null)
  }

  @Suppress("SameParameterValue")
  private fun bookPosition(chapters: List<Chapter>, currentChapter: Chapter.Id, positionInChapter: Long): Long {
    return book(
      chapters = chapters,
      time = positionInChapter,
      currentChapter = currentChapter
    ).position
  }
}


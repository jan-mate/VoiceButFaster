package de.ph1b.audiobook

import de.ph1b.audiobook.data.Book
import de.ph1b.audiobook.data.BookContent
import de.ph1b.audiobook.data.BookMetaData
import de.ph1b.audiobook.data.Chapter

object BookFactory {

  fun create(
    id: Long = -1,
    type: Book.Type = Book.Type.SINGLE_FOLDER,
    author: String = "TestAuthor",
    currentFileIndex: Int = 0,
    time: Int = 0,
    name: String = "TestBook",
    playbackSpeed: Float = 1F,
    loudnessGain: Int = 500,
    skipSilence: Boolean = false,
    chapters: List<Chapter> = listOf(ChapterFactory.create())
  ): Book {

    val currentFile = chapters[currentFileIndex].file
    val root = if (currentFile.parent != null) currentFile.parent else "fakeRoot"

    return Book(
      id = id,
      metaData = BookMetaData(
        id = id,
        type = type,
        author = author,
        name = name,
        root = root
      ),
      content = BookContent(
        id = id,
        currentFile = currentFile,
        positionInChapter = time,
        chapters = chapters,
        playbackSpeed = playbackSpeed,
        loudnessGain = loudnessGain,
        skipSilence = skipSilence
      )
    )
  }
}

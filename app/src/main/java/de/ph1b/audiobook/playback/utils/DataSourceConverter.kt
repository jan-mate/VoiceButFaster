package de.ph1b.audiobook.playback.utils

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.Reusable
import de.ph1b.audiobook.data.BookContent
import de.ph1b.audiobook.data.Chapter
import de.ph1b.audiobook.playback.exoPlayerFix.FixedExtractorMediaSource
import java.io.File
import javax.inject.Inject

/**
 * Converts books to media sources.
 */
@Reusable
class DataSourceConverter
@Inject constructor(context: Context) {

  private val mediaSourceFactory: FixedExtractorMediaSource.Factory

  init {
    val dataSourceFactory = DefaultDataSourceFactory(context, context.packageName)
    val extractorsFactory = DefaultExtractorsFactory()
      .setConstantBitrateSeekingEnabled(true)
    mediaSourceFactory = FixedExtractorMediaSource.Factory(dataSourceFactory)
      .setExtractorsFactory(extractorsFactory)
  }

  private fun Chapter.toMediaSource(): MediaSource = toMediaSource(file)

  fun toMediaSource(file: File): MediaSource {
    return mediaSourceFactory.createMediaSource(Uri.fromFile(file))
  }

  /** convert a content to a media source. If the size is > 1 use a concat media source, else a regular */
  fun toMediaSource(content: BookContent): MediaSource {
    return if (content.chapters.size > 1) {
      val allSources = content.chapters.map {
        it.toMediaSource()
      }
      ConcatenatingMediaSource(*allSources.toTypedArray())
    } else content.currentChapter.toMediaSource()
  }
}

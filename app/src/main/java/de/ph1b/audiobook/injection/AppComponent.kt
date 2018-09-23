package de.ph1b.audiobook.injection

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import de.ph1b.audiobook.data.di.DataComponent
import de.ph1b.audiobook.data.repo.internals.PersistenceModule
import de.ph1b.audiobook.features.MainActivity
import de.ph1b.audiobook.features.audio.LoudnessDialog
import de.ph1b.audiobook.features.bookOverview.BookOverviewController
import de.ph1b.audiobook.features.bookOverview.EditBookBottomSheet
import de.ph1b.audiobook.features.bookOverview.EditBookTitleDialogFragment
import de.ph1b.audiobook.features.bookOverview.EditCoverDialogFragment
import de.ph1b.audiobook.features.bookOverview.list.LoadBookCover
import de.ph1b.audiobook.features.bookPlaying.BookPlayController
import de.ph1b.audiobook.features.bookPlaying.BookPlayPresenter
import de.ph1b.audiobook.features.bookPlaying.JumpToPositionDialogFragment
import de.ph1b.audiobook.features.bookPlaying.SeekDialogFragment
import de.ph1b.audiobook.features.bookPlaying.SleepTimerDialogFragment
import de.ph1b.audiobook.features.bookmarks.BookmarkPresenter
import de.ph1b.audiobook.features.folderChooser.FolderChooserPresenter
import de.ph1b.audiobook.features.folderOverview.FolderOverviewPresenter
import de.ph1b.audiobook.features.imagepicker.ImagePickerController
import de.ph1b.audiobook.features.settings.SettingsController
import de.ph1b.audiobook.features.settings.dialogs.AutoRewindDialogFragment
import de.ph1b.audiobook.features.settings.dialogs.PlaybackSpeedDialogFragment
import de.ph1b.audiobook.features.settings.dialogs.ThemePickerDialogFragment
import de.ph1b.audiobook.features.widget.BaseWidgetProvider
import de.ph1b.audiobook.persistence.pref.Pref
import de.ph1b.audiobook.playback.MediaPlayer
import de.ph1b.audiobook.playback.PlayStateManager
import javax.inject.Named
import javax.inject.Singleton

/**
 * Base component that is the entry point for injection.
 */
@Singleton
@Component(
  modules = [
    AndroidModule::class,
    PrefsModule::class,
    BindingModule::class,
    AndroidSupportInjectionModule::class,
    PersistenceModule::class
  ]
)
interface AppComponent : DataComponent {

  val bookmarkPresenter: BookmarkPresenter
  val context: Context
  val player: MediaPlayer
  val playStateManager: PlayStateManager
  @get:Named(PrefKeys.CRASH_REPORT_ENABLED)
  val allowCrashReports: Pref<Boolean>

  @Component.Builder
  interface Builder {

    @BindsInstance
    fun application(application: Application): Builder

    fun build(): AppComponent
  }

  fun inject(target: App)
  fun inject(target: AutoRewindDialogFragment)
  fun inject(target: BaseWidgetProvider)
  fun inject(target: BookOverviewController)
  fun inject(target: BookPlayController)
  fun inject(target: BookPlayPresenter)
  fun inject(target: EditBookBottomSheet)
  fun inject(target: EditBookTitleDialogFragment)
  fun inject(target: EditCoverDialogFragment)
  fun inject(target: FolderChooserPresenter)
  fun inject(target: FolderOverviewPresenter)
  fun inject(target: ImagePickerController)
  fun inject(target: JumpToPositionDialogFragment)
  fun inject(target: LoadBookCover)
  fun inject(target: LoudnessDialog)
  fun inject(target: MainActivity)
  fun inject(target: PlaybackSpeedDialogFragment)
  fun inject(target: SeekDialogFragment)
  fun inject(target: SettingsController)
  fun inject(target: SleepTimerDialogFragment)
  fun inject(target: ThemePickerDialogFragment)
}

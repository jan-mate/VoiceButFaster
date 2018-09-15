package de.ph1b.audiobook.features.settings.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import de.ph1b.audiobook.R
import de.ph1b.audiobook.injection.PrefKeys
import de.ph1b.audiobook.misc.DialogLayoutContainer
import de.ph1b.audiobook.misc.inflate
import de.ph1b.audiobook.misc.onProgressChanged
import de.ph1b.audiobook.persistence.pref.Pref
import kotlinx.android.synthetic.main.dialog_amount_chooser.*
import org.koin.android.ext.android.inject

class AutoRewindDialogFragment : DialogFragment() {

  private val autoRewindAmountPref: Pref<Int> by inject(PrefKeys.AUTO_REWIND_AMOUNT)

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val container =
      DialogLayoutContainer(activity!!.layoutInflater.inflate(R.layout.dialog_amount_chooser))

    val oldRewindAmount = autoRewindAmountPref.value
    container.seekBar.max = (MAX - MIN) * FACTOR
    container.seekBar.progress = (oldRewindAmount - MIN) * FACTOR
    container.seekBar.onProgressChanged(initialNotification = true) {
      val progress = it / FACTOR
      val autoRewindSummary = context!!.resources.getQuantityString(
        R.plurals.pref_auto_rewind_summary,
        progress,
        progress
      )
      container.textView.text = autoRewindSummary
    }

    return MaterialDialog.Builder(context!!)
      .title(R.string.pref_auto_rewind_title)
      .customView(container.containerView, true)
      .positiveText(R.string.dialog_confirm)
      .negativeText(R.string.dialog_cancel)
      .onPositive { _, _ ->
        val newRewindAmount = container.seekBar.progress / FACTOR + MIN
        autoRewindAmountPref.value = newRewindAmount
      }
      .build()
  }

  companion object {
    val TAG: String = AutoRewindDialogFragment::class.java.simpleName

    private const val MIN = 0
    private const val MAX = 20
    private const val FACTOR = 10
  }
}

package de.ph1b.audiobook.uitools

import android.content.Context
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import de.ph1b.audiobook.R

object ThemeUtil {

  @AnyRes
  fun getResourceId(c: Context, @AttrRes attr: Int): Int {
    val ta = c.obtainStyledAttributes(intArrayOf(attr))
    val resId = ta.getResourceId(0, -1)
    ta.recycle()
    require(resId != -1) { "Resource with attr=$attr not found" }
    return resId
  }

  enum class Theme(@StringRes val nameId: Int) {
    DAY_NIGHT(R.string.pref_theme_daynight),
    DAY(R.string.pref_theme_day),
    NIGHT(R.string.pref_theme_night)
  }
}

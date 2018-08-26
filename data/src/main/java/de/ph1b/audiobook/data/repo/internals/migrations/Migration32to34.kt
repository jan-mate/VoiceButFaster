package de.ph1b.audiobook.data.repo.internals.migrations

import android.annotation.SuppressLint
import android.content.ContentValues
import android.provider.BaseColumns
import androidx.room.OnConflictStrategy
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.ph1b.audiobook.data.repo.internals.getLong
import de.ph1b.audiobook.data.repo.internals.getString
import de.ph1b.audiobook.data.repo.internals.mapRows
import de.ph1b.audiobook.data.repo.internals.transaction
import timber.log.Timber

private const val BOOKMARK_TABLE_NAME = "tableBookmarks"
private const val BM_PATH = "bookmarkPath"
private const val BM_TITLE = "bookmarkTitle"
private const val BM_TIME = "bookmarkTime"

private const val PATH = "bookmarkPath"
private const val TITLE = "bookmarkTitle"
private const val TABLE_NAME = "tableBookmarks"
private const val TIME = "bookmarkTime"
private const val ID = BaseColumns._ID
private const val CREATE_TABLE_BOOKMARKS = """
    CREATE TABLE $TABLE_NAME (
      $ID INTEGER PRIMARY KEY AUTOINCREMENT,
      $PATH TEXT NOT NULL,
      $TITLE TEXT NOT NULL,
      $TIME INTEGER NOT NULL
    )
  """

class Migration32to34 : Migration(32, 34) {

  @SuppressLint("Recycle")
  override fun migrate(db: SupportSQLiteDatabase) {
    // retrieve old bookmarks
    val cursor = db.query("SELECT * FROM BOOKMARK_TABLE_NAME")
    val entries = cursor.mapRows {
      val path = getString(BM_PATH)
      val title = getString(BM_TITLE)
      val time = getLong(BM_TIME)
      Holder(path, title, time)
    }
    Timber.i("Restored bookmarks=$entries")

    // delete table
    db.execSQL("DROP TABLE $BOOKMARK_TABLE_NAME")

    // create new bookmark scheme
    db.execSQL(CREATE_TABLE_BOOKMARKS)
    Timber.i("Created $CREATE_TABLE_BOOKMARKS")

    // add old bookmarks to new bookmark scheme
    db.transaction {
      entries.forEach {
        val cv = ContentValues().apply {
          put(PATH, it.path)
          put(TITLE, it.title)
          put(TIME, it.time)
        }
        db.insert(TABLE_NAME, OnConflictStrategy.FAIL, cv)
        Timber.i("Inserted $cv to $TABLE_NAME")
      }
    }
  }

  private data class Holder(val path: String, val title: String, val time: Long)
}

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.AppDatabase

fun checkpoint(db: AppDatabase) {
    db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
        if (cursor.moveToFirst()) {
            val isBusy = cursor.getInt(0)
            val log = cursor.getInt(1)
            val checkpointed = cursor.getInt(2)
        }
    }
}

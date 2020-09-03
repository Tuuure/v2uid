package app.tuuure.v2uid

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: CharSequence,
    val packageName: String,
    val uid: Int,
    val icon: Drawable,
    val flags: Int,
    var isChecked: Boolean = false
)
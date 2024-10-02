package com.uptick.sdk

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.text.set

fun Int.pxToDp(): Int {
    return (this / Resources.getSystem().displayMetrics.density).toInt()
}

fun Int.dpToPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Context.openLink(link: String) {
    var url = link
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "http://" + url
    }
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

fun Spannable.clickableSpan(text: String, on: () -> Unit) {
    val start = this.indexOf(text)
    this[start..start + text.length] = object : ClickableSpan() {
        override fun onClick(p0: View) {
            on()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = true

        }
    }
}

fun Spannable.boldSpan(text: String) {
    setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}
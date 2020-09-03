package app.tuuure.v2uid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_main.toolBar


class AboutActivity : AppCompatActivity() {
    companion object {
        private const val github_url = "https://github.com/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setUrlText(about_thanks_2dust, getString(R.string.about_thanks_2dust))

        setUrlText(about_thanks_sirpryderi, getString(R.string.about_thanks_sirpryderi))

    }

    private fun setUrlText(view: TextView, name: String) {
        val url = github_url + name
        val ss = SpannableString(name)
        ss.setSpan(
            URLSpan(url), 0, name.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = ss
        view.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
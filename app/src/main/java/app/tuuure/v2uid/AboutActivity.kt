package app.tuuure.v2uid

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.tuuure.v2uid.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    companion object {
        private const val github_url = "https://github.com/"
    }

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setUrlText(
            binding.aboutTitle,
            getString(R.string.app_name),
            link = getString(R.string.about_github_link)
        )

        setUrlText(binding.aboutThanks2dust, getString(R.string.about_thanks_2dust))

        setUrlText(binding.aboutThanksSirpryderi, getString(R.string.about_thanks_sirpryderi))

    }

    private fun setUrlText(view: TextView, name: String, link: String = name) {
        val ss = SpannableString(name)
        ss.setSpan(
            URLSpan(github_url + link), 0, name.length,
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
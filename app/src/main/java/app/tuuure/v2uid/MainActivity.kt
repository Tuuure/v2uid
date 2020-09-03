package app.tuuure.v2uid

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.ArraySet
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import app.tuuure.v2uid.AppListManager.Companion.PerAppMode
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val androidpackagenamelistUrl =
            "https://raw.githubusercontent.com/2dust/androidpackagenamelist/master/proxy.txt"
    }

    private lateinit var adapter: AppListAdapter
    private lateinit var result: Pair<PerAppMode, Collection<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = AppListAdapter(this);
        appListView.layoutManager = LinearLayoutManager(this)
        appListView.adapter = adapter

        initData()

        setSupportActionBar(toolBar)
        toolBar.setOnClickListener {
            appListView.smoothScrollToPosition(0)
        }

        swipeRefresh.setOnRefreshListener {
            if (this::result.isInitialized) {
                updateData(result.second)
            }
            swipeRefresh.isRefreshing = false
        }
    }


    private var bypassItem: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        bypassItem = menu.findItem(R.id.action_bypass)
        bypassItem?.isChecked = (mode == PerAppMode.BYPASS_MODE)

        val searchItem: MenuItem? = menu.findItem(R.id.action_search)
        val queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter = newText ?: ""
                adapter.notifyDataSetChanged()
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
        }
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(queryTextListener)
        return true
    }

    private var mode: PerAppMode = PerAppMode.ALLOW_MODE

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_search -> false // Not implemented here
            R.id.action_save -> {
                if (ExecuteAsRootBase.isRootAvailable() and ExecuteAsRootBase.canRunRootCommands()) {
                    val appIDs = ArraySet<String>(adapter.itemCount)
                    for (info in adapter.data) {
                        if (info.isChecked)
                            appIDs.add(info.uid.toString())
                    }
                    val file = AppListManager.saveToTempFile(this, mode, appIDs)
                    if (file != null) {
                        if (AppListManager.writeToFile(file)) {
                            Snackbar.make(
                                rootLayout,
                                getString(R.string.msg_save_success),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Snackbar.make(
                        rootLayout,
                        getString(R.string.msg_missing_root),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                true
            }
            R.id.action_bypass -> {
                mode = if (item.isChecked) {
                    PerAppMode.BYPASS_MODE
                } else {
                    PerAppMode.ALLOW_MODE
                }
                item.isChecked = !item.isChecked
                true
            }
            R.id.action_select_proxy_app -> {
                Snackbar.make(
                    rootLayout,
                    getString(R.string.msg_downloading_content),
                    Snackbar.LENGTH_LONG
                ).show()
                CoroutineScope(Dispatchers.Default).launch {
                    val content = URL(androidpackagenamelistUrl).readText()
                    val pkgList = content.split("\n")
                    withContext(Dispatchers.Main) {
                        if (this@MainActivity::adapter.isInitialized) {
                            updateData(pkgList, reset = false)
                            Snackbar.make(
                                rootLayout,
                                getString(R.string.msg_select_app),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                true
            }
            R.id.action_clear -> {
                updateData(emptyList())
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    private fun initData() {
        CoroutineScope(Dispatchers.Default).launch {
            val result = async(Dispatchers.IO) {
                AppListManager.readFromFile()
            }
            val appList: LinkedList<AppInfo> = LinkedList()
            val installedPackages =
                packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES)
            installedPackages.sortByDescending { it.applicationInfo.uid }
            for (pkgInfo in installedPackages) {
                val applicationInfo = pkgInfo.applicationInfo

                appList.add(
                    AppInfo(
                        applicationInfo.loadLabel(packageManager),
                        pkgInfo.packageName,
                        applicationInfo.uid,
                        applicationInfo.loadIcon(packageManager),
                        applicationInfo.flags
                    )
                )
            }
            adapter.data = appList
            withContext(Dispatchers.Main) {
                updateData(result.await().second)
            }
            mode = result.await().first
            withContext(Dispatchers.Main) {
                bypassItem?.isChecked = (mode == PerAppMode.BYPASS_MODE)
            }

        }
    }

    private fun updateData(appList: Collection<String>, reset: Boolean = true) {
        val checkedList: LinkedList<AppInfo> = LinkedList()
        val unCheckedList: LinkedList<AppInfo> = LinkedList()

        for (info in adapter.data.sortedByDescending { it.uid }) {
            if (reset or !info.isChecked) {
                info.isChecked = ((info.uid.toString() in appList) or (info.packageName in appList))
            }
            if (info.isChecked) {
                checkedList.addLast(info)
            } else {
                unCheckedList.addLast(info)
            }
        }
        for (info in unCheckedList) {
            checkedList.addLast(info)
        }
        adapter.data = checkedList
        adapter.notifyDataSetChanged()
    }
}
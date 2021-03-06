package app.tuuure.v2uid

import android.content.Context
import java.io.*

class AppListManager {
    companion object {
        private const val V2RAY_FOLDER = "/data/v2ray"
        private const val APPID_LIST_PATH = "$V2RAY_FOLDER/appid.list"
        private const val TEMP_FILE = "temp.txt"
        private const val BYPASS_FLAG = "#bypass"

        @JvmStatic
        private fun getListBufferedReader(): BufferedReader? {
            setchown()
            val file = File(APPID_LIST_PATH)
            if (file.exists()) {
                val inputStreamReader = InputStreamReader(FileInputStream(file))
                return BufferedReader(inputStreamReader)
            } else {
                return null
            }
        }

        @JvmStatic
        private fun setchown(): Boolean {
            val root: ExecuteAsRootBase = object : ExecuteAsRootBase() {
                override val commandsToExecute: ArrayList<String>?
                    get() {
                        val commands: ArrayList<String> = ArrayList()
                        commands.add("chown -R 3003:3003 $V2RAY_FOLDER")
                        commands.add("exit")
                        return commands
                    }
            }
            return root.execute()
        }

        enum class PerAppMode {
            ALLOW_MODE,
            BYPASS_MODE
        }

        @JvmStatic
        fun readFromFile(): Pair<PerAppMode, Collection<String>> {
            val appList = mutableSetOf<String>()
            val bufferedReader: BufferedReader? = getListBufferedReader()

            var line: String? = bufferedReader?.readLine()
            var mode: PerAppMode = PerAppMode.ALLOW_MODE
            if (line?.toIntOrNull() != null) {
                appList.add(line)
            } else if (line == BYPASS_FLAG) {
                mode = PerAppMode.BYPASS_MODE
            }
            while (bufferedReader?.readLine().also { line = it } != null) {
                if (line?.toIntOrNull() != null) {
                    appList.add(line!!)
                }
            }
            return Pair(mode, appList)
        }

        @JvmStatic
        fun saveToTempFile(
            context: Context,
            mode: PerAppMode,
            appList: Collection<String>
        ): File? {
            val temp = File(context.cacheDir, TEMP_FILE)
            val bufferedWriter = BufferedWriter(FileWriter(temp))
            if (mode == PerAppMode.BYPASS_MODE) {
                bufferedWriter.write(BYPASS_FLAG)
                bufferedWriter.write("\n")
            }
            for (appID in appList) {
                bufferedWriter.write(appID)
                bufferedWriter.write("\n")
            }
            bufferedWriter.close()
            return temp
        }

        @JvmStatic
        fun writeToFile(file: File): Boolean {
            val root: ExecuteAsRootBase = object : ExecuteAsRootBase() {
                override val commandsToExecute: ArrayList<String>?
                    get() {
                        val commands: ArrayList<String> = ArrayList()

                        // need to mount /system in write mode
                        //commands.add("/sbin/.magisk/img/v2ray/scripts/v2ray.service stop")
                        commands.add("mount -o remount,rw /data")
                        commands.add("mv -f ${file.absolutePath} $APPID_LIST_PATH")
                        commands.add("chmod 644 $APPID_LIST_PATH")
                        commands.add("chown -R 3003:3003 $V2RAY_FOLDER")
                        commands.add("mount -o remount,ro /data")
                        //commands.add("/sbin/.magisk/img/v2ray/scripts/v2ray.service restart")
                        commands.add("bash /sbin/.magisk/img/v2ray/scripts/start.sh")
                        commands.add("exit")
                        return commands
                    }
            }
            return root.execute()
        }

    }
}
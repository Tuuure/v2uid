package app.tuuure.v2uid

import java.io.*

/**
 * Created by Vittorio on 17/07/17.
 *
 *
 * This class handles the execution of commands requiring super user access.
 *
 *
 * Original class taken from: http://muzikant-android.blogspot.it/2011/02/how-to-get-root-access-and-execute.html
 */

abstract class ExecuteAsRootBase {
    companion object {
        @JvmStatic
        fun isRootAvailable(): Boolean {
            val paths = System.getenv("PATH")?.split(":")
            if (!paths.isNullOrEmpty()) {
                for (pathDir in paths) {
                    if (File(pathDir, "su").exists()) {
                        return true
                    }
                }
            }
            return false
        }

        @JvmStatic
        fun canRunRootCommands(): Boolean = try {
            val suProcess = Runtime.getRuntime().exec("su")
            val os = OutputStreamWriter(BufferedOutputStream(suProcess.outputStream))
            val osRes =
                BufferedReader(InputStreamReader(BufferedInputStream(suProcess.inputStream)))
            // Getting the id of the current user to check if this is root
            os.write("id\n")
            os.flush()
            val currUid: String? = osRes.readLine()
            if (currUid != null) {
                os.write("exit\n")
                os.flush()
                currUid.contains("uid=0")
                // True as "Root access granted", false as "Root access rejected"
            } else {
                false
                // Can't get root access or denied by user
            }
        } catch (e: Exception) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted
            false
        }
    }

    protected abstract val commandsToExecute: ArrayList<String>?

    fun execute(): Boolean = try {
        val commands = commandsToExecute
        if (null != commands && commands.size > 0) {
            val suProcess = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(suProcess.outputStream)

            // Execute commands that require root access
            for (currCommand in commands) {
                os.writeBytes("$currCommand\n")
                os.flush()
            }
            os.writeBytes("exit\n")
            os.flush()
            val suProcessReturnValue = suProcess.waitFor()
            // Root access granted  or denied
            255 != suProcessReturnValue
        } else {
            false
        }
    } catch (ex: Exception) {
        false
    }

}
package ptsiogas.gr.securebox

import android.content.Context
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class StoreUtils {
    companion object {
        fun storeVarName(variableName: String, context: Context?) {
            if (context == null) {
                return
            }
            try {
                val map = loadVarNames(context)
                map[variableName] = true
                val fos =
                    context.openFileOutput("varNames_secureHelper.dat", Context.MODE_PRIVATE)
                val oos = ObjectOutputStream(fos)
                oos.writeObject(map)
                oos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun loadVarNames(context: Context?): HashMap<String, Boolean> {
            if (context == null) {
                return HashMap<String, Boolean>()
            }
            try {
                val fileInputStream = context.openFileInput("varNames_secureHelper.dat")
                var result = HashMap<String, Boolean>()
                fileInputStream?.use { fileInputStream ->
                    val objectInputStream = ObjectInputStream(fileInputStream)
                    result = objectInputStream.readObject() as HashMap<String, Boolean>
                }
                return result
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return HashMap<String, Boolean>()
        }
    }
}
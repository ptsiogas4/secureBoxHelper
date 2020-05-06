package ptsiogas.gr.securebox

import android.util.Log

enum class ErrorMessage(val message: String) {
    generalError(message = "Something went wrong! Either variable name or password incorrect"),
    deletionError(message = "variable deleted successfully"),
    errorTitle(message = "SecureBoxHelper"),
    initError(message = "You should first call init method before using the helper!!!");

    companion object {
        fun logGeneralError() {
            Log.e(ErrorMessage.errorTitle.message, ErrorMessage.generalError.message)
        }
    }
}
package whatis.love.agedate.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun Uri.addUriParameter(
    key: String,
    newValue: String,
): Uri {
    val params = queryParameterNames
    val newUri = buildUpon().clearQuery()
    var isSameParamPresent = false
    for (param in params) {
        newUri.appendQueryParameter(
            param,
            if (param == key) newValue else getQueryParameter(param),
        )
        if (param == key) {
            isSameParamPresent = true
        }
    }
    if (!isSameParamPresent) {
        newUri.appendQueryParameter(
            key,
            newValue,
        )
    }
    return newUri.build()
}

fun openExternalRedirectWithToken(
    context: Context,
    url: String,
    token: String,
) {
    val uri = url.toUri().addUriParameter("token", token)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

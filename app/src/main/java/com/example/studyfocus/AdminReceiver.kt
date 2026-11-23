package com.example.studyfocus

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class AdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        return if (Prefs.isStudyActiveNow(context))
            "अभी स्टडी टाइम है, यह बंद नहीं कर सकते"
        else
            "बंद करने पर ऐप अनइंस्टॉल हो सकेगा"
    }
}

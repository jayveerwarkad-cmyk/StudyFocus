import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class AdminReceiver : DeviceAdminReceiver() {
    // While a study slot is active, we warn before admin is removed.
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return if (Storage(context).isStudyActiveNow())
            "Study time chal raha hai! Abhi admin band mat karo."
        else "Admin band karne se strict mode hat jayega."
    }
}

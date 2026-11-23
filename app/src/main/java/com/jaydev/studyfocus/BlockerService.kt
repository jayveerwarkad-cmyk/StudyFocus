isDialerOrSettingsCritical(pkg: String): Boolean =
        pkg.contains("dialer") || pkg.contains("incallui") || pkg == "com.android.settings"

    override fun onInterrupt() {}
}

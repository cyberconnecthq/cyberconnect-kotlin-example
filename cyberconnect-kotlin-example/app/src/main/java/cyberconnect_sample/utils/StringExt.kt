package cyberconnect_sample.utils

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ToastUtils
import timber.log.Timber

fun String?.d(tag: String = "walletConnect d -->") {
    if (AppUtils.isAppDebug()) {
        this?.let {
            Timber.tag(tag).d(it)
        }
    }
}

fun String?.i(tag: String = "walletConnect i -->") {
    if (AppUtils.isAppDebug()) {
        this?.let {
            Timber.tag(tag).i(it)
        }
    }
}

fun String?.e(tag: String = "walletConnect e -->") {
    if (AppUtils.isAppDebug()) {
        this?.let {
            Timber.tag(tag).e(it)
        }
    }
}

fun String?.toast() {
    this?.let {
        ToastUtils.showShort(it)
    }
}

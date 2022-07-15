package cyberconnect_sample.common

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cyberconnect_sample.data.session.SessionRepository
import cyberconnect_sample.api.WalletConnectKitConfig
import cyberconnect_sample.data.wallet.WalletRepository
import okhttp3.OkHttpClient
import org.walletconnect.impls.FileWCSessionStore
import org.walletconnect.impls.MoshiPayloadAdapter
import org.walletconnect.impls.OkHttpTransport
import java.io.File

internal class WalletConnectKitModule(context: Context, config: WalletConnectKitConfig) {

    val sessionRepository by lazy {
        SessionRepository(
            payloadAdapter,
            storage,
            transporter,
            config
        )
    }

    val walletRepository by lazy { WalletRepository(config, sessionRepository) }

    private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    private val payloadAdapter by lazy { MoshiPayloadAdapter(moshi) }

    private val storage by lazy { FileWCSessionStore(file.apply { createNewFile() }, moshi) }

    private val transporter by lazy {
        OkHttpTransport.Builder(
            OkHttpClient.Builder().build(),
            moshi
        )
    }

    private val file by lazy { File(context.cacheDir, "session_store.json") }
}
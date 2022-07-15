package cyberconnect_sample.api

import cyberconnect_sample.common.WalletConnectKitModule
import cyberconnect_sample.data.session.SessionManager
import cyberconnect_sample.data.wallet.WalletManager

class WalletConnectKit private constructor(
    sessionManager: SessionManager,
    walletManager: WalletManager,
) : SessionManager by sessionManager, WalletManager by walletManager {

    class Builder(config: WalletConnectKitConfig) {

        private val walletConnectKitModule = WalletConnectKitModule(config.context, config)

        fun build() = WalletConnectKit(
            walletConnectKitModule.sessionRepository, walletConnectKitModule.walletRepository
        )
    }
}
# cyberconnect-kotlin-example

cyberconnect-kotlin-example is a demo project based on
[cyberconnect-kotlin](https://github.com/cyberconnecthq/cyberconnect-kotlin-lib) and WalletConnect

### Here is the process of how cyberconnect work:
#### 1. Get address of your wallet
#### 2. Generate a pair of P256 keys and save it in keychain
#### 3. Sign a **message** with you wallet(should be pair with the address in step 1) and get the _signature_
#### 4. You can get right format message:
```
let cyberconnectInstance = CyberConnect(WALLETADDRESS) //in step 1
let message = cyberconnectInstance.getAuthorizeString() //use the pubkey of the pairs in step 2
```
#### 5. Sign this message with your wallet and get signature
```
let signature = signature //from step 3
cyberconnectInstance.registerKey(signature: signature, network: .eth) { data in
    print(data) 
}

```
#### 6. Now you can use cyberconnectInstance to create your own connection with other people in web3.0 world if you get success feedback in step 5

Here is a demo Video:
https://user-images.githubusercontent.com/10152008/179936857-8b65a95b-11ae-4efb-b532-527e5b06ec5d.mp4

Want to know more APIs CyberConnect supported please refer to: [cyberconnect-kotlin](https://github.com/cyberconnecthq/cyberconnect-kotlin-lib)

Feel free to open issue and pull request when you have any concern!

### About WalletConnect
WalletConnect is a tool to use your wallet function based on scheme or QR code, many mainstream wallets is supported for now.






# BFT-SMaRt Decentralized Token Infrastructure

DTI is a deterministic, wallet-like service that manages coins and NFTs using the UTXO (Unspent Transaction Output) model. The system allows users to mint, spend, and trade both coins and NFTs in a Byzantine Fault-Tolerant manner using BFT-SMaRt.

The system is configured (system.config) to tolerate up to 1 Byzantine fault (f = 1) with 4 replicas.

---
## Build the Solution
To be able to run the program you need to first open a terminal and run ./gradlew install inside the `library` directory.
After that you can try our program!

## Example Workflow
You can open 6 terminals:

- 4 Replica servers
  
- 2 for the interactives clients
  
Once inside the `library/build/install/library` directory, run:

### Replica servers:

```bash
./smartrun.sh bftsmart.intol.bftmap.BFTMapServer 0
./smartrun.sh bftsmart.intol.bftmap.BFTMapServer 1
./smartrun.sh bftsmart.intol.bftmap.BFTMapServer 2
./smartrun.sh bftsmart.intol.bftmap.BFTMapServer 3
```


### Client:

```bash
./smartrun.sh bftsmart.intol.bftmap.BFTMapInteractiveClient <ClientID>
```

Example:
```bash
./smartrun.sh bftsmart.intol.bftmap.BFTMapInteractiveClient 4
```

---
## Important Notes

- In `MINT`, only the user with the ID 4, **can create coins**.
- In `SPEND`, users **can spend their own coins** back to themselves. This can be useful to compress coin values.
- In `BUY_NFT`, it is **not allowed to buy your own NFT**. This rule was talked in the TP Class.

---

## 📂 Directory Structure

```
bftmap/
├── BFTMapInteractiveClient.java
├── BFTMapServer.java
├── BFTMap.java
├── BFTMapMessage.java
├── BFTMapRequestType.java
├── Coin.java
├── NFT.java
```

---



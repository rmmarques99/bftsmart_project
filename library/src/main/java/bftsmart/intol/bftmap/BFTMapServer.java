/**
 * BFT Map implementation (server side).
 *
 */
package bftsmart.intol.bftmap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

public class BFTMapServer<K, V> extends DefaultSingleRecoverable {
    private final Logger logger = LoggerFactory.getLogger("bftsmart");
    private static int idServerCoins = 999;
    private static int idServerNFTs = 1999;
    TreeMap<Integer, List<Coin>> coinMap;
    TreeMap<Integer, List<NFT>> nftMap;

    // The constructor passes the id of the server to the super class
    public BFTMapServer(int id) {
        coinMap = new TreeMap<>();// dicionario ordenado
        nftMap = new TreeMap<>();
        // turn-on BFT-SMaRt'replica
        new ServiceReplica(id, this, this);// sempre no terminal se conecta replica(copia de um server) é criada aqui
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Use: java BFTMapServer <server id>");
            System.exit(-1);
        }
        new BFTMapServer<Integer, String>(Integer.parseInt(args[0])); // cria um server
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) { // command recebido do invokeorder do
                                                                             // BFTMap
        // all operations must be defined here to be invoked by BFT-SMaRt
        try {
            BFTMapMessage<K, V> request = BFTMapMessage.fromBytes(command);// vai receber request do BFTMap
            BFTMapMessage<K, V> response = new BFTMapMessage<>();
            BFTMapRequestType cmd = request.getType();
            int clientID = msgCtx.getSender();
            logger.info("Ordered execution of a {} request from {}", cmd, msgCtx.getSender());

            switch (cmd) {

                case MY_COINS:
                    List<Coin> clienteCoins = coinMap.getOrDefault(clientID, new ArrayList<>());//devolve lista [] se nao existir
                    response.setMyCoins(clienteCoins);
                    return BFTMapMessage.toBytes(response);
                case MINT:
                   if(clientID==4){
                        List<Coin> clientCoins = coinMap.computeIfAbsent(clientID, k -> new ArrayList<>());
                        idServerCoins++;
                        Coin newCoin = new Coin(idServerCoins, clientID, request.getValue());
                        clientCoins.add(newCoin);
                        response.setID(idServerCoins);
                        return BFTMapMessage.toBytes(response);
                   }else{
                        response.setID(-1);
                        return BFTMapMessage.toBytes(response);
                   }
                case SPEND:
                    if(request.getId() != 0 && request.getId() != 1 && request.getId() != 2 && request.getId() != 3){
                        List<Coin> coinsServer = coinMap.get(clientID);// lista de moedas do cliente no server, treemaps
                                                                    // das replicas, este get aponta para um endereco do coinMap e nao para uma copia
                        // validar se input do client esta mesmo no server
                        int sumValues = 0;
                        List<Integer> serverCoinIds = new ArrayList<>();
                        // Criar lista de IDs das moedas no servidor
                        for (Coin coin : coinsServer) {
                            serverCoinIds.add(coin.getId());
                        }
                        if (!serverCoinIds.containsAll(request.getCoinsID())) { // verficia se moedas do request estao no
                                                                                // server
                            response.setmultiplesValues(Arrays.asList(-1));
                            return BFTMapMessage.toBytes(response);
                        }
                        for (Coin coin : coinsServer) {
                            if (request.getCoinsID().contains(coin.getId())) { // faz o sumvalues
                                sumValues += coin.getValue();
                            }
                        }
                        if (sumValues < request.getValue()) {
                            response.setmultiplesValues(Arrays.asList(-1)); 
                            return BFTMapMessage.toBytes(response);
                        }
                        // adicionamos uma coin a replica
                        coinsServer.removeIf(coin -> request.getCoinsID().contains(coin.getId()));
                        idServerCoins++;
                        Coin coinReceiver = new Coin(idServerCoins, request.getId(), request.getValue());// nova moeda
                        coinMap.computeIfAbsent(request.getId(), k -> new ArrayList<>()).add(coinReceiver);
                        if ((sumValues - request.getValue()) == 0) {
                            response.setmultiplesValues(Arrays.asList(0));
                        } else {
                            idServerCoins++;
                            Coin coinClient = new Coin(idServerCoins, clientID, sumValues - request.getValue());// nova                                                                          // moeda
                            coinMap.computeIfAbsent(clientID, k -> new ArrayList<>()).add(coinClient);
                            response.setmultiplesValues(Arrays.asList(idServerCoins,sumValues - request.getValue()));
                        }
                        return BFTMapMessage.toBytes(response);
                }else{
                    response.setmultiplesValues(Arrays.asList(-1));
                    return BFTMapMessage.toBytes(response);
                }
                case MY_NFTS:
                    List<NFT> clientNfts = nftMap.getOrDefault(clientID, new ArrayList<>());
                    response.setMyNFTS(clientNfts);
                    return BFTMapMessage.toBytes(response);

                case MINT_NFT:
                    List<NFT> clientNFT = nftMap.computeIfAbsent(clientID, k -> new ArrayList<>());
                    for (NFT nft : clientNFT) {
                        if (nft.getName().equals(request.getName())) {
                            logger.error("O nome do NFT já está em uso: " + request.getName());
                            response.setID(-1); // Código de erro para nome duplicado
                            return BFTMapMessage.toBytes(response);
                        }
                    }
                    idServerNFTs++;
                    NFT newNFT = new NFT(idServerNFTs, clientID, request.getName(), request.getUri(),
                            request.getValue());
                    clientNFT.add(newNFT);
                    response.setID(idServerNFTs);
                    return BFTMapMessage.toBytes(response);

                case SET_NFT_PRICE:
                    List<NFT> nftsOwned = nftMap.get(clientID);
                    if (nftsOwned != null) {
                        for (NFT nft : nftsOwned) {
                            if (nft.getId() == request.getId()) {
                                nft.setValue(request.getValue());
                                response.setID(nft.getId());
                                return BFTMapMessage.toBytes(response);
                            }
                        }
                    }
                    response.setID(-1);
                    return BFTMapMessage.toBytes(response);
                case SEARCH_NFT:
                    String searchText = request.getName().toLowerCase();
                    List<NFT> foundNFTs = new ArrayList<>();
                    if (nftMap.isEmpty()) {
                        System.out.println("Ainda não há NFT's criados.");
                        response.setMyNFTS(Collections.emptyList());
                        break;
                    }
                    for (List<NFT> nftList : nftMap.values()) {
                        for (NFT nft : nftList) {
                            if (nft.getName().toLowerCase().contains(searchText)) {
                                foundNFTs.add(nft);
                            }
                        }
                    }
                    if (foundNFTs.isEmpty()) {
                        System.out.println("Nenhum NFT encontrado com esse nome.");
                    }
                    response.setMyNFTS(foundNFTs);
                    return BFTMapMessage.toBytes(response);

                case BUY_NFT:
                    int nftId = request.getId();
                    List<Integer> coinIds = request.getCoinsID();
                    List<Coin> buyerCoins = coinMap.get(clientID);
                    NFT nftToBuy = null;

                    // ver se existe e de quem é o NFT
                    for (List<NFT> nftList : nftMap.values()) {
                        for (NFT nft : nftList) {
                            if (nft.getId() == nftId) {
                                nftToBuy = nft;
                                break;
                            }
                        }
                        if (nftToBuy != null)
                            break;
                    }

                    if (nftToBuy == null || nftToBuy.getOwner() == clientID) {
                        System.out.println("NFT não encontrado ou já pertence ao comprador.");
                        response.setID(-1);
                        return BFTMapMessage.toBytes(response);
                    }
                    // Validate coins
                    int totalValue = 0;
                    List<Integer> buyerCoinIds = new ArrayList<>();
                    for (Coin coin : buyerCoins) {
                        buyerCoinIds.add(coin.getId());
                    }
                    if (!buyerCoinIds.containsAll(coinIds)) {
                        System.out.println("Moedas não encontradas.");
                        response.setID(-1);
                        return BFTMapMessage.toBytes(response);
                    }
                    for (Coin coin : buyerCoins) {
                        if (coinIds.contains(coin.getId())) {
                            totalValue += coin.getValue();
                        }
                    }
                    if (totalValue < nftToBuy.getValue()) {
                        System.out.println("Fundos insuficientes.");
                        response.setmultiplesValues(Arrays.asList(-1)); // Insufficient funds
                        return BFTMapMessage.toBytes(response);
                    }
                    // transacao
                    buyerCoins.removeIf(coin -> coinIds.contains(coin.getId()));
                    int sellerId = nftToBuy.getOwner();
                    nftToBuy.setOwner(clientID); // Update dono da nft

                    // Remove NFT from the seller's list
                    List<NFT> sellerNFTs = nftMap.get(sellerId);
                    if (sellerNFTs != null) {
                        sellerNFTs.removeIf(nft -> nft.getId() == nftId);
                    }

                    // Adicionar nft ao comprador
                    nftMap.computeIfAbsent(clientID, k -> new ArrayList<>()).add(nftToBuy);
                    idServerCoins++;
                    Coin sellerCoin = new Coin(idServerCoins, sellerId, nftToBuy.getValue());
                    coinMap.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(sellerCoin);
                    float remainingValue = totalValue - nftToBuy.getValue();
                    if (remainingValue > 0) {
                        idServerCoins++;
                        Coin buyerChangeCoin = new Coin(idServerCoins, clientID, remainingValue);
                        coinMap.computeIfAbsent(clientID, k -> new ArrayList<>()).add(buyerChangeCoin);
                        response.setmultiplesValues(Arrays.asList(idServerCoins, remainingValue));
                    } else {
                        response.setmultiplesValues(Arrays.asList(0)); // No change
                        
                    }
                    return BFTMapMessage.toBytes(response);
            }
            return null;
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("Failed to process ordered request", ex);
            return new byte[0];
        }
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        // read-only operations can be defined here to be invoked without running
        // consensus
        try {
            BFTMapMessage<K, V> response = new BFTMapMessage<>();
            BFTMapMessage<K, V> request = BFTMapMessage.fromBytes(command);
            BFTMapRequestType cmd = request.getType();
            int clientID = msgCtx.getSender();
            logger.info("Unordered execution of a {} request from {}", cmd, msgCtx.getSender());
            switch (cmd) {
                case MY_COINS:
                    List<Coin> clienteCoins = coinMap.getOrDefault(clientID, new ArrayList<>());
                    response.setMyCoins(clienteCoins);
                    return BFTMapMessage.toBytes(response);

                case MY_NFTS:
                    List<NFT> clientNfts = nftMap.getOrDefault(clientID, new ArrayList<>());
                    response.setMyNFTS(clientNfts);
                    return BFTMapMessage.toBytes(response);
                case SEARCH_NFT:
                    String searchText = request.getName().toLowerCase();
                    List<NFT> foundNFTs = new ArrayList<>();
                    if (nftMap.isEmpty()) {
                        System.out.println("Ainda não há NFT's criados.");
                        response.setMyNFTS(Collections.emptyList());
                        break;
                    }
                    for (List<NFT> nftList : nftMap.values()) {
                        for (NFT nft : nftList) {
                            if (nft.getName().toLowerCase().contains(searchText)) {
                                foundNFTs.add(nft);
                            }
                        }
                    }
                    if (foundNFTs.isEmpty()) {
                        System.out.println("Nenhum NFT encontrado com esse nome.");
                    }
                    response.setMyNFTS(foundNFTs);
                    return BFTMapMessage.toBytes(response);
            }
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("Failed to process unordered request", ex);
            return new byte[0];
        }
        return null;
    }

    @Override
    public byte[] getSnapshot() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeInt(idServerCoins);
            out.writeInt(idServerNFTs);
            out.writeObject(coinMap);
            out.writeObject(nftMap);
            out.flush();
            bos.flush();
            return bos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace(); // debug instruction
            return new byte[0];
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void installSnapshot(byte[] state) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(state);
                ObjectInput in = new ObjectInputStream(bis)) {
            idServerCoins = in.readInt();
            idServerNFTs = in.readInt();
            coinMap = (TreeMap<Integer, List<Coin>>) in.readObject();
            nftMap = (TreeMap<Integer, List<NFT>>) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace(); // debug instruction
        }
    }
}

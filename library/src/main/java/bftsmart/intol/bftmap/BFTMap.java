/**
 * BFT Map implementation (client side).
 *
 */
package bftsmart.intol.bftmap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bftsmart.tom.ServiceProxy;

public class BFTMap<K, V> {
    private final Logger logger = LoggerFactory.getLogger("bftsmart");
    private final ServiceProxy serviceProxy;

    public BFTMap(int id) {
        serviceProxy = new ServiceProxy(id); // service proxy lê config/system.config, descobre servidores replicados
                                             // para saber para onde enviar pedidos
    } // define parametros de seguranca(modo de comunicacao, cifra etc), evia pedidos
      // atraves de metodos p.e invoke order

    /**
     *
     * @param key The key associated to the value
     * @return value The value previously added to the map
     */
    public List<Coin> my_Coins() {
        byte[] rep;
        BFTMapMessage<K, V> request = new BFTMapMessage<>();
        request.setType(BFTMapRequestType.MY_COINS);
        try {
            rep = serviceProxy.invokeUnordered(BFTMapMessage.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send MY_COINS request");
            return null;
        }
        try {
            BFTMapMessage<K, V> response = BFTMapMessage.fromBytes(rep);
            return response.getMyCoins();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of My_Coins request");
            return null;
        }
    }

    public int mint(float coinValue) throws ClassNotFoundException {
        byte[] rep;
        try {
            BFTMapMessage<K, V> request = new BFTMapMessage<>();
            request.setType(BFTMapRequestType.MINT);
            request.setValue(coinValue);
            rep = serviceProxy.invokeOrdered(BFTMapMessage.toBytes(request));
            BFTMapMessage<K, V> response = BFTMapMessage.fromBytes(rep);
            return response.getId();
        } catch (IOException e) {
            logger.error("Failed to send MINT request");
            return -1;
        }
    }

    public List<Object> spend(List<Integer> idsPedidosInput, int receiver, float valueCoin) throws ClassNotFoundException {
        byte[] rep;
        List<Object> respostaLista = new ArrayList<>();
        try {
            BFTMapMessage<K, V> request = new BFTMapMessage<>();
            request.setType(BFTMapRequestType.SPEND);
            request.setCoinsID(idsPedidosInput);
            request.setID(receiver);
            request.setValue(valueCoin);
            rep = serviceProxy.invokeOrdered(BFTMapMessage.toBytes(request));
            BFTMapMessage<K, V> response = BFTMapMessage.fromBytes(rep);
            
            int id = (int) response.getmultiplesValues().get(0);
            
            if(id==0){
                respostaLista.add(id);
            }else if(id==-1){
                respostaLista.add(id);
            }else{
                respostaLista.add(id);
                float value = (float) response.getmultiplesValues().get(1);
                respostaLista.add(value);
            }
            return respostaLista;

        } catch (IOException e) {
            logger.error("Failed to send SPEND request");
            respostaLista.add(-1);
            return respostaLista;
        }
    }

    public List<NFT> my_NFTs() {
        byte[] rep;
        BFTMapMessage<K, V> request = new BFTMapMessage<>();
        request.setType(BFTMapRequestType.MY_NFTS);
        try {
            rep = serviceProxy.invokeUnordered(BFTMapMessage.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send MY_NFTS request");
            return null;
        }
        try {
            BFTMapMessage<K, V> response = BFTMapMessage.fromBytes(rep);
            return response.getMyNfts();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialize response of MY_NFTS request");
            return null;
        }
    }

    public int mint_NFT(String name, String uri, float value) throws ClassNotFoundException {
        byte[] rep;
        try {
            BFTMapMessage<K, V> request = new BFTMapMessage<>();
            request.setType(BFTMapRequestType.MINT_NFT);
            request.setNftName(name);
            request.setNftUri(uri);
            request.setValue(value);
            rep = serviceProxy.invokeOrdered(BFTMapMessage.toBytes(request));
            BFTMapMessage<K, V> response = BFTMapMessage.fromBytes(rep);

            int nftId = response.getId();
            if (nftId == -1) {
                return -1;
            }
            return nftId;
        } catch (IOException e) {
            logger.error("Failed to send MINT_NFT request");
            return -1;
        }
    }

    public int setNFTPrice(int nftId, float newValue) throws ClassNotFoundException {
        byte[] rep;
        try {
            BFTMapMessage<K, V> request = new BFTMapMessage<>();
            request.setType(BFTMapRequestType.SET_NFT_PRICE);
            request.setID(nftId);
            request.setValue(newValue);

            rep = serviceProxy.invokeOrdered(BFTMapMessage.toBytes(request));
            BFTMapMessage<K, V> response = BFTMapMessage.fromBytes(rep);

            return response.getId();
        } catch (IOException e) {
            logger.error("Failed to send SET_NFT_PRICE request");
            return -1;
        }
    }

    public List<NFT> searchNFT(String searchText) {
        byte[] rep;
        BFTMapMessage<K, V> request = new BFTMapMessage<>();
        request.setType(BFTMapRequestType.SEARCH_NFT);
        request.setNftName(searchText);

        try {
            rep = serviceProxy.invokeUnordered(BFTMapMessage.toBytes(request));

            if (rep == null) {
                logger.error("SEARCH_NFT request returned null response.");
                return new ArrayList<>();
            }

        } catch (IOException e) {
            logger.error("Failed to send SEARCH_NFT request");
            return new ArrayList<>();
        }

        try {
            BFTMapMessage<K, V> response = BFTMapMessage.fromBytes(rep);
            return response.getMyNfts();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialize response of SEARCH_NFT request");
            return new ArrayList<>();
        }
    }

    public List<Object> buyNFT(int nftId, List<Integer> coinIds) throws ClassNotFoundException {
        byte[] rep;
        List<Object> respostaLista = new ArrayList<>();
        try {
            BFTMapMessage<K, V> request = new BFTMapMessage<>();
            request.setType(BFTMapRequestType.BUY_NFT);
            request.setID(nftId);
            request.setCoinsID(coinIds);
            rep = serviceProxy.invokeOrdered(BFTMapMessage.toBytes(request));
            BFTMapMessage<K, V> response = BFTMapMessage.fromBytes(rep);
            int id = (int) response.getmultiplesValues().get(0);
            
            if(id==0){
                respostaLista.add(id);
            }else if(id==-1){
                respostaLista.add(id);
            }else{
                respostaLista.add(id);
                float value = (float) response.getmultiplesValues().get(1);
                respostaLista.add(value);
            }
            return respostaLista;
        } catch (IOException e) {
            logger.error("Failed to send BUY_NFT request");
            return respostaLista;
        }
    }
}

package bftsmart.intol.bftmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class BFTMapMessage<K, V> implements Serializable {
    private BFTMapRequestType type;
    private float value;
    private List<Coin> myCoins;
    private List<Integer> coinsID;
    private Integer id;
    private String unique;
    private List<NFT> myNfts;
    private int owner;
    private String nftName;
    private String nftURI;
    private List<Object> multiplesValues;
    public BFTMapMessage() {
    }
    public static <K, V> byte[] toBytes(BFTMapMessage<K, V> message) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(message);

        objOut.flush();
        byteOut.flush();

        return byteOut.toByteArray();
    }
    @SuppressWarnings("unchecked")
    public static <K, V> BFTMapMessage<K, V> fromBytes(byte[] rep) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(rep);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        return (BFTMapMessage<K, V>) objIn.readObject();
    }

    @SuppressWarnings("unchecked")
    public List<Object> getmultiplesValues() {
        return multiplesValues;
    }
    @SuppressWarnings("unchecked")
    public void setmultiplesValues(List<Object> multiplesValues) {
        this.multiplesValues = multiplesValues;
    }
    public BFTMapRequestType getType() {
        return type;
    }
    public int getId() {
        return id;
    }
    public float getValue() {
        return value;
    }
    @SuppressWarnings("unchecked")
    public void setID(int id) {
        this.id = id;
    }
    @SuppressWarnings("unchecked")
    public void setValue(float value) {
        this.value = value;
    }
    public void setMyCoins(List<Coin> coins) {
        this.myCoins = coins;
    }
    public void setCoinsID(List<Integer> coins) {
        this.coinsID = coins;
    }
    public List<Integer> getCoinsID() {
        return coinsID;
    }
    public List<Coin> getMyCoins() {
        return myCoins;
    }
    public void setType(BFTMapRequestType type) {
        this.type = type;
    }
    public void setUnique(String unique) {
        this.unique = unique;
    }
    public String getUnique() {
        return unique;
    }
    public List<NFT> getMyNfts() {
        return myNfts;
    }
    public void setMyNFTS(List<NFT> nfts) {
        this.myNfts = nfts;
    }
    public String getName() {
        return nftName;
    }
    public void setNftName(String nftname) {
        this.nftName = nftname;
    }
    public int getOwner() {
        return owner;
    }
    public void setOwner(int newOwner) {
        this.owner = newOwner;
    }
    public String getUri() {
        return nftURI;
    }
    public void setNftUri(String uri) {
        this.nftURI = uri;
    }

}

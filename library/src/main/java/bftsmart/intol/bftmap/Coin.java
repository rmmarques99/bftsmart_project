package bftsmart.intol.bftmap;

import java.io.Serializable;

public class Coin implements Serializable {
    private int id;
    private int owner;
    private float value;

    public Coin(Integer id, Integer owner, float value) {
        this.id = id;
        this.owner = owner;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getOwner() {
        return owner;
    }

    public float getValue() {
        return value;
    }

    public String toString() {
        return "Coin{" + "id=" + id + ", owner=" + owner + ", value=" + value + '}';
    }
}

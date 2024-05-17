/* Authors: Qaturi Vaughn & Moses Tennent
 * Assignment 4, TCSS 342 Spring 2024
 * This is the MyCuckooTableclass for the compressed literature 2 assignment
 * 
 * The MyCuckooTable class implements a Cuckoo Hashing data structure. 
 * Cuckoo Hashing is a technique that resolves collisions by using multiple 
 * hash functions and two hash tables.
 */

//Class  MyCuckooTable must not use any Java standard class other than String, Math, Integer, and 
//eventually (for debugging and statistics reporting) System and/or RuntimeException

public class MyCuckooTable<K, V> {
    private static final int lgTABSIZE = 16;
    public static final int TABSIZE = 1 << lgTABSIZE;
    private static final int MaxLoop = 3 * lgTABSIZE;

    private int size;
    private int evictions; 
    private K[] keys;
    private V[] values;
    private final SipHash sh = new SipHash(); // Add this line

    @SuppressWarnings("unchecked")
    public MyCuckooTable() {
        this.size = 0;
        this.evictions = 0;
        this.keys = (K[]) new Object[TABSIZE];
        this.values = (V[]) new Object[TABSIZE];
    }

    public int size() {
        return size;
    }

    public int getEvictions() {
        return evictions;
    }

    @SuppressWarnings("unchecked")
    public void reset() {
        this.size = 0;
        this.evictions = 0;
        this.keys = (K[]) new Object[TABSIZE];
        this.values = (V[]) new Object[TABSIZE];
    }

    public boolean put(K searchKey, V newValue) {
        if (size >= TABSIZE) {
            reset();
            return false;
        }
        int h1 = hash(searchKey, 1);
        int h2 = hash(searchKey, 2);
        for (int loop = 0; loop < MaxLoop; loop++) {
            if (keys[h1] == null || keys[h1].equals(searchKey)) {
                keys[h1] = searchKey;
                values[h1] = newValue;
                size++;
                return true;
            } else if (keys[h2] == null || keys[h2].equals(searchKey)) {
                keys[h2] = searchKey;
                values[h2] = newValue;
                size++;
                return true;
            } else {
                K displacedKey;
                V displacedValue;
                if (Math.random() < 0.5) {
                    displacedKey = keys[h1];
                    displacedValue = values[h1];
                    keys[h1] = searchKey;
                    values[h1] = newValue;
                } else {
                    displacedKey = keys[h2];
                    displacedValue = values[h2];
                    keys[h2] = searchKey;
                    values[h2] = newValue;
                }
                searchKey = displacedKey;
                newValue = displacedValue;
                h1 = hash(searchKey, 1);
                h2 = hash(searchKey, 2);
                if (keys[h1] != null) {
                    evictions++;
                }
            }
        }
        reset();
        return false;
    }    

    public V get(K searchKey) {
        int h1 = hash(searchKey, 1);
        int h2 = hash(searchKey, 2);
        if (keys[h1] != null && keys[h1].equals(searchKey)) {
            return values[h1];
        }
        if (keys[h2] != null && keys[h2].equals(searchKey)) {
            return values[h2];
        }
        return null; // Key not found
    }

    private int hash(K key, int fno) {
        long hash = (fno == 1) ? sh.hash("1" + key.toString()) : sh.hash("2" + key.toString());
        return (int) (hash & (TABSIZE - 1));
    }
}

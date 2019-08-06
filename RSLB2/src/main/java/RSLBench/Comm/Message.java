package RSLBench.Comm;

/**
 * This class is an empty class that allows to implement custom messages for different algorithms.
 */
public interface Message {
    
    /** Number of bytes used by an entity id */
    public static final int BYTES_ENTITY_ID = 4;
    
    /** Number of bytes used by a utility value */
    public static final int BYTES_UTILITY_VALUE = 8;
    
    /**
     * Get the number of bytes of this message.
     * @return number of bytes of this message.
     */
    public abstract int getBytes();
    
}

package DataModel;

/**
 *
 * @author fairfax
 */
public class dmTransitionTemplate {
    private boolean prevailing;
    private String value;
    private String origin;
    
    /**
     * Constructor for prevailing transition template
     * @param val state variable value
     */
    public dmTransitionTemplate(String val) {
        this.prevailing = true;
        this.value = val;
        this.origin = null;
    }
    
    public dmTransitionTemplate(String from, String to) {
        this.prevailing = false;
        this.origin = from;
        this.value = to;
    }
    
    public boolean isPrevailing() {
        return this.prevailing;
    }
    
    /**
     * In case of non-prevailing transition this is target value class.
     * In case of prevailing transition target and original value classes are equal.
     * 
     * @return 
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * In case of prevailing transition this is equal to target value class.
     * In case of non-prevailing transtion this is original value class of state variable.
     * 
     * @return 
     */
    public String getOrigin() {
        if (this.prevailing) {
            return this.value;
        } else {
            return this.origin;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final dmTransitionTemplate other = (dmTransitionTemplate) obj;
        if (this.prevailing != other.prevailing) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        if (this.origin != other.origin && (this.origin == null || !this.origin.equals(other.origin))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 89 * hash + (this.origin != null ? this.origin.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        if (prevailing) {
            return value.toString();
        } else {
            return origin.toString() + " -> " + value.toString();
        }
    }
}

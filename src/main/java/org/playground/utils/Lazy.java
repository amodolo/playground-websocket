/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.playground.utils;

import java.io.Serializable;
import java.util.function.Supplier;


/**
 *
 * @author mmanfrin
 */
public class Lazy<T> implements Serializable {
    protected static final String NULL=new String();
    
    public interface SerializableSupplier<T>extends Supplier<T>, Serializable {
    }
    
    protected final SerializableSupplier<T> supplier;
    protected transient volatile Object value;
    
    public Lazy() {
        supplier=null;
    }
    
    public Lazy(SerializableSupplier<T> supplier) {
        this.supplier=supplier;
    }
    
    public final T get() {
        return get(supplier,null);
    }
    
    public final T get(T def) {
        return get(supplier,def);
    }
    
    
    public final T get(Supplier<T> supplier,T def) {
        if (value==null) { // never been loaded
            synchronized(this) {
                if (supplier==null) throw new UnsupportedOperationException("No value supplier has been provided");
                if (value==null) value=supplier.get();
                if (value==null) value=NULL;
            }
        }
        
        if (value==NULL) return def;
        else return (T)value;
    }
    
}

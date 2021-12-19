package jw05.anish.algorithm;

public class Tuple<T1, T2> {
    public T1 first;
    public T2 second;

    public Tuple(T1 f, T2 s) {
        first = f;
        second = s;
    }

    public Tuple(){
        
    }
    
    @Override
    public String toString() {
        return new String("<"+first+","+second+">");
    }
}
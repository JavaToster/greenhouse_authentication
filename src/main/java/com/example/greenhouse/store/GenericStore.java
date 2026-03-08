package com.example.greenhouse.store;

public interface GenericStore<T, ID>{
    T findById(ID id);
    T save(T t);
}

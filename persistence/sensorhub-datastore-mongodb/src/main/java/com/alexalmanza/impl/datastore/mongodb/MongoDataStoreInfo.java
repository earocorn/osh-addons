package com.alexalmanza.impl.datastore.mongodb;

import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;

public class MongoDataStoreInfo {
    protected String name;


    protected MongoDataStoreInfo()
    {
    }


    public String getName()
    {
        return name;
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends Builder> T builder()
    {
        return (T)new Builder(new MongoDataStoreInfo());
    }


    @SuppressWarnings("unchecked")
    public static class Builder<B extends Builder<B, T>, T extends MongoDataStoreInfo> extends BaseBuilder<T>
    {
        protected Builder(T instance)
        {
            super(instance);
        }


        public B withName(String name)
        {
            instance.name = name;
            return (B)this;
        }


        public T build()
        {
            Asserts.checkNotNull(instance.name, "name");
            return super.build();
        }
    }
}

package com.chelsenok.wifidirect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

final class Serializer {

    public static synchronized byte[] serialize(final Object obj) {
        try {
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            final ObjectOutput o = new ObjectOutputStream(b);
            o.writeObject(obj);
            return b.toByteArray();
        } catch (final Exception e) {
            return new byte[0];
        }
    }

    public static synchronized Object deserialize(final byte[] bytes) {
        try {
            final ByteArrayInputStream b = new ByteArrayInputStream(bytes);
            final ObjectInput o = new ObjectInputStream(b);
            return o.readObject();
        } catch (final Exception e) {
            return new Object();
        }
    }
}
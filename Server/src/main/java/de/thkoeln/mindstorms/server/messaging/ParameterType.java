package de.thkoeln.mindstorms.server.messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Objects;

/**
 * ParameterType
 */
public enum ParameterType {
    BYTE(byte.class, Byte.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            return dataInputStream.readByte();
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            dataOutputStream.writeByte((byte) value);
        }
    }),

    SHORT(short.class, Short.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            return dataInputStream.readShort();
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            dataOutputStream.writeShort((short) value);
        }
    }),

    INTEGER(int.class, Integer.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            return dataInputStream.readInt();
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            dataOutputStream.writeInt((int) value);
        }
    }),

    LONG(long.class, Long.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            return dataInputStream.readLong();
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            dataOutputStream.writeLong((long) value);
        }
    }),

    FLOAT(float.class, Float.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            return dataInputStream.readFloat();
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            dataOutputStream.writeFloat((float) value);
        }
    }),

    DOUBLE(double.class, Double.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            return dataInputStream.readDouble();
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            dataOutputStream.writeDouble((double) value);
        }
    }),

    BOOLEAN(boolean.class, Boolean.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            return dataInputStream.readBoolean();
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            dataOutputStream.writeBoolean((boolean) value);
        }
    }),

    CHARACTER(char.class, Character.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            return dataInputStream.readChar();
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            dataOutputStream.writeChar((char) value);
        }
    }),

    VOID(void.class, Void.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) {
            return null;
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) {

        }
    }),

    STRING(null, String.class, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            return dataInputStream.readUTF();
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            dataOutputStream.writeUTF(String.valueOf(value));
        }
    }),

    ARRAY(null, null, new ReaderAdapter() {
        @Override
        public Object readFrom(DataInputStream dataInputStream) throws IOException {
            int size = dataInputStream.readInt();
            int type = dataInputStream.readInt();
            ParameterType parameterType = values()[Math.abs(type) - 1];
            Object array = Array.newInstance(type < 0 ? parameterType.primitiveClass : parameterType.wrapperClass, size);
            for (int i = 0; i < size; i++) {
                Array.set(array, i, parameterType.readerAdapter.readFrom(dataInputStream));
            }
            return array;
        }
    }, new WriterAdapter() {
        @Override
        public void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException {
            int size = Array.getLength(value);
            ParameterType parameterType = Objects.requireNonNull(getByType(value.getClass().getComponentType()));
            int type = parameterType.ordinal() + 1;
            if (value.getClass().getComponentType().isPrimitive())
                type = -type;
            dataOutputStream.writeInt(size);
            dataOutputStream.writeInt(type);
            for (int i = 0; i < size; i++) {
                parameterType.writeTo(dataOutputStream, Array.get(value, i));
            }
        }
    });

    private final Class<?> primitiveClass, wrapperClass;
    private final ReaderAdapter readerAdapter;
    private final WriterAdapter writerAdapter;

    ParameterType(Class<?> primitiveClass, Class<?> wrapperClass, ReaderAdapter readerAdapter, WriterAdapter writerAdapter) {
        this.primitiveClass = primitiveClass;
        this.wrapperClass = wrapperClass;
        this.readerAdapter = readerAdapter;
        this.writerAdapter = writerAdapter;
    }

    public Object readFrom(DataInputStream is) throws IOException {
        return readerAdapter.readFrom(is);
    }

    public void writeTo(DataOutputStream os, Object value) throws IOException {
        writerAdapter.writeTo(os, value);
    }

    public static ParameterType getByType(Class<?> type) {
        if (type.isArray())
            return ARRAY;

        for (ParameterType parameterType : values()) {
            if (Objects.equals(parameterType.primitiveClass, type) || Objects.equals(parameterType.wrapperClass, type)) {
                return parameterType;
            }
        }

        return null;
    }

    private interface WriterAdapter {
        void writeTo(DataOutputStream dataOutputStream, Object value) throws IOException;
    }

    private interface ReaderAdapter {
        Object readFrom(DataInputStream dataInputStream) throws IOException;
    }
}

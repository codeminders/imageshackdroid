package com.codeminders.imageshackdroid.model;

import com.codeminders.imageshackdroid.Constants;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Igor Giziy <linsalion@codeminders.com>
 */
public class CountingMultipartEntity extends MultipartEntity {
    private long transferred;
    private String name;
    private static int maxId;
    private int id;
    private int type;
    private String status;
    private String size;


    public CountingMultipartEntity(String name, int type){
        this.name = name;
        this.type = type;
        status = Constants.STATUS_WAIT;
        id = ++maxId;
    }


    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream));
    }

    public class CountingOutputStream extends FilterOutputStream {
        public CountingOutputStream(final OutputStream out) {
            super(out);
            transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            transferred += len;
        }

        public void write(int b) throws IOException {
            out.write(b);
            transferred++;
        }
    }


    public long getTransferred() {
        return transferred;
    }

    public void setTransferred(long transferred) {
        this.transferred = transferred;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public static void setMaxId(int id) {
        maxId = id;
    }

}

package org.ltzin.database.storage;

public class StorageMetadata {

    private Boolean connect;
    private Integer ping;

    private Long sizeBytes;

    public Boolean connected() {
        return this.connect;
    }

    public Integer ping() {
        return this.ping;
    }

    public Long sizeBytes() {
        return this.sizeBytes;
    }

    public StorageMetadata connected(boolean connect) {
        this.connect = connect;
        return this;
    }

    public StorageMetadata ping(int ping) {
        this.ping = ping;
        return this;
    }

    public StorageMetadata sizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
        return this;
    }

    public StorageMetadata combine(StorageMetadata other) {
        if (this.connect == null || (other.connect != null && !other.connect)) {
            this.connect = other.connect;
        }

        if (this.ping == null || (other.ping != null && other.ping > this.ping)) {
            this.ping = other.ping;
        }

        if (this.sizeBytes == null || (other.sizeBytes != null && other.sizeBytes > this.sizeBytes)) {
            this.sizeBytes = other.sizeBytes;
        }

        return this;
    }
}

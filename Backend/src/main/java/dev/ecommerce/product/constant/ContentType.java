package dev.ecommerce.product.constant;

public enum ContentType {
    TEXT, IMAGE, VIDEO;

    public boolean isMedia() {
        return this == IMAGE || this == VIDEO;
    }
}

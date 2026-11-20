package com.medicinelocator.auth.application.query;

public class GetAllUsersQuery {

    private final int page;
    private final int size;

    public GetAllUsersQuery(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
}
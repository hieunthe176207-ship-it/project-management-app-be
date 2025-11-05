package com.fpt.project.constant;

public enum Role {
    OWNER("Quản lý"),
    MEMBER("Thành viên");

    private final String nhan;

    Role(String nhan) {
        this.nhan = nhan;
    }

    public String getNhan() {
        return nhan;
    }
}

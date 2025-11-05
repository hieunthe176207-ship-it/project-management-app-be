package com.fpt.project.constant;

public enum Role {
    OWNER("Chủ sở hữu"),
    MEMBER("Thành viên");

    private final String nhan;

    Role(String nhan) {
        this.nhan = nhan;
    }

    public String getNhan() {
        return nhan;
    }
}

package com.xllyll.fire.model;

public enum VegetationType {
    NONE("none","无植被"),
    SPARSE("sparse","稀疏植被"),
    NORMAL("normal","一般植被"),
    DENSE("dense","密集植被");
    private final String code;
    private final String desc;
    VegetationType(String code,String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }
}

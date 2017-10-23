package com.lhrsite.jdbc.enums;

/**
 *
 * @author 刘浩然
 * @date 2017/7/27
 */
public enum ParamType {

    INPARAM(0, "入参"),
    OUTPARAM(1, "出参"),
    ;

    private int type;
    private String typeName;



    ParamType(int type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}

package com.kisro.poi.enums;

import lombok.Getter;

/**
 * @author zoutao
 * @since 2023/4/11
 **/
@Getter
public enum FuelTgTypeEnum {

    /**
     * 燃料类型(FV:重型柴油车,GV:重型燃气车,DV:双燃料汽车,FEV:油电混合车,GEV:气电混合汽车)
     */
    FV("FV", "重型柴油车"),
    GV("GV", "重型燃气车"),
    DV("DV", "双燃料汽车"),
    FEV("FEV", "油电混合车"),
    GEV("GEV", "气电混合汽车"),
    NONE("", "");

    private final String code;
    private final String name;

    FuelTgTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;

    }

    public static FuelTgTypeEnum getByCode(String code) throws Exception {
        for (FuelTgTypeEnum e : FuelTgTypeEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return NONE;
    }

    public static String getNameByCode(String code) {
        for (FuelTgTypeEnum e : FuelTgTypeEnum.values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return NONE.getName();
    }

    public static String getCodeByName(String name) {
        for (FuelTgTypeEnum e : FuelTgTypeEnum.values()) {
            if (e.getName().equals(name)) {
                return e.getCode();
            }
        }
        return NONE.getCode();
    }

}

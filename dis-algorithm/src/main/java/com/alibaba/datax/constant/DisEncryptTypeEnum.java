package com.alibaba.datax.constant;

public enum DisEncryptTypeEnum {


    NULL
    , HIDING
    , RSA, RSA_DECRYPT
    , SM2, SM2_DECRYPT
    , SM4, SM4_DECRYPT

    ;

    public static DisEncryptTypeEnum getEncryptType(String code) {
        for (DisEncryptTypeEnum value : DisEncryptTypeEnum.values()) {
            if (value.name().equals(code)) {
                return value;
            }
        }
        return NULL;
    }

}

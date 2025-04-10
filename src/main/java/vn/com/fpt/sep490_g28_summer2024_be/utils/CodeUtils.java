package vn.com.fpt.sep490_g28_summer2024_be.utils;


import org.springframework.stereotype.Component;

import java.math.BigInteger;


@Component
public class CodeUtils {
    public String genCode(String prefix, BigInteger id){
        StringBuilder numString = new StringBuilder(id.toString());
        while (numString.toString().length() < 3){
            numString.insert(0, "0");
        }
        return prefix + numString.toString();
    }

}

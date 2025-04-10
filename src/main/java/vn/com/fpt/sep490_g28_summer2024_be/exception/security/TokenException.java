package vn.com.fpt.sep490_g28_summer2024_be.exception.security;

public class TokenException extends RuntimeException{
    public TokenException(String message) {
        super(message);
    }
}

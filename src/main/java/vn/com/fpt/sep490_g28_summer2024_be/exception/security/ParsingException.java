package vn.com.fpt.sep490_g28_summer2024_be.exception.security;

public class ParsingException extends RuntimeException{
    public ParsingException(String message) {
        super(message);
    }
}

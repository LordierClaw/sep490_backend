package vn.com.fpt.sep490_g28_summer2024_be.service.email;

import vn.com.fpt.sep490_g28_summer2024_be.utils.Email;

import java.util.concurrent.CompletableFuture;

public interface EmailService {

    CompletableFuture<Void> sendEmail(Email email);
}

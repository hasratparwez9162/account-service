package com.bank.app.account_service.util;

import java.time.LocalDate;
import java.util.Random;

public class AccountUtil {

    /**
     * Generates a unique account number.
     * @return A randomly generated account number.
     */
    public static String generateAccountNumber() {
        // Get the current year
        int year = LocalDate.now().getYear();

        // Generate a random 6-digit number
        Random random = new Random();
        int randomDigits = 100000 + random.nextInt(900000); // Ensures it's a 6-digit number

        // Combine the year and the random number to form the account number
        return String.valueOf(year) + randomDigits;
    }
}
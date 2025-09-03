package io.ghassen.pockito.domain;

/**
 * Wallet type enum defining the different categories of wallets.
 * 
 * This enum provides the various wallet types that can be assigned to a wallet entity,
 * allowing users to categorize their financial accounts and instruments.
 */
public enum WalletType {
    BANK_ACCOUNT,
    CASH,
    CREDIT_CARD,
    SAVINGS,
    CUSTOM;

    @Override
    public String toString() {
        return name();
    }
}

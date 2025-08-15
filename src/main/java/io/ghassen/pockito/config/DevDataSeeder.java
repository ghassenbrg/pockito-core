package io.ghassen.pockito.config;

import io.ghassen.pockito.domain.Currency;
import io.ghassen.pockito.repo.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

  private final CurrencyRepository currencyRepository;

  @Override
  public void run(String... args) throws Exception {
    log.info("Starting dev data seeding...");
    
    seedCurrencies();
    
    log.info("Dev data seeding completed successfully");
    log.info("Note: User seeding will occur automatically when first authenticated");
  }

  private void seedCurrencies() {
    if (currencyRepository.count() == 0) {
      log.info("Seeding currencies...");
      
      Currency usd = Currency.builder()
        .code("USD")
        .name("US Dollar")
        .symbol("$")
        .decimals((short) 2)
        .isActive(true)
        .createdBy("system")
        .updatedBy("system")
        .build();
      
      Currency eur = Currency.builder()
        .code("EUR")
        .name("Euro")
        .symbol("€")
        .decimals((short) 2)
        .isActive(true)
        .createdBy("system")
        .updatedBy("system")
        .build();
      
      Currency jpy = Currency.builder()
        .code("JPY")
        .name("Japanese Yen")
        .symbol("¥")
        .decimals((short) 0)
        .isActive(true)
        .createdBy("system")
        .updatedBy("system")
        .build();

        Currency tnd = Currency.builder()
        .code("TND")
        .name("Tunisian Dinar")
        .symbol("DT")
        .decimals((short) 0)
        .isActive(true)
        .createdBy("system")
        .updatedBy("system")
        .build();
      
      currencyRepository.save(usd);
      currencyRepository.save(eur);
      currencyRepository.save(jpy);
      currencyRepository.save(tnd);

      log.info("Seeded {} currencies", currencyRepository.count());
    } else {
      log.info("Currencies already exist, skipping currency seeding");
    }
  }
}

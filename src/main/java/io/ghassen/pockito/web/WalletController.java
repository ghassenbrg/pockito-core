package io.ghassen.pockito.web;

import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.service.WalletService;
import io.ghassen.pockito.web.dto.WalletDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {
  
  private final WalletService service;

  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public List<WalletDtos.Resp> list() {
    log.debug("Listing wallets");
    return service.list().stream().map(this::toResp).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('USER')")
  public WalletDtos.Resp get(@PathVariable UUID id) {
    log.debug("Getting wallet: {}", id);
    return toResp(service.get(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<WalletDtos.Resp> create(@Valid @RequestBody WalletDtos.CreateReq req) {
    log.debug("Creating wallet: {}", req.name());
    Wallet wallet = service.create(req);
    URI location = URI.create("/api/wallets/" + wallet.getId());
    return ResponseEntity.created(location).body(toResp(wallet));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('USER')")
  public WalletDtos.Resp update(@PathVariable UUID id, @Valid @RequestBody WalletDtos.UpdateReq req) {
    log.debug("Updating wallet: {}", id);
    return toResp(service.update(id, req));
  }

  @PostMapping("/{id}/archive")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Void> archive(@PathVariable UUID id) {
    log.debug("Archiving wallet: {}", id);
    service.archive(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Void> activate(@PathVariable UUID id) {
    log.debug("Activating wallet: {}", id);
    service.activate(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/default")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Void> setDefault(@PathVariable UUID id) {
    log.debug("Setting wallet as default: {}", id);
    service.setDefault(id);
    return ResponseEntity.noContent().build();
  }

  private WalletDtos.Resp toResp(Wallet wallet) {
    return new WalletDtos.Resp(
      wallet.getId(),
      wallet.getName(),
      wallet.getIconType(),
      wallet.getIconValue(),
      wallet.getCurrencyCode(),
      wallet.getColor(),
      wallet.getType(),
      wallet.getInitialBalance(),
      wallet.isDefault(),
      wallet.getGoalAmount(),
      wallet.getUserId(),
      wallet.getCreatedAt(),
      wallet.getUpdatedAt()
    );
  }
}

package io.ghassen.pockito.web;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.service.CategoryService;
import io.ghassen.pockito.web.dto.CategoryDtos;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
  
  private final CategoryService service;

  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public List<CategoryDtos.Resp> list(
      @RequestParam(defaultValue = "true") boolean includeArchived) {
    log.debug("Listing categories with includeArchived: {}", includeArchived);
    return service.list(includeArchived).stream().map(this::toResp).toList();
  }

  @GetMapping("/type/{type}")
  @PreAuthorize("hasRole('USER')")
  public List<CategoryDtos.Resp> getByType(
      @PathVariable Category.CategoryType type,
      @RequestParam(defaultValue = "true") boolean includeArchived) {
    log.debug("Getting categories by type: {} with includeArchived: {}", type, includeArchived);
    return service.listByType(type, includeArchived).stream().map(this::toResp).toList();
  }

  @GetMapping("/search")
  @PreAuthorize("hasRole('USER')")
  public List<CategoryDtos.Resp> search(
      @RequestParam(required = false) String searchTerm,
      @RequestParam(defaultValue = "true") boolean includeArchived) {
    log.debug("Searching categories with term: '{}', includeArchived: {}", searchTerm, includeArchived);
    return service.searchCategories(searchTerm, includeArchived).stream().map(this::toResp).toList();
  }

  @GetMapping("/status/active")
  @PreAuthorize("hasRole('USER')")
  public List<CategoryDtos.Resp> getActiveCategories() {
    log.debug("Getting active categories only");
    return service.list(false).stream().map(this::toResp).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('USER')")
  public CategoryDtos.Resp get(@PathVariable UUID id) {
    log.debug("Getting category: {}", id);
    return toResp(service.get(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<CategoryDtos.Resp> create(@Valid @RequestBody CategoryDtos.CreateReq req) {
    log.debug("Creating category: {}", req.name());
    Category category = service.create(req);
    URI location = URI.create("/api/categories/" + category.getId());
    return ResponseEntity.created(location).body(toResp(category));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('USER')")
  public CategoryDtos.Resp update(@PathVariable UUID id, @Valid @RequestBody CategoryDtos.UpdateReq req) {
    log.debug("Updating category: {}", id);
    return toResp(service.update(id, req));
  }

  @PostMapping("/{id}/archive")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Void> archive(@PathVariable UUID id) {
    log.debug("Archiving category: {}", id);
    service.archive(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Void> activate(@PathVariable UUID id) {
    log.debug("Activating category: {}", id);
    service.activate(id);
    return ResponseEntity.noContent().build();
  }

  private CategoryDtos.Resp toResp(Category category) {
    return new CategoryDtos.Resp(
      category.getId(),
      category.getName(),
      category.getType(),
      category.getColor(),
      category.getIconType(),
      category.getIconValue(),
      category.getParent() != null ? category.getParent().getId() : null,
      category.getParent() != null ? category.getParent().getName() : null,
      category.getArchivedAt() == null,
      category.getUserId(),
      category.getCreatedAt(),
      category.getUpdatedAt()
    );
  }
}

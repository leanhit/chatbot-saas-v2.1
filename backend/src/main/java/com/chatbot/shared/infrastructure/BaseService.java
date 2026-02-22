package com.chatbot.shared.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID, R extends BaseRepository<T, ID>> {

    protected final R repository;

    protected BaseService(R repository) {
        this.repository = repository;
    }

    @Transactional
    public T save(T entity) {
        return repository.save(entity);
    }

    @Transactional
    public List<T> saveAll(List<T> entities) {
        return repository.saveAll(entities);
    }

    @Transactional
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Transactional
    public void delete(T entity) {
        repository.delete(entity);
    }

    @Transactional
    public void deleteAll(List<T> entities) {
        repository.deleteAll(entities);
    }

    @Transactional
    public void softDelete(T entity) {
        repository.softDelete(entity);
    }

    @Transactional
    public void softDeleteById(ID id) {
        repository.softDeleteById(id);
    }

    @Transactional
    public void restore(T entity) {
        repository.restore(entity);
    }

    @Transactional
    public void restoreById(ID id) {
        repository.restoreById(id);
    }

    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    public Optional<T> findByIdAndActive(ID id) {
        return repository.findByIdAndActive(id);
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public List<T> findAllActive() {
        return repository.findAllActive();
    }

    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<T> findAllActive(Pageable pageable) {
        return repository.findAllActive(pageable);
    }

    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    public boolean existsByIdAndActive(ID id) {
        return repository.existsByIdAndActive(id);
    }

    public long count() {
        return repository.count();
    }

    public long countActive() {
        return repository.countActive();
    }

    @Transactional
    public void deleteInactive() {
        repository.deleteInactive();
    }

    protected void validateEntity(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
    }

    protected void validateId(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }

    protected T requireEntity(Optional<T> optional) {
        return optional.orElseThrow(() -> 
            new RuntimeException("Entity not found"));
    }

    protected T requireEntity(ID id) {
        return requireEntity(findById(id));
    }

    protected T requireActiveEntity(ID id) {
        return requireEntity(findByIdAndActive(id));
    }

    protected boolean isActive(T entity) {
        return entity instanceof BaseEntity && ((BaseEntity) entity).isActive();
    }

    protected void setActive(T entity, boolean active) {
        if (entity instanceof BaseEntity) {
            ((BaseEntity) entity).setActive(active);
        }
    }

    protected void activate(T entity) {
        setActive(entity, true);
    }

    protected void deactivate(T entity) {
        setActive(entity, false);
    }

    protected void updateAuditFields(T entity, String updatedBy) {
        if (entity instanceof AuditEntity) {
            AuditEntity auditEntity = (AuditEntity) entity;
            auditEntity.setUpdatedBy(updatedBy);
            auditEntity.setUpdatedAt(java.time.LocalDateTime.now());
        }
    }

    protected void createAuditFields(T entity, String createdBy) {
        if (entity instanceof AuditEntity) {
            AuditEntity auditEntity = (AuditEntity) entity;
            auditEntity.setCreatedBy(createdBy);
            auditEntity.setCreatedAt(java.time.LocalDateTime.now());
            auditEntity.setUpdatedBy(createdBy);
            auditEntity.setUpdatedAt(java.time.LocalDateTime.now());
        }
    }

    @Transactional
    public T create(T entity, String createdBy) {
        validateEntity(entity);
        createAuditFields(entity, createdBy);
        return save(entity);
    }

    @Transactional
    public T update(T entity, String updatedBy) {
        validateEntity(entity);
        updateAuditFields(entity, updatedBy);
        return save(entity);
    }

    @Transactional
    public T createOrUpdate(T entity, String userId) {
        validateEntity(entity);
        if (entity instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) entity;
            if (baseEntity.getId() == null) {
                return create(entity, userId);
            } else {
                return update(entity, userId);
            }
        }
        return save(entity);
    }
}

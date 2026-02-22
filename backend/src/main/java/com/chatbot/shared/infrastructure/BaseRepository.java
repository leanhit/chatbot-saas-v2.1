package com.chatbot.shared.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    default Optional<T> findByIdAndActive(ID id) {
        return findById(id).filter(entity -> {
            if (entity instanceof BaseEntity) {
                return ((BaseEntity) entity).isActive();
            }
            return true;
        });
    }

    default List<T> findAllActive() {
        return findAll().stream()
                .filter(entity -> {
                    if (entity instanceof BaseEntity) {
                        return ((BaseEntity) entity).isActive();
                    }
                    return true;
                })
                .toList();
    }

    default Page<T> findAllActive(Pageable pageable) {
        return findAll(pageable).map(entity -> {
            if (entity instanceof BaseEntity) {
                return ((BaseEntity) entity).isActive() ? entity : null;
            }
            return entity;
        });
    }

    default void softDelete(T entity) {
        if (entity instanceof BaseEntity) {
            ((BaseEntity) entity).setActive(false);
            save(entity);
        } else {
            delete(entity);
        }
    }

    default void softDeleteById(ID id) {
        findById(id).ifPresent(this::softDelete);
    }

    default void restore(T entity) {
        if (entity instanceof BaseEntity) {
            ((BaseEntity) entity).setActive(true);
            save(entity);
        }
    }

    default void restoreById(ID id) {
        findById(id).ifPresent(this::restore);
    }

    default boolean existsByIdAndActive(ID id) {
        return existsById(id) && findByIdAndActive(id).isPresent();
    }

    default long countActive() {
        return count();
    }

    default void deleteInactive() {
        findAll().stream()
                .filter(entity -> entity instanceof BaseEntity && !((BaseEntity) entity).isActive())
                .forEach(this::delete);
    }
}

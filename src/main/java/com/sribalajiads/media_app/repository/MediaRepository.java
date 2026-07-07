package com.sribalajiads.media_app.repository;

import com.sribalajiads.media_app.model.Company;
import com.sribalajiads.media_app.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    Page<Media> findByMediaCodeContainingIgnoreCase(String mediaCode, Pageable pageable);

    Page<Media> findByMediaTypeIgnoreCaseAndMediaCodeContainingIgnoreCase(String mediaType, String mediaCode, Pageable pageable);

    Page<Media> findByBelongsToAndMediaCodeContainingIgnoreCase(Company company, String mediaCode, Pageable pageable);

    Page<Media> findByBelongsToAndMediaTypeIgnoreCaseAndMediaCodeContainingIgnoreCase(Company company, String mediaType, String mediaCode, Pageable pageable);

    List<Media> findByMediaCodeIn(List<String> codes);
}

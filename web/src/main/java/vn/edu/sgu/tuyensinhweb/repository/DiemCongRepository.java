package vn.edu.sgu.tuyensinhweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.tuyensinhweb.model.DiemCongXetTuyen;
import java.util.Optional;

public interface DiemCongRepository extends JpaRepository<DiemCongXetTuyen, Integer> {
    Optional<DiemCongXetTuyen> findByTsCccd(String cccd);
}

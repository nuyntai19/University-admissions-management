package vn.edu.sgu.tuyensinhweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.tuyensinhweb.model.DiemCongXetTuyen;
import java.util.List;

public interface DiemCongRepository extends JpaRepository<DiemCongXetTuyen, Integer> {
    List<DiemCongXetTuyen> findByTsCccd(String cccd);
}

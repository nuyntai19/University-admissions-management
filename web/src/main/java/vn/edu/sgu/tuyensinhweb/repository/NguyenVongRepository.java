package vn.edu.sgu.tuyensinhweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.tuyensinhweb.model.NguyenVongXetTuyen;
import java.util.List;

public interface NguyenVongRepository extends JpaRepository<NguyenVongXetTuyen, Integer> {
    List<NguyenVongXetTuyen> findByNvCccdOrderByNvTtAsc(String cccd);
}

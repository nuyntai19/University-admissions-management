package vn.edu.sgu.tuyensinhweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.sgu.tuyensinhweb.model.DiemThiXetTuyen;
import java.util.List;

public interface DiemThiRepository extends JpaRepository<DiemThiXetTuyen, Integer> {
    List<DiemThiXetTuyen> findByCccd(String cccd);

    @Query("SELECT d FROM DiemThiXetTuyen d WHERE d.cccd = :cccd AND d.phuongThuc = :pt")
    List<DiemThiXetTuyen> findByCccdAndPhuongThuc(@Param("cccd") String cccd, @Param("pt") String pt);
}

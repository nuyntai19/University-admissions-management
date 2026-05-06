package vn.edu.sgu.tuyensinhweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.tuyensinhweb.model.Nganh;
import java.util.Optional;
import java.util.List;

public interface NganhRepository extends JpaRepository<Nganh, Integer> {
    Optional<Nganh> findByMaNganh(String maNganh);
    List<Nganh> findAllByOrderByMaNganhAsc();
    /** Chỉ lấy ngành hỗ trợ phương thức ĐGNL */
    List<Nganh> findByDgnl(String dgnl);
    /** Chỉ lấy ngành hỗ trợ phương thức V-SAT */
    List<Nganh> findByVsat(String vsat);
}

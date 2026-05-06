package vn.edu.sgu.tuyensinhweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.tuyensinhweb.model.ThiSinh;
import java.util.Optional;

public interface ThiSinhRepository extends JpaRepository<ThiSinh, Integer> {
    Optional<ThiSinh> findByCccd(String cccd);
    Optional<ThiSinh> findByCccdAndPassword(String cccd, String password);
}

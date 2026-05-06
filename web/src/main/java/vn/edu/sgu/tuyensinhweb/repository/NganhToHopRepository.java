package vn.edu.sgu.tuyensinhweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.tuyensinhweb.model.NganhToHop;
import java.util.List;

public interface NganhToHopRepository extends JpaRepository<NganhToHop, Integer> {
    List<NganhToHop> findByMaNganh(String maNganh);
}

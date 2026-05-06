package vn.edu.sgu.tuyensinhweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.tuyensinhweb.model.ToHopMon;
import java.util.Optional;

public interface ToHopMonRepository extends JpaRepository<ToHopMon, Integer> {
    Optional<ToHopMon> findByMaToHop(String maToHop);
}

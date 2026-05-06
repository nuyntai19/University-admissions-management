package vn.edu.sgu.tuyensinhweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.sgu.tuyensinhweb.model.BangQuyDoi;
import java.util.List;

public interface BangQuyDoiRepository extends JpaRepository<BangQuyDoi, Integer> {

    @Query("SELECT b FROM BangQuyDoi b WHERE b.dPhuongThuc = :pt")
    List<BangQuyDoi> findByPhuongThuc(@Param("pt") String phuongThuc);

    @Query("SELECT b FROM BangQuyDoi b WHERE b.dPhuongThuc = :pt AND b.dMon = :mon")
    List<BangQuyDoi> findByPhuongThucAndMon(@Param("pt") String phuongThuc, @Param("mon") String mon);

    @Query("SELECT b FROM BangQuyDoi b WHERE b.dPhuongThuc = :pt AND b.dToHop = :th")
    List<BangQuyDoi> findByPhuongThucAndToHop(@Param("pt") String phuongThuc, @Param("th") String toHop);
}

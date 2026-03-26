package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.BangQuyDoiDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi;
import java.util.List;

public class BangQuyDoiBUS {
    private BangQuyDoiDAO dao = new BangQuyDoiDAO();

    public List<BangQuyDoi> getAll() {
        return dao.getAll();
    }

    public BangQuyDoi getByMaQuyDoi(String maQuyDoi) {
        return dao.getByMaQuyDoi(maQuyDoi);
    }

    public boolean add(BangQuyDoi bqd) {
        if (bqd.getDMaQuyDoi() == null || bqd.getDMaQuyDoi().trim().isEmpty()) {
            System.out.println("Mã quy đổi không được để trống!");
            return false;
        }
        return dao.add(bqd);
    }

    public boolean update(BangQuyDoi bqd) {
        return dao.update(bqd);
    }

    public boolean delete(int idQd) {
        return dao.delete(idQd);
    }
}

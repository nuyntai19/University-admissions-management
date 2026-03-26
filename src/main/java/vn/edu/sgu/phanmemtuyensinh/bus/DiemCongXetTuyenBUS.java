package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.DiemCongXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import java.util.List;

public class DiemCongXetTuyenBUS {
    private DiemCongXetTuyenDAO dao = new DiemCongXetTuyenDAO();

    public List<DiemCongXetTuyen> getAll() {
        return dao.getAll();
    }

    public DiemCongXetTuyen getByByCccd(String cccd) {
        return dao.getByByCccd(cccd);
    }

    public boolean add(DiemCongXetTuyen diem) {
        if (diem.getTsCccd() == null || diem.getTsCccd().trim().isEmpty()) {
            System.out.println("CCCD thí sinh không được để trống!");
            return false;
        }
        if (diem.getDcKeys() == null || diem.getDcKeys().trim().isEmpty()) {
            System.out.println("Khóa không được để trống!");
            return false;
        }
        return dao.add(diem);
    }

    public boolean update(DiemCongXetTuyen diem) {
        return dao.update(diem);
    }

    public boolean delete(int idDiemCong) {
        return dao.delete(idDiemCong);
    }
}

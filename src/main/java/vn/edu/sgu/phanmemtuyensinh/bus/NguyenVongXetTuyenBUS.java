package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.NguyenVongXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;
import java.util.List;

public class NguyenVongXetTuyenBUS {
    private NguyenVongXetTuyenDAO dao = new NguyenVongXetTuyenDAO();

    public List<NguyenVongXetTuyen> getAll() {
        return dao.getAll();
    }

    public List<NguyenVongXetTuyen> getByCccd(String cccd) {
        return dao.getByCccd(cccd);
    }

    public boolean add(NguyenVongXetTuyen nv) {
        if (nv.getNnCccd() == null || nv.getNnCccd().trim().isEmpty()) {
            System.out.println("CCCD không được để trống!");
            return false;
        }
        if (nv.getNvMaNganh() == null || nv.getNvMaNganh().trim().isEmpty()) {
            System.out.println("Mã ngành không được để trống!");
            return false;
        }
        return dao.add(nv);
    }

    public boolean update(NguyenVongXetTuyen nv) {
        return dao.update(nv);
    }

    public boolean delete(int idNv) {
        return dao.delete(idNv);
    }
}

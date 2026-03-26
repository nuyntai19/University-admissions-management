package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.NganhToHopDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NganhToHop;
import java.util.List;

public class NganhToHopBUS {
    private NganhToHopDAO dao = new NganhToHopDAO();

    public List<NganhToHop> getAll() {
        return dao.getAll();
    }

    public List<NganhToHop> getByMaNganh(String maNganh) {
        return dao.getByMaNganh(maNganh);
    }

    public boolean add(NganhToHop nth) {
        if (nth.getMaNganh() == null || nth.getMaNganh().trim().isEmpty()) {
            System.out.println("Mã ngành không được để trống!");
            return false;
        }
        if (nth.getMaToHop() == null || nth.getMaToHop().trim().isEmpty()) {
            System.out.println("Mã tổ hợp không được để trống!");
            return false;
        }
        return dao.add(nth);
    }

    public boolean update(NganhToHop nth) {
        return dao.update(nth);
    }

    public boolean delete(int id) {
        return dao.delete(id);
    }
}

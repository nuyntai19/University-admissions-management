package vn.edu.sgu.phanmemtuyensinh.dal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;

public class NguyenVongXetTuyenDAO {

    public List<NguyenVongXetTuyen> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM NguyenVongXetTuyen", NguyenVongXetTuyen.class).list();
        }
    }

    public NguyenVongXetTuyen getById(int idNv) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(NguyenVongXetTuyen.class, idNv);
        }
    }

    public List<NguyenVongXetTuyen> getByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery("FROM NguyenVongXetTuyen WHERE nvCccd = :cccd", NguyenVongXetTuyen.class)
                    .setParameter("cccd", cccd).list();
        }
    }

    public List<NguyenVongXetTuyen> getByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM NguyenVongXetTuyen WHERE nvMaNganh = :ma", NguyenVongXetTuyen.class)
                    .setParameter("ma", maNganh).list();
        }
    }

    public Map<String, Long> countByMaNganh() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                    "SELECT nv.nvMaNganh, COUNT(nv) FROM NguyenVongXetTuyen nv GROUP BY nv.nvMaNganh",
                    Object[].class)
                    .list();
            Map<String, Long> result = new HashMap<>();
            for (Object[] row : rows) {
                if (row[0] != null) {
                    result.put(String.valueOf(row[0]), (Long) row[1]);
                }
            }
            return result;
        }
    }

    /**
     * Đếm nguyện vọng theo từng ngành và từng phương thức.
     * Key = "MANGANH|PHUONGTHUC", value = số lượng.
     */
    public Map<String, Long> countByMaNganhAndPhuongThuc() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                    "SELECT nv.nvMaNganh, nv.ttPhuongThuc, COUNT(nv) " +
                    "FROM NguyenVongXetTuyen nv " +
                    "GROUP BY nv.nvMaNganh, nv.ttPhuongThuc",
                    Object[].class)
                    .list();
            Map<String, Long> result = new HashMap<>();
            for (Object[] row : rows) {
                String maNganh = row[0] == null ? "" : String.valueOf(row[0]);
                String pt = row[1] == null ? "" : String.valueOf(row[1]);
                Long cnt = (Long) row[2];
                result.put(maNganh + "|" + pt.toUpperCase(), cnt);
            }
            return result;
        }
    }

    public List<Object[]> getTrungTuyenChiTiet() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT nv.nvMaNganh, nv.nvTenMaNganh, nv.nvCccd, nv.nvTt, nv.ttPhuongThuc, nv.ttThm, "
                                    + "nv.diemThxt, nv.diemCong, nv.diemUtqd, nv.diemXetTuyen "
                                    + "FROM NguyenVongXetTuyen nv "
                                    + "WHERE nv.nvKetQua = 'Trúng tuyển' "
                                    + "ORDER BY nv.nvMaNganh, nv.ttPhuongThuc, nv.diemXetTuyen DESC",
                            Object[].class)
                    .list();
        }
    }

    public List<Object[]> getThongKeTrungTuyenTheoNganhPhuongThuc() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Nhóm THPT và trống cùng nhau, chỉ tách riêng ĐGNL và V-SAT
            return session.createQuery(
                            "SELECT nv.nvMaNganh, nv.nvTenMaNganh, "
                                    + "CASE WHEN nv.ttPhuongThuc IN ('\u0110GNL', 'DGNL', 'V-SAT', 'VSAT') THEN nv.ttPhuongThuc ELSE 'THPT / Kh\u00f4ng' END AS ptGroup, "
                                    + "COUNT(nv) "
                                    + "FROM NguyenVongXetTuyen nv "
                                    + "WHERE nv.nvKetQua = 'Tr\u00fang tuy\u1ec3n' "
                                    + "GROUP BY nv.nvMaNganh, nv.nvTenMaNganh, "
                                    + "CASE WHEN nv.ttPhuongThuc IN ('\u0110GNL', 'DGNL', 'V-SAT', 'VSAT') THEN nv.ttPhuongThuc ELSE 'THPT / Kh\u00f4ng' END "
                                    + "ORDER BY nv.nvMaNganh, ptGroup",
                            Object[].class)
                    .list();
        }
    }

    public Map<String, Long> getTotalCountsByStatus() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                    "SELECT nv.nvKetQua, COUNT(nv) FROM NguyenVongXetTuyen nv GROUP BY nv.nvKetQua",
                    Object[].class).list();
            Map<String, Long> result = new HashMap<>();
            long total = 0;
            for (Object[] row : rows) {
                String status = row[0] == null ? "Ch\u01b0a x\u00e9t" : row[0].toString();
                long count = (Long) row[1];
                result.put(status, count);
                total += count;
            }
            result.put("T\u1ed5ng", total);
            return result;
        }
    }

    /**
     * L\u1ea5y d\u1eef li\u1ec7u th\u1ed1ng k\u00ea t\u1ed5ng h\u1ee3p cho b\u00e1o c\u00e1o dashboard.
     * Tr\u1ea3 v\u1ec1: MaNganh, TenNganh, ChiTieu, SL_TT, SL_DGNL, SL_THPT, SL_VSAT, Tong_TT, DiemChuan
     */
    public List<Object[]> getReportDashboard() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createNativeQuery(
                "SELECT n.manganh, n.tennganh, n.n_chitieu, " +
                "SUM(CASE WHEN nv.tt_phuongthuc LIKE '%TUY\u1ec4N TH\u1eb2NG%' OR nv.tt_phuongthuc LIKE '%TUYEN THANG%' THEN 1 ELSE 0 END) as sl_tt, " +
                "SUM(CASE WHEN nv.tt_phuongthuc IN ('\u0110GNL', 'DGNL') THEN 1 ELSE 0 END) as sl_dgnl, " +
                "SUM(CASE WHEN nv.tt_phuongthuc = 'THPT' OR nv.tt_phuongthuc IS NULL OR nv.tt_phuongthuc = '' THEN 1 ELSE 0 END) as sl_thpt, " +
                "SUM(CASE WHEN nv.tt_phuongthuc IN ('V-SAT', 'VSAT') THEN 1 ELSE 0 END) as sl_vsat, " +
                "COUNT(nv.idnv) as tong_tt, " +
                "n.n_diemtrungtuyen " +
                "FROM xt_nganh n " +
                "LEFT JOIN xt_nguyenvongxettuyen nv ON n.manganh = nv.nv_manganh AND nv.nv_ketqua = 'Tr\u00fang tuy\u1ec3n' " +
                "GROUP BY n.manganh, n.tennganh, n.n_chitieu, n.n_diemtrungtuyen " +
                "ORDER BY n.manganh", Object[].class).list();
        }
    }

    public boolean add(NguyenVongXetTuyen nv) {
        Transaction transaction = null;
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.persist(nv);

            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback(); 
            }
            e.printStackTrace();
            return false;

        } finally {
            if (session != null && session.isOpen()) {
                session.close(); 
            }
        }
    }

    public boolean addList(List<NguyenVongXetTuyen> list) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int batchSize = 100;
            for (int i = 0; i < list.size(); i++) {
                session.persist(list.get(i));
                if (i > 0 && i % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(NguyenVongXetTuyen nv) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(nv);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateList(List<NguyenVongXetTuyen> list) {
        if (list == null || list.isEmpty()) return true;
        org.hibernate.StatelessSession session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openStatelessSession();
            transaction = session.beginTransaction();
            for (NguyenVongXetTuyen nv : list) {
                session.update(nv);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    public boolean delete(int idNv) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            NguyenVongXetTuyen nv = session.get(NguyenVongXetTuyen.class, idNv);
            if (nv != null) {
                session.remove(nv);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
    
//    public boolean exists(){
//        
//    }
}

package vn.edu.sgu.phanmemtuyensinh.dal;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;
import java.util.List;

public class DiemCongXetTuyenDAO {

    /**
     * Lấy toàn bộ danh sách điểm cộng từ database
     */
    public List<DiemCongXetTuyen> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemCongXetTuyen", DiemCongXetTuyen.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tìm kiếm CCCD từ bảng thí sinh để phục vụ chức năng Suggestions trên GUI
     * Lưu ý: "ThiSinhXetTuyen25" là tên Class Entity tương ứng với bảng thí sinh của bạn
     */
    public List<String> searchCccd(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t.cccd FROM ThiSinh t WHERE t.cccd LIKE :kw";
            return session.createQuery(hql, String.class)
                          .setParameter("kw", keyword + "%")
                          .setMaxResults(10)
                          .list();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Thêm mới hoặc cập nhật bản ghi điểm cộng (Sử dụng saveOrUpdate/merge)
     */
    public boolean saveOrUpdate(DiemCongXetTuyen diem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(diem); // Tự động nhận diện thêm mới hoặc cập nhật dựa trên ID
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean add(DiemCongXetTuyen diem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Kiểm tra trùng dc_keys trước khi thêm
            if (isExistedKey(diem.getDcKeys())) {
                System.err.println("dc_keys đã tồn tại!");
                return false;
            }

            session.persist(diem); // chỉ thêm mới
            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa bản ghi điểm cộng theo ID
     */
    public boolean delete(int idDiemCong) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DiemCongXetTuyen diem = session.get(DiemCongXetTuyen.class, idDiemCong);
            if (diem != null) {
                session.remove(diem);
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

    /**
     * Kiểm tra sự tồn tại của dc_keys để tránh lỗi Duplicate Key trước khi lưu
     */
    public boolean isExistedKey(String dcKey) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(d.idDiemCong) FROM DiemCongXetTuyen d WHERE d.dcKeys = :key";
            Long count = session.createQuery(hql, Long.class)
                                .setParameter("key", dcKey)
                                .uniqueResult();
            return count != null && count > 0;
        }
    }
}
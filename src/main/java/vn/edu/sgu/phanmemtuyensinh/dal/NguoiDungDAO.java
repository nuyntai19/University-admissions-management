package vn.edu.sgu.phanmemtuyensinh.dal;

import org.hibernate.Session;
import org.hibernate.Transaction;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;
import java.util.List;

public class NguoiDungDAO {

    public List<NguoiDung> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM NguoiDung", NguoiDung.class).list();
        }
    }

    public NguoiDung getByTaiKhoan(String taiKhoan) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM NguoiDung WHERE taiKhoan = :tk", NguoiDung.class)
                    .setParameter("tk", taiKhoan).uniqueResult();
        }
    }

    public boolean add(NguoiDung nguoiDung) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(nguoiDung);
            transaction.commit();
            return true;
        } catch (Exception e) {
            safeRollback(transaction);
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(NguoiDung nguoiDung) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(nguoiDung);
            transaction.commit();
            return true;
        } catch (Exception e) {
            safeRollback(transaction);
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idNguoiDung) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            NguoiDung nd = session.get(NguoiDung.class, idNguoiDung);
            if (nd != null) {
                session.remove(nd);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            safeRollback(transaction);
            e.printStackTrace();
            return false;
        }
    }

    private void safeRollback(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        try {
            if (transaction.getStatus() != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
        } catch (Exception ignored) {
            // Không ném thêm lỗi rollback để tránh che khuất lỗi gốc
        }
    }
}

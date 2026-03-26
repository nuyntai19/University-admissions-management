package vn.edu.sgu.phanmemtuyensinh.dal;

import org.hibernate.Session;
import org.hibernate.Transaction;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;
import java.util.List;

public class DiemCongXetTuyenDAO {

    public List<DiemCongXetTuyen> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemCongXetTuyen", DiemCongXetTuyen.class).list();
        }
    }

    public DiemCongXetTuyen getByByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DiemCongXetTuyen WHERE tsCccd = :cccd", DiemCongXetTuyen.class)
                    .setParameter("cccd", cccd).uniqueResult();
        }
    }

    public boolean add(DiemCongXetTuyen diem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(diem);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(DiemCongXetTuyen diem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(diem);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

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
}

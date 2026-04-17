package vn.edu.sgu.phanmemtuyensinh.dal;

import org.hibernate.Session;
import org.hibernate.Transaction;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;
import java.util.List;

public class BangQuyDoiDAO {

    public List<BangQuyDoi> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM BangQuyDoi", BangQuyDoi.class).list();
        }
    }

    public BangQuyDoi getByMaQuyDoi(String maQuyDoi) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM BangQuyDoi WHERE dMaQuyDoi = :ma", BangQuyDoi.class)
                    .setParameter("ma", maQuyDoi).uniqueResult();
        }
    }

    public List<BangQuyDoi> getByPhuongThuc(String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM BangQuyDoi WHERE dPhuongThuc = :pt ORDER BY dPhanVi ASC", BangQuyDoi.class)
                    .setParameter("pt", phuongThuc)
                    .list();
        }
    }

    public BangQuyDoi findIntervalExclusiveLower(String phuongThuc, String toHop, String mon, java.math.BigDecimal x) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM BangQuyDoi b " +
                            "WHERE b.dPhuongThuc = :pt " +
                            "AND ((:th IS NULL AND b.dToHop IS NULL) OR b.dToHop = :th) " +
                            "AND ((:m IS NULL AND b.dMon IS NULL) OR b.dMon = :m) " +
                            "AND :x > b.dDiemA AND :x <= b.dDiemB " +
                            "ORDER BY b.dDiemB ASC",
                    BangQuyDoi.class)
                    .setParameter("pt", phuongThuc)
                    .setParameter("th", toHop)
                    .setParameter("m", mon)
                    .setParameter("x", x)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    public BangQuyDoi findIntervalInclusive(String phuongThuc, String toHop, String mon, java.math.BigDecimal x) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM BangQuyDoi b " +
                            "WHERE b.dPhuongThuc = :pt " +
                            "AND ((:th IS NULL AND b.dToHop IS NULL) OR b.dToHop = :th) " +
                            "AND ((:m IS NULL AND b.dMon IS NULL) OR b.dMon = :m) " +
                            "AND :x >= b.dDiemA AND :x <= b.dDiemB " +
                            "ORDER BY b.dDiemB ASC",
                    BangQuyDoi.class)
                    .setParameter("pt", phuongThuc)
                    .setParameter("th", toHop)
                    .setParameter("m", mon)
                    .setParameter("x", x)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    public boolean add(BangQuyDoi bqd) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(bqd);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(BangQuyDoi bqd) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(bqd);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idQd) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BangQuyDoi bqd = session.get(BangQuyDoi.class, idQd);
            if (bqd != null) {
                session.remove(bqd);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
}

package vn.edu.sgu.phanmemtuyensinh.dal;

import java.util.List;

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
